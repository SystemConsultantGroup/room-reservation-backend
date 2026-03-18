package edu.skku.scg.reservation.domain.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import edu.skku.scg.reservation.domain.auth.jwt.JwtProvider;
import edu.skku.scg.reservation.domain.user.entity.User;
import edu.skku.scg.reservation.domain.user.entity.UserRole;
import edu.skku.scg.reservation.domain.user.repository.CollegeAdminRepository;
import edu.skku.scg.reservation.domain.user.repository.CollegeMembershipRepository;
import edu.skku.scg.reservation.domain.user.repository.UserRepository;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CollegeAdminRepository collegeAdminRepository;
    private final CollegeMembershipRepository collegeMembershipRepository;
    private final JwtProvider jwtProvider;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public AuthService(
            UserRepository userRepository,
            CollegeAdminRepository collegeAdminRepository,
            CollegeMembershipRepository collegeMembershipRepository,
            JwtProvider jwtProvider,
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String googleClientId) {
        this.userRepository = userRepository;
        this.collegeAdminRepository = collegeAdminRepository;
        this.collegeMembershipRepository = collegeMembershipRepository;
        this.jwtProvider = jwtProvider;
        this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(List.of(googleClientId))
                .build();
    }

    @Transactional
    public String verifyGoogleTokenAndLogin(String credential, String studentId) {
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
                .orElseGet(() -> registerNewUser(email, name, googleId, studentId));

        return  jwtProvider.createToken(user.getId());
    }

    private User registerNewUser(String email, String name, String googleId, String studentId) {
        User newUser = new User(email, name, studentId, googleId);

        return userRepository.save(newUser);
    }

    private List<Long> getAdminCollegeIds(User user) {
        if (user.getRole() == UserRole.COLLEGE_ADMIN) {
            return collegeAdminRepository.findAllByAdminId(user.getId())
                    .stream()
                    .map(ca -> ca.getCollege().getId())
                    .toList();
        }
        return Collections.emptyList();
    }

    private List<Long> getApprovedCollegeIds(User user) {
        return collegeMembershipRepository.findAllByUserId(user.getId())
                .stream()
                .map(cm -> cm.getCollege().getId())
                .toList();
    }
}