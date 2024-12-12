//package com.email.spamfilter.google.example.service;
//
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.googleapis.json.GoogleJsonError;
//import com.google.api.client.googleapis.json.GoogleJsonResponseException;
//import com.google.api.client.http.HttpRequestInitializer;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.services.gmail.Gmail;
//import com.google.api.services.gmail.GmailScopes;
//import com.google.api.services.gmail.model.Message;
//import jakarta.mail.MessagingException;
//import jakarta.mail.Session;
//import jakarta.mail.internet.InternetAddress;
//import jakarta.mail.internet.MimeMessage;
//import lombok.Builder;
//import org.apache.commons.codec.binary.Base64;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.Properties;
//
//public class GmailApiService {
//
//    /**
//     * Create a MimeMessage using the parameters provided.
//     *
//     * @param toEmailAddress   email address of the receiver
//     * @param fromEmailAddress email address of the sender, the mailbox account
//     * @param subject          subject of the email
//     * @param bodyText         body text of the email
//     * @return the MimeMessage to be used to send email
//     * @throws MessagingException - if a wrongly formatted address is encountered.
//     */
//    // 일반적인 이메일 메시지를 만들기
//    public MimeMessage createEmail(String toEmailAddress,
//                                          String fromEmailAddress,
//                                          String subject,
//                                          String bodyText)
//            throws MessagingException {
//        Properties props = new Properties();
//        Session session = Session.getDefaultInstance(props, null);
//
//        MimeMessage email = new MimeMessage(session);
//
//        email.setFrom(new InternetAddress(fromEmailAddress));
//        email.addRecipient(jakarta.mail.Message.RecipientType.TO,
//                new InternetAddress(toEmailAddress));
//        email.setSubject(subject);
//        email.setText(bodyText);
//        return email;
//    }
//
//    /**
//     * Create a message from an email.
//     *
//     * @param emailContent Email to be set to raw of message
//     * @return a message containing a base64url encoded email
//     * @throws IOException        - if service account credentials file not found.
//     * @throws MessagingException - if a wrongly formatted address is encountered.
//     */
//    //  MimeMessage를 인코딩하고 Message 객체를 인스턴스화한 다음 base64url로 인코딩된 메시지 문자열을 raw 속성의 값으로 설정
//    public Message createMessageWithEmail(MimeMessage emailContent)
//            throws MessagingException, IOException {
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        emailContent.writeTo(buffer);
//        byte[] bytes = buffer.toByteArray();
//        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
//        Message message = new com.google.api.services.gmail.model.Message();
//        message.setRaw(encodedEmail);
//        return message;
//    }
//
//    // 첨부파일 있는 메시지 만들기 : 생략
//
//    // 메시지 전송
//    public static Message sendEmail(String fromEmailAddress,
//                                    String toEmailAddress, String resourceServerAccessToken)
//            throws MessagingException, IOException {
//        /* Load pre-authorized user credentials from the environment.
//           TODO(developer) - See https://developers.google.com/identity for
//            guides on implementing OAuth2 for your application.*/
//
//        //GoogleCredential 유틸리티 클래스를 사용하여 Google 서비스에서 OAuth 2.0 인증을 수행
//            //
//        GoogleCredential credential = new GoogleCredential().
//                setAccessToken(resourceServerAccessToken);
//
//        Plus plus = new Plus.builder(new NetHttpTransport(),
//                GsonFactory.getDefaultInstance(),
//                credential)
//                .setApplicationName("Google-PlusSample/1.0")
//                .build();
//
//
////        GoogleCredentials credentials = GoogleCredentials
////                .createScoped(GmailScopes.GMAIL_SEND);
////        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
//
//        // Create the gmail API client
//        Gmail service = new Gmail.Builder(new NetHttpTransport(),
//                GsonFactory.getDefaultInstance(),
//                requestInitializer)
//                .setApplicationName("Gmail samples")
//                .build();
//
//        // Create the email content
//        String messageSubject = "Test message";
//        String bodyText = "lorem ipsum.";
//
//        // Encode as MIME message
//        Properties props = new Properties();
//        Session session = Session.getDefaultInstance(props, null);
//        MimeMessage email = new MimeMessage(session);
//        email.setFrom(new InternetAddress(fromEmailAddress));
//        email.addRecipient(jakarta.mail.Message.RecipientType.TO,
//                new InternetAddress(toEmailAddress));
//        email.setSubject(messageSubject);
//        email.setText(bodyText);
//
//        // Encode and wrap the MIME message into a gmail message
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        email.writeTo(buffer);
//        byte[] rawMessageBytes = buffer.toByteArray();
//        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
//        Message message = new Message();
//        message.setRaw(encodedEmail);
//
//        try {
//            // Create send message
//            message = service.users().messages().send("me", message).execute();
//            System.out.println("Message id: " + message.getId());
//            System.out.println(message.toPrettyString());
//            return message;
//        } catch (GoogleJsonResponseException e) {
//            // TODO(developer) - handle error appropriately
//            GoogleJsonError error = e.getDetails();
//            if (error.getCode() == 403) {
//                System.err.println("Unable to send message: " + e.getDetails());
//            } else {
//                throw e;
//            }
//        }
//        return null;
//    }
//
//
////    @Builder
////    class GoogleCredential{
////        private String AccessToken;
////    }
//
//
//
//
//}
