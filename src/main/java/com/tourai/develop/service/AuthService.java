package com.tourai.develop.service;

import com.tourai.develop.domain.entity.Tag;
import com.tourai.develop.domain.entity.User;
import com.tourai.develop.dto.ReissueDto;
import com.tourai.develop.dto.SignUpDto;
import com.tourai.develop.exception.*;
import com.tourai.develop.exception.enumType.ErrorCode;
import com.tourai.develop.jwt.JwtUtil;
import com.tourai.develop.jwt.RefreshTokenService;
import com.tourai.develop.repository.TagRepository;
import com.tourai.develop.repository.UserRepository;
import com.tourai.develop.validation.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

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
    private final JwtUtil jwtUtil;
    public final static Long accessTokenExpiredMs = 60 * 60 * 10L;
    public final static Long refreshTokenExpiredMs = 864 * 100000L;

    @Transactional
    public void signUp(SignUpDto signUpDto) {

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

        userRepository.save(user);

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


}
