package com.email.spamfilter.google.gmail.controller;

import com.email.spamfilter.google.gmail.service.GmailApiService;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
@Slf4j
public class GmailApiController {

    private final GmailApiService gmailApiService;

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages() {
        try {
            String messages = gmailApiService.fetchMessages();
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch Gmail messages: " + e.getMessage());
        }
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
