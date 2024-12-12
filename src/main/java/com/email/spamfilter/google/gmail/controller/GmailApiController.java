package com.email.spamfilter.google.gmail.controller;

import com.email.spamfilter.google.gmail.service.GmailApiService;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

//@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
@Slf4j
@Controller
public class GmailApiController {

//    private final GmailApiService gmailApiService;
//
//    @GetMapping("/messages")
//    public ResponseEntity<?> getMessages() {
//        try {
//            String messages = gmailApiService.fetchMessages();
//            return ResponseEntity.ok(messages);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to fetch Gmail messages: " + e.getMessage());
//        }
//    }

    private final OAuth2AuthorizedClientService auth2AuthorizedClientService;

    @GetMapping("/test")
    public String test(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient auth2AuthorizedClient) {
        log.info("test 진입,OAuth2AuthorizedClient : {} ", auth2AuthorizedClient);


        OAuth2AccessToken accessToken = auth2AuthorizedClient.getAccessToken();
        log.info("access Token value : {} ",accessToken.getTokenValue());
        log.info("access Token Type : {} ",accessToken.getTokenType());
        return accessToken.getTokenValue().toString();
    }





//
//    @GetMapping("/emails")
//    public ResponseEntity<List<Message>> getEmails(@AuthenticationPrincipal UserDetails userDetails) {
//        // UserDetails에서 이메일 등 정보 추출
//        String username = userDetails.getUsername(); // 이메일 또는 사용자명
//        log.info("로그인된 사용자: {}", username);
//
//        List<Message> emails = gmailApiService.getEmails(username);
//        return ResponseEntity.ok(null);
//    }


}
