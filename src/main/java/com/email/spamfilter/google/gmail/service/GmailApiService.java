package com.email.spamfilter.google.gmail.service;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GmailApiService {
    private final OAuth2AuthorizedClientService authorizedClientService;
    public void fetchEmails(OAuth2User user) throws IOException {
        // Access Token 가져오기
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                "google", user.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        // Gmail API 클라이언트 초기화
        Gmail gmail = new Gmail.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                GsonFactory.getDefaultInstance(), // 흠.. 일단 JsonFactory.getDefaultInstacne()  는 빨간줄 떠서 수정함. abstract 라서 그랬나??
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
        ).setApplicationName("My Gmail API App").build();

        // Gmail API 호출 - 예: 메시지 리스트 가져오기
        ListMessagesResponse messages = gmail.users().messages().list("me").execute();
        messages.getMessages().forEach(message -> {
            System.out.println("Message ID: " + message.getId());
        });
    }
}
