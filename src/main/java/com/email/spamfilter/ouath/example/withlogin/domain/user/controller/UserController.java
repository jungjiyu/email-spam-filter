package com.email.spamfilter.ouath.example.withlogin.domain.user.controller;

import com.email.spamfilter.ouath.example.withlogin.domain.user.dto.UserSignUpDto;
import com.email.spamfilter.ouath.example.withlogin.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

//    @PostMapping("/sign-up")
//    public String signUp(@RequestBody UserSignUpDto userSignUpDto) throws Exception {
//        userService.signUp(userSignUpDto);
//        return "회원가입 성공";
//    }

    @GetMapping("/sign-up")
    public String showSignUpForm() {
        log.info("fffffffffffffffffffffffffffffffff");
        return "signUpForm"; // templates/signUpForm.html을 Thymeleaf 템플릿 엔진을 통해 렌더링
    }



    @PostMapping("/sign-up")
    public String signUp(@ModelAttribute UserSignUpDto userSignUpDto) throws Exception {
        // 회원가입 처리
        userService.signUp(userSignUpDto);
        log.info("post signUp 방문");

        // 회원가입 성공 후 리다이렉트 또는 페이지 반환
        return "redirect:/login"; // 로그인 페이지로 리다이렉트 또는 다른 페이지로 이동
    }


    @GetMapping("/jwt-test")
    public String jwtTest() {
        return "jwtTest 요청 성공";
    }
}
