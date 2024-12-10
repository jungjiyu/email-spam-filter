package com.email.spamfilter.ouath.example.withlogin.domain.user.controller;

import com.email.spamfilter.ouath.example.withlogin.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

}
