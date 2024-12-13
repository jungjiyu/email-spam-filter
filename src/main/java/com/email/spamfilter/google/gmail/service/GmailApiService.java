package com.email.spamfilter.google.gmail.service;


import com.email.spamfilter.google.global.service.GoogleTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GmailApiService {

    private final GoogleTokenService googleTokenService;
    private final RestTemplate restTemplate;


    private String GMAIL_API_BASE_URL = "https://gmail.googleapis.com/gmail/v1/users";

    /**
     * Fetch Gmail messages for the authenticated user
     * @param authorizationCode OAuth2 authorization code.
     * @return List of messages as a JSON string.
     */
    public String fetchMessages(String authorizationCode) {
        log.info("GmailApiService - fetchMessages() 진입");

        // Access Token 생성
        String accessToken = googleTokenService.getAccessToken(authorizationCode);
        log.info("Access Token 생성 성공: {}", accessToken);

        // Gmail API 메시지 목록 URL
        String messagesListUrl = GMAIL_API_BASE_URL + "/me/messages";

        // REST API 요청 준비
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            // Gmail API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    messagesListUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            log.info("Gmail API 호출 성공: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Gmail API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("Gmail API 호출 중 오류 발생", e);
        }
    }
}

