package edu.skku.scg.reservation.domain.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginResult;
import edu.skku.scg.reservation.domain.auth.dto.GoogleTokenResponse;
import edu.skku.scg.reservation.domain.auth.jwt.JwtProvider;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.entity.UserType;
import edu.skku.scg.reservation.domain.user.repository.UserManagementUnitRepository;
import edu.skku.scg.reservation.domain.user.repository.UserRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserManagementUnitRepository userManagementUnitRepository;
    private final JwtProvider jwtProvider;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String redirectUri;

    public AuthService(
            UserRepository userRepository,
            UserManagementUnitRepository userManagementUnitRepository,
            JwtProvider jwtProvider,
            @Value("${oauth.google.client-id}") String googleClientId,
            @Value("${oauth.google.client-secret}") String googleClientSecret,
            @Value("${oauth.google.redirect-uri}") String redirectUri
    ) {
        this.userRepository = userRepository;
        this.userManagementUnitRepository = userManagementUnitRepository;
        this.jwtProvider = jwtProvider;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.redirectUri = redirectUri;
        this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(List.of(googleClientId))
                .build();
    }

    public String getGoogleAuthUrl(String originUrl) {
        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", originUrl != null ? originUrl : "/")
                .build()
                .toUriString();
    }

    public GoogleLoginResult processGoogleCallback(String code) {
        String idToken = fetchIdTokenFromGoogle(code);
        return verifyGoogleTokenAndLogin(idToken);
    }

    @Transactional
    public String completeOnboarding(Long userId, UserType userType, String studentId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.completeOnboarding(userType, studentId);
        List<Long> managedUnitIds = userManagementUnitRepository.findAllManagedUnitIdsByUserId(user.getId());
        return jwtProvider.createAccessToken(
                user.getId(),
                user.getType(),
                managedUnitIds
        );
    }

    private String fetchIdTokenFromGoogle(String code) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", googleClientId);
        params.put("client_secret", googleClientSecret);
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "authorization_code");

        try {
            GoogleTokenResponse response = restTemplate.postForObject(
                    "https://oauth2.googleapis.com/token",
                    params,
                    GoogleTokenResponse.class
            );

            if (response == null || response.idToken() == null) {
                throw new BusinessException(ErrorCode.OAUTH_LOGIN_FAIL);
            }

            return response.idToken();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OAUTH_LOGIN_FAIL, e);
        }
    }

    private GoogleLoginResult verifyGoogleTokenAndLogin(String credential) {
        GoogleIdToken.Payload payload;
        String googleId;
        String email;
        String name;

        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(credential);
            if (idToken == null) {
                throw new IllegalArgumentException("유효하지 않은 구글 토큰입니다.");
            }

            payload = idToken.getPayload();
            googleId = payload.getSubject();
            email = payload.getEmail();
            name = payload.get("name").toString();

        } catch (Exception e) {
             throw new BusinessException(ErrorCode.OAUTH_LOGIN_FAIL, e);
        }

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> registerNewUser(googleId, email, name));

        List<Long> managedUnitIds = userManagementUnitRepository.findAllManagedUnitIdsByUserId(user.getId());

        return GoogleLoginResult.builder()
                .isGuest(user.getType() == UserType.GUEST)
                .accessToken(jwtProvider.createAccessToken(user.getId(), user.getType(), managedUnitIds))
                .build();
    }

    private User registerNewUser(String googleId, String email, String name) {
        try {
            User newUser = User.builder()
                    .googleId(googleId)
                    .email(email)
                    .name(name)
                    .build();

            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_USER);
        }
    }
}