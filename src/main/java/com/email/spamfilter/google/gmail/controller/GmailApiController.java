package com.email.spamfilter.google.gmail.controller;

import com.email.spamfilter.google.gmail.service.GmailApiService;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
public class GmailApiController {
    private final GmailApiService gmailApiService;

    @GetMapping("/fetch-emails")
    public String fetchEmails(@AuthenticationPrincipal OAuth2User user) {
        try {
            gmailApiService.fetchEmails(user);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "emails";
    }


}
