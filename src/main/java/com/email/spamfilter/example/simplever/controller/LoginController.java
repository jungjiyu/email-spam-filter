//package com.email.spamfilter.ouath.example.simplever.controller;
//
//import com.email.spamfilter.ouath.example.simplever.service.LoginService;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping(value = "/login/oauth2", produces = "application/json")
//public class LoginController {
//
//    LoginService loginService;
//
//    public LoginController(LoginService loginService) {
//        this.loginService = loginService;
//    }
//
//    @GetMapping("/code/{registrationId}")
//    public void googleLogin(@RequestParam String code, @PathVariable String registrationId) {
//        loginService.socialLogin(code, registrationId);
//    }
//}
