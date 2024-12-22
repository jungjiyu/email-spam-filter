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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
@Slf4j
public class GmailApiController {

    private final GmailApiService gmailApiService;

    @GetMapping("/messages/list")
    public ResponseEntity<List<String>> getMsgIdLIst(@RequestParam("code") String authorizationCode) {
        log.info("getMsgIdLIst API 호출됨");

        // Gmail 메시지 가져오기
        List<String> messages;
        try {
            messages = gmailApiService.getMsgIdList(authorizationCode);
            log.info("Gmail 메시지 목록 가져오기 성공");
        } catch (Exception e) {
            log.error("Gmail 메시지 목록 가져오기 실패", e);
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.ok(messages);
    }

    /**
     * 특정 Gmail 메시지 가져오기
     * @param authorizationCode OAuth2 인증 코드
     * @param messageId 메시지 ID
     * @return 메시지 내용
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity<String> getMessageById(@RequestParam("code") String authorizationCode,
                                                   @PathVariable("id") String messageId) {
        log.info("getMessageById API 호출됨: messageId={}", messageId);

        try {
            // Gmail 메시지 내용 가져오기
            String message = gmailApiService.getMessageById(authorizationCode, messageId);
            log.info("Gmail 메시지 내용 가져오기 성공");
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("Gmail 메시지 내용 가져오기 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Gmail API 호출 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 특정 Gmail 메시지 가져오기
     * @param authorizationCode OAuth2 인증 코드
     * @return 메시지 내용
     */
    @GetMapping("/messages/all")
    public ResponseEntity<List<Map<String, Object>>> getAllMessages(@RequestParam("code") String authorizationCode) {

        try {
            // Gmail 메시지 내용 가져오기
            List<Map<String, Object>> message = gmailApiService.getAllMessages(authorizationCode);
            log.info("Gmail 메시지 내용 가져오기 성공");
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("Gmail 메시지 내용 가져오기 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}
