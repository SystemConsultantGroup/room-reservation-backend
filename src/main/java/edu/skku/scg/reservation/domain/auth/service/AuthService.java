package edu.skku.scg.reservation.domain.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import edu.skku.scg.reservation.domain.auth.dto.GoogleLoginResult;
import edu.skku.scg.reservation.domain.auth.dto.RegisterToken;
import edu.skku.scg.reservation.domain.auth.jwt.JwtProvider;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.entity.UserType;
import edu.skku.scg.reservation.domain.user.repository.UserManagementUnitRepository;
import edu.skku.scg.reservation.domain.user.repository.UserRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserManagementUnitRepository userManagementUnitRepository;
    private final JwtProvider jwtProvider;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public AuthService(
            UserRepository userRepository,
            UserManagementUnitRepository userManagementUnitRepository,
            JwtProvider jwtProvider,
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId) {
        this.userRepository = userRepository;
        this.userManagementUnitRepository = userManagementUnitRepository;
        this.jwtProvider = jwtProvider;
        this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(List.of(googleClientId))
                .build();
    }

    @Transactional
    public GoogleLoginResult verifyGoogleTokenAndLogin(String credential) {
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


        return userRepository.findByGoogleId(googleId)
                .map(user -> {
                    List<Long> managedUnitIds = userManagementUnitRepository.findAllManagedUnitIdsByUserId(user.getId());

                    return GoogleLoginResult.builder()
                            .isNewUser(false)
                            .accessToken(jwtProvider.createAccessToken(user.getId(), managedUnitIds))
                            .email(user.getEmail())
                            .name(user.getName())
                            .build();
                })
                .orElseGet(() -> GoogleLoginResult.builder()
                        .isNewUser(true)
                        .registerToken(jwtProvider.createRegisterToken(googleId, email, name))
                        .email(email)
                        .name(name)
                        .build());
    }

    public String registerNewUser(String registerToken, String studentId, UserType type) {
        RegisterToken registerTokenDto;
        try {
            registerTokenDto = jwtProvider.parseRegisterToken(registerToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, e);
        }

        if (userRepository.existsByGoogleId(registerTokenDto.googleId())) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_USER);
        }

        if (type == UserType.STUDENT) {
            if (studentId == null || !studentId.matches("^\\d{10}$")) {
                throw new BusinessException(ErrorCode.INVALID_STUDENT_ID_FORMAT);
            }
        } else if (type == UserType.FACULTY) {
            if (studentId != null && !studentId.isBlank()) {
                throw new BusinessException(ErrorCode.STUDENT_ID_NOT_ALLOWED);
            }
            studentId = null;
        }

        User newUser = User.builder()
                .googleId(registerTokenDto.googleId())
                .email(registerTokenDto.email())
                .name(registerTokenDto.name())
                .studentId(studentId)
                .type(type)
                .build();

        userRepository.save(newUser);

        return jwtProvider.createAccessToken(newUser.getId(), List.of());
    }
}