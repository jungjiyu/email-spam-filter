package com.email.spamfilter.google.gmail.service;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GmailApiService {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    private static final String GMAIL_API_URL = "https://gmail.googleapis.com/gmail/v1/users/me/messages";

    public String fetchMessages() {
        log.info("GmailApiService 의 fetchMessages 진입");
        // 인증된 사용자의 Access Token을 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new IllegalStateException("Authentication 객체가 null입니다. 사용자 인증이 필요합니다.");

        log.info("Authentication: {}", authentication);

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("google")
                .principal(authentication)
                .build();
        log.info("authorizeRequest: {}", authorizeRequest);
        log.info("authorizedClient: {}", authorizeRequest.getAuthorizedClient());
        log.info("authorizedClient's AccessToken: {}", authorizeRequest.getAuthorizedClient().getAccessToken());
        log.info("getClientRegistrationId: {}", authorizeRequest.getClientRegistrationId());
        log.info("getAttributes: {}", authorizeRequest.getAttributes());

        OAuth2AuthorizedClient authorizedClient =null;

        try {
            authorizedClient= authorizedClientManager.authorize(authorizeRequest);
        } catch (Exception e) {
            log.error("exception 발생 : {} ", e.getCause());
            throw e; // 원인을 확인한 후 필요한 경우 예외를 다시 던집니다.
        }
        if (authorizedClient == null ) {
            log.info("authorizedClient == null");
            throw new IllegalStateException("Access Token을 가져올 수 없습니다.");
        }


        if (authorizedClient.getAccessToken() == null) {
            log.info("authorizedClient.getAccessToken() == null");
            throw new IllegalStateException("Access Token을 가져올 수 없습니다.");
        }


        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        log.info("accessToken: {}",accessToken);


        // RestTemplate으로 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                GMAIL_API_URL,
                HttpMethod.GET,
                request,
                String.class
        );

        return response.getBody();
    }


//    private final Gmail gmail;

//    public List<Message> getEmails(String userId) {
//        try {
//            log.info("Fetching emails for userId: {}", userId);
//
//            // Gmail API의 'users.messages.list' 호출
//            ListMessagesResponse response = gmail.users().messages().list(userId).execute();
//            List<Message> messages = new ArrayList<>();
//            if (response.getMessages() != null) {
//                for (Message message : response.getMessages()) {
//                    // 각 메시지 정보를 Gmail API에서 가져옴
//                    Message fullMessage = gmail.users().messages().get(userId, message.getId()).execute();
//                    messages.add(fullMessage);
//                }
//            }
//            return messages;
//        } catch (IOException e) {
//            log.error("Error while fetching emails", e);
//            throw new RuntimeException("이메일 목록을 가져오는 중 오류가 발생했습니다.", e);
//        }
//    }
}
