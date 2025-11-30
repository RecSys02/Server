package com.tourai.develop.service;

import com.tourai.develop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    // 회원정보 관리 메서드 구현
    private final UserRepository userRepository;



}
