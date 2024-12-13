package com.email.spamfilter.google.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GoogleTokenService {
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    public String getAccessToken(String authorizationCode) {
        log.info("Access Token 요청 시작");


        // 요청 본문 구성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", authorizationCode);  // 클라이언트로부터 받은 인증 코드
        body.add("redirect_uri", redirectUri);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            // Access Token 요청
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("access_token")) {
                String accessToken = (String) responseBody.get("access_token");
                log.info("Access Token 획득 성공: {}", accessToken);
                return accessToken;
            } else {
                log.error("Access Token 획득 실패: 응답에 access_token 없음");
                throw new IllegalStateException("Access Token이 응답에 포함되지 않았습니다.");
            }
        } catch (Exception e) {
            log.error("Access Token 요청 중 오류 발생", e);
            throw new IllegalStateException("Access Token 요청 실패: " + e.getMessage());
        }
    }


}
