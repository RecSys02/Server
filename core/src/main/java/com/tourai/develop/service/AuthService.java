package com.tourai.develop.service;

import com.tourai.develop.aop.annotation.UserActionLog;
import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.domain.enumType.Action;
import com.tourai.develop.dto.AvailabilityResponse;
import com.tourai.develop.dto.ReissueDto;
import com.tourai.develop.dto.SignUpDto;
import com.tourai.develop.exception.*;
import com.tourai.develop.exception.enumType.ErrorCode;
import com.tourai.develop.jwt.JwtUtil;
import com.tourai.develop.jwt.RefreshTokenService;
import com.tourai.develop.oauth2.OAuth2LoginService;
import com.tourai.develop.oauth2.OAuth2SignUpService;
import com.tourai.develop.repository.TagRepository;
import com.tourai.develop.repository.UserRepository;
import com.tourai.develop.validation.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    // 로그인, 회원가입 관련 메서드 구현

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final PasswordValidator passwordValidator;
    private final RefreshTokenService refreshTokenService;
    private final OAuth2LoginService oAuth2LoginService;
    private final OAuth2SignUpService oAuth2SignUpService;
    private final JwtUtil jwtUtil;
    public final static Long accessTokenExpiredMs = 60 * 60 * 10L;
    public final static Long refreshTokenExpiredMs = 864 * 100000L;

    @Transactional
    @UserActionLog(action = Action.SIGN_UP)
    public User signUp(SignUpDto signUpDto) {

        //이메일 중복 확인
        if (userRepository.existsByEmail(signUpDto.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        //닉네임 중복 확인
        if (userRepository.existsByUserName(signUpDto.userName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }
        passwordValidator.validatePassword(signUpDto.password());

        String encodedPassword = bCryptPasswordEncoder.encode(signUpDto.password());


        User user = User.builder().userName(signUpDto.userName())
                .email(signUpDto.email())
                .password(encodedPassword).build();


        if (signUpDto.tagIds() != null) {
            for (Long tagId : signUpDto.tagIds()) {

                Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
                user.addTag(tag);
            }
        }

        return userRepository.save(user);

    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ReissueDto validateAndReissueToken(String refreshToken) {

        String username = validateRefreshTokenAndGetUsername(refreshToken);
        validateRefreshTokenMatchesRedis(refreshToken, username);
        String role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.createJwt("access", username, role, accessTokenExpiredMs);
        String newRefreshToken = jwtUtil.createJwt("refresh", username, role, refreshTokenExpiredMs);


        refreshTokenService.delete(username);
        refreshTokenService.save(username, newRefreshToken, Duration.ofMillis(refreshTokenExpiredMs));


        return ReissueDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

    }

    @Transactional
    public User findOrCreateAndGetUserForOAuth2(String provider, String providerId, String email) {

        String username = provider + "_" + providerId;
        Optional<User> findUser = userRepository.findByUserName(username);

        if (findUser.isPresent()) {
            //기존 OAuth2 회원일 경우
            return oAuth2LoginService.login(findUser.get());
        }
        //새롭게 OAuth2 가입하는 경우
        return oAuth2SignUpService.signUp(provider, providerId, email);

    }


    private String validateRefreshTokenAndGetUsername(String refreshToken) {
        if (refreshToken == null) {
            throw new AuthException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        if (jwtUtil.isExpired(refreshToken)) {
            throw new AuthException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }


        if (!(jwtUtil.getTokenType(refreshToken)).equals("refresh")) {
            throw new AuthException(ErrorCode.REFRESH_TOKEN_TYPE_INVALID);
        }

        return jwtUtil.getUsername(refreshToken);

    }

    private void validateRefreshTokenMatchesRedis(String refreshToken, String email) {
        if (!refreshTokenService.isMatch(email, refreshToken)) {
            throw new AuthException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }
    }

    @UserActionLog(action = Action.LOGIN)
    public void onLoginSuccess(User user) {
        // 로그인 성공 후 추가 로직이 필요하다면 여기에 작성
    }

    @UserActionLog(action = Action.LOGOUT)
    public void onLogoutSuccess(User user) {
        // 로그아웃 성공 후 추가 로직이 필요하다면 여기에 작성
    }

    public AvailabilityResponse checkEmailAvailable(String email) {
        boolean available = !userRepository.existsByEmail(email);
        return new AvailabilityResponse(available);
    }

    public AvailabilityResponse checkNameAvailable(String userName) {
        boolean available = !userRepository.existsByUserName(userName);
        return new AvailabilityResponse(available);
    }

}
