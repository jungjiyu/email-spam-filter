package com.email.spamfilter.google.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Collections;

/**
 *
 */
@Configuration
@RequiredArgsConstructor
public class GoogleApiConfig {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.google.scope}")
    private String scope;

//    private static final String TOKEN_SERVER_URL = "https://oauth2.googleapis.com/token";
//    private static final String AUTHORIZATION_SERVER_URL = "https://accounts.google.com/o/oauth2/v2/auth";
//
//    @Bean
//    public JacksonFactory jacksonFactory() {
//        return JacksonFactory.getDefaultInstance();
//    }
//
//    @Bean
//    public NetHttpTransport netHttpTransport() {
//        return new NetHttpTransport();
//    }
//
//    @Bean
//    public Gmail gmailApiService(JacksonFactory jacksonFactory, NetHttpTransport netHttpTransport) {
//        Credential credential = new Credential.Builder(Credential.AccessMethod.AUTHORIZATION_HEADER)
//                .setTransport(netHttpTransport)
//                .setJsonFactory(jacksonFactory)
//                .build();
//
//        return new Gmail.Builder(netHttpTransport, jacksonFactory, credential)
//                .setApplicationName("Your Application Name")
//                .build();
//    }
//
//    public String getAuthorizationUrl() {
//        GoogleAuthorizationCodeRequestUrl authorizationUrl = new GoogleAuthorizationCodeRequestUrl(
//                AUTHORIZATION_SERVER_URL, clientId, redirectUri, Collections.singletonList(scope));
//        return authorizationUrl.build();
//    }
//
//    public Credential exchangeCodeForToken(String authorizationCode, JacksonFactory jacksonFactory, NetHttpTransport netHttpTransport) throws IOException {
//        TokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
//                netHttpTransport,
//                jacksonFactory,
//                TOKEN_SERVER_URL,
//                clientId,
//                clientSecret,
//                authorizationCode,
//                redirectUri
//        ).execute();
//
//        return new Credential.Builder(Credential.AccessMethod.AUTHORIZATION_HEADER)
//                .setTransport(netHttpTransport)
//                .setJsonFactory(jacksonFactory)
//                .build()
//                .setFromTokenResponse(tokenResponse);
//    }
}


