package com.email.spamfilter.ouath.example.withlogin.domain.user.service;

import com.email.spamfilter.ouath.example.withlogin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;

}


