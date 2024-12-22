package com.email.spamfilter.google.global.controller;


import com.email.spamfilter.google.global.service.GoogleTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/google")
@RequiredArgsConstructor
@Slf4j
@Controller
public class GoogleTokenController {

    private final GoogleTokenService googleTokenService;


    /**
     * 실제로 호출할 일은 없음. (GoogpleApi단 에서 활용되는지) 테스트 용으로만 ..
     * @param authorizationCode
     * @return
     */
    @GetMapping("/token")
    public ResponseEntity<String> getAccessToken(@RequestParam("code") String authorizationCode) {
        log.info("Google OAuth2 Callback 호출됨, authorizationCode: {}", authorizationCode);

        try {
            String accessToken = googleTokenService.getAccessToken(authorizationCode);
            return ResponseEntity.ok("Access Token: " + accessToken);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Access Token 요청 실패: " + e.getMessage());
        }
    }
}
