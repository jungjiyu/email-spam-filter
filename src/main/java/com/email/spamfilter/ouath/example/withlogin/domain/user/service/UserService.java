package com.email.spamfilter.ouath.example.withlogin.domain.user.service;

import com.email.spamfilter.ouath.example.withlogin.domain.user.Role;
import com.email.spamfilter.ouath.example.withlogin.domain.user.User;
import com.email.spamfilter.ouath.example.withlogin.domain.user.dto.UserSignUpDto;
import com.email.spamfilter.ouath.example.withlogin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 자체 회원가입을 위한 메서드
     * @param userSignUpDto
     * @throws Exception
     */
    public void signUp(UserSignUpDto userSignUpDto) throws Exception {

        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        if (userRepository.findByNickname(userSignUpDto.getNickname()).isPresent()) {
            throw new Exception("이미 존재하는 닉네임입니다.");
        }

        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .password(userSignUpDto.getPassword())
                .nickname(userSignUpDto.getNickname())
                .age(userSignUpDto.getAge())
                .city(userSignUpDto.getCity())
                .role(Role.USER)
                .build();

        user.passwordEncode(passwordEncoder);
        userRepository.save(user);
    }
}


