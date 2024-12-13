package com.email.spamfilter.google.gmail.controller;

import com.email.spamfilter.google.global.service.GoogleTokenService;
import com.email.spamfilter.google.gmail.service.GmailApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

//@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
@Slf4j
@Controller
public class GmailApiController {

    private final GmailApiService gmailApiService;

    @GetMapping("/messages")
    public ResponseEntity<String> fetchMessages(@RequestParam("code") String authorizationCode) {
        log.info("fetchMessages API 호출됨");


        // Gmail 메시지 가져오기
        String messages;
        try {
            messages = gmailApiService.fetchMessages(authorizationCode);
            log.info("Gmail 메시지 목록 가져오기 성공");
        } catch (Exception e) {
            log.error("Gmail 메시지 목록 가져오기 실패", e);
            return ResponseEntity.status(500).body("Gmail API 호출 중 오류 발생: " + e.getMessage());
        }

        return ResponseEntity.ok(messages);
    }
}
