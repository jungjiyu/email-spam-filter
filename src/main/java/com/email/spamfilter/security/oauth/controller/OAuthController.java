package com.email.spamfilter.security.oauth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/login/oauth2")
public class OAuthController {

    @GetMapping("/getCode")
    public ResponseEntity<String> getAuthorizationUrl() {
        String CLIENT_ID = "351156196442-fmr85i89fehgd5lsn1hsfu0jgem91qr0.apps.googleusercontent.com";
        String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/google";
        String SCOPE = "https://mail.google.com/";
        String RESPONSE_TYPE = "code";

        String oauthUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=" + SCOPE +
                "&response_type=" + RESPONSE_TYPE;

        return ResponseEntity.ok(oauthUrl);
    }

    @GetMapping("/code/google")
    public ResponseEntity<Void> handleOAuthCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        log.info("handleOAuthCallback-code: {}",code);
        // 리다이렉트된 code를 부모 창으로 전달
        String script = "<script>window.opener.postMessage({ code: '" + code + "' }, '*'); window.close();</script>";
        response.setContentType("text/html");
        response.getWriter().write(script);
        return ResponseEntity.ok().build();
    }
}
