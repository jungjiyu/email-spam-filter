package com.email.spamfilter.google.gmail.service;


import com.email.spamfilter.google.global.service.GoogleTokenService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;





@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GmailApiService {

    private final GoogleTokenService googleTokenService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String GMAIL_API_BASE_URL = "https://gmail.googleapis.com/gmail/v1/users";

    private String accessToken; // Access Token 저장 필드

    /**
     * Access Token을 가져오는 메서드 (필드에 저장, 비어있으면 새로 발급)
     * @param authorizationCode OAuth2 authorization code.
     * @return Access Token.
     */
    private String getAccessToken(String authorizationCode) {
        if (accessToken == null || accessToken.isEmpty()) {
            log.info("Access Token이 비어 있으므로 새로 발급합니다.");
            accessToken = googleTokenService.getAccessToken(authorizationCode);
            log.info("Access Token 발급 완료: {}", accessToken);
        } else {
            log.info("기존 Access Token 재사용: {}", accessToken);
        }
        return accessToken;
    }

    /**
     * Fetch all Gmail message IDs for the authenticated user.
     * @param authorizationCode OAuth2 authorization code.
     * @return List of message IDs.
     */
    public List<String> getMsgIdList(String authorizationCode) {
        log.info("GmailApiService - getMsgIdList() 진입");

        String token = getAccessToken(authorizationCode); // Access Token 가져오기
        String messagesListUrl = GMAIL_API_BASE_URL + "/me/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        List<String> messageIds = new ArrayList<>();
        String nextPageToken = null;

        do {
            try {
                // Gmail API는 한 번의 호출로 기본적으로 최대 100개의 메시지 목록을 반환하며, 추가 메시지를 가져오기 위해 pageToken을 사용해야
                String urlWithToken = nextPageToken == null ? messagesListUrl : messagesListUrl + "?pageToken=" + nextPageToken;
                ResponseEntity<Map> response = restTemplate.exchange(urlWithToken, HttpMethod.GET, requestEntity, Map.class);

                Map<String, Object> responseBody = response.getBody();
                if (responseBody == null || !responseBody.containsKey("messages")) {
                    log.warn("Gmail API 응답에 메시지 목록 없음");
                    break;
                }

                List<Map<String, String>> messages = (List<Map<String, String>>) responseBody.get("messages");
                for (Map<String, String> message : messages) {
                    messageIds.add(message.get("id"));
                }

                nextPageToken = (String) responseBody.get("nextPageToken");
            } catch (Exception e) {
                log.error("Gmail API 호출 중 오류 발생: {}", e.getMessage());
                throw new RuntimeException("Gmail 메시지 ID를 가져오는 중 오류 발생", e);
            }
        } while (nextPageToken != null);

        log.info("총 메시지 ID 개수: {}", messageIds.size());
        return messageIds;
    }

    /**
     * Fetch all Gmail messages' content for the authenticated user.
     * @param authorizationCode OAuth2 authorization code.
     * @return List of detailed messages.
     */
    public List<Map<String, Object>> getAllMessages(String authorizationCode) {
        log.info("GmailApiService - getAllMessages() 진입");

        List<String> messageIds = getMsgIdList(authorizationCode);
        log.info("가져온 메시지 ID 개수: {}", messageIds.size());

        String token = getAccessToken(authorizationCode); // Access Token 가져오기
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        List<Map<String, Object>> parsedMessages = new ArrayList<>();
        for (String messageId : messageIds) {
            String messageDetailsUrl = GMAIL_API_BASE_URL + "/me/messages/" + messageId;
            try {
                ResponseEntity<String> response = restTemplate.exchange(messageDetailsUrl, HttpMethod.GET, requestEntity, String.class);
                Map<String, Object> parsedMessage = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
                parsedMessages.add(parsedMessage);
                log.info("메시지 ID {} 상세 정보 추가 완료", messageId);
            } catch (Exception e) {
                log.error("Gmail 메시지 ID {} 상세 정보 가져오기 실패: {}", messageId, e.getMessage());
            }
        }

        log.info("모든 메시지 상세 정보 요청 완료");
        return parsedMessages;
    }

    /**
     * Fetch a specific Gmail message's content by ID.
     * @param authorizationCode OAuth2 authorization code.
     * @param messageId ID of the Gmail message.
     * @return Content of the specified Gmail message as a JSON string.
     */
    public String getMessageById(String authorizationCode, String messageId) {
        log.info("GmailApiService - fetchMessageById() 진입, messageId: {}", messageId);

        String token = getAccessToken(authorizationCode); // Access Token 가져오기
        String messageUrl = GMAIL_API_BASE_URL + "/me/messages/" + messageId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(messageUrl, HttpMethod.GET, requestEntity, String.class);
            log.info("Gmail 메시지 가져오기 성공: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Gmail 메시지 가져오기 실패: {}", e.getMessage());
            throw new RuntimeException("Gmail 메시지 가져오는 중 오류 발생", e);
        }
    }




}





