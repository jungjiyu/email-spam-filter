//package com.email.spamfilter.google.gmail.service;
//
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.services.gmail.Gmail;
//import com.google.api.services.gmail.model.ListMessagesResponse;
//import com.google.api.services.gmail.model.Message;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class GmailApiService {
//
//    private final Gmail gmail;
//
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
//}
