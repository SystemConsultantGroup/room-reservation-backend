package edu.skku.scg.reservation.domain.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginResult;
import edu.skku.scg.reservation.domain.auth.dto.OnboardingRequest;
import edu.skku.scg.reservation.domain.auth.jwt.JwtProvider;
import edu.skku.scg.reservation.domain.organization.service.MajorService;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.entity.UserType;
import edu.skku.scg.reservation.domain.user.repository.UserManagementUnitRepository;
import edu.skku.scg.reservation.domain.user.repository.UserRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserManagementUnitRepository userManagementUnitRepository;
    private final JwtProvider jwtProvider;
    private final MajorService majorService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final NetHttpTransport transport;
    private final GsonFactory jsonFactory;

    private final String googleClientId;
    private final String googleClientSecret;
    private final String googleCallbackUri;

    public AuthService(
            UserRepository userRepository,
            UserManagementUnitRepository userManagementUnitRepository,
            JwtProvider jwtProvider,
            MajorService majorService,
            @Value("${oauth.google.client-id}") String googleClientId,
            @Value("${oauth.google.client-secret}") String googleClientSecret,
            @Value("${oauth.google.callback-uri}") String googleCallbackUri
    ) {
        this.userRepository = userRepository;
        this.userManagementUnitRepository = userManagementUnitRepository;
        this.jwtProvider = jwtProvider;
        this.majorService = majorService;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.googleCallbackUri = googleCallbackUri;
        this.transport = new NetHttpTransport();
        this.jsonFactory = new GsonFactory();

        this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(List.of(googleClientId))
                .build();
    }

    public String getGoogleAuthUrl(String originUrl) {
        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleCallbackUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", originUrl)
                .encode()
                .build()
                .toUriString();
    }

    public GoogleLoginResult processGoogleCallback(String code) {
        String idToken = fetchIdTokenFromGoogle(code);
        return verifyGoogleTokenAndLogin(idToken);
    }

    @Transactional
    public String completeOnboarding(Long userId, OnboardingRequest dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.completeOnboarding(dto.name(), dto.userType(), dto.studentId());

        majorService.applyMajor(user.getId(), dto.majors());

        List<Long> managingUnitIds = userManagementUnitRepository.findAllManagementUnitIdsByUserId(user.getId());

        return jwtProvider.createAccessToken(
                user.getId(),
                user.getType(),
                managingUnitIds
        );
    }

    private String fetchIdTokenFromGoogle(String code) {
        try {
            GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                    transport,
                    jsonFactory,
                    "https://oauth2.googleapis.com/token",
                    googleClientId,
                    googleClientSecret,
                    code,
                    googleCallbackUri
            ).execute();

            return response.getIdToken();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OAUTH_LOGIN_FAIL, e);
        }
    }

    private GoogleLoginResult verifyGoogleTokenAndLogin(String credential) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(credential);
            if (idToken == null) {
                throw new IllegalArgumentException("유효하지 않은 구글 토큰입니다.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            User user = userRepository.findByGoogleId(googleId)
                    .orElseGet(() -> registerNewUser(googleId, email, name));

            List<Long> managingUnitIds = userManagementUnitRepository.findAllManagementUnitIdsByUserId(user.getId());

            return GoogleLoginResult.builder()
                    .isGuest(user.getType() == UserType.GUEST)
                    .accessToken(jwtProvider.createAccessToken(user.getId(), user.getType(), managingUnitIds))
                    .build();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OAUTH_LOGIN_FAIL, e);
        }
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