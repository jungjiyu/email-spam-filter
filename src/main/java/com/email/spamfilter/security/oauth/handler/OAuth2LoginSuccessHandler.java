package com.email.spamfilter.security.oauth.handler;

import com.email.spamfilter.global.exception.BusinessException;
import com.email.spamfilter.global.exception.ExceptionType;
import com.email.spamfilter.security.oauth.dto.OAuthAttributes;
import com.email.spamfilter.user.enums.Role;
import com.email.spamfilter.user.entity.User;
import com.email.spamfilter.user.enums.SocialType;
import com.email.spamfilter.user.repository.UserRepository;
import com.email.spamfilter.security.jwt.service.JwtService;
import com.email.spamfilter.security.oauth.dto.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;





    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    log.info("OAuth2 Login 성공하여 onAuthenticationSuccess 호줄됨");
    try {
        CustomOAuth2User oAuth2User = null;

        try{
            oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        }catch(ClassCastException e){
            log.info("oicd user : {}",e.getMessage());
            DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
            User user = userRepository.findByEmail(oidcUser.getEmail()).orElse(null);

            String accessToken = jwtService.createAccessToken(oidcUser.getEmail());
            String refreshToken = jwtService.createRefreshToken();
            log.info("accessToken : {}, refreshToken : {}", accessToken,refreshToken);

            // 회원가입 (db저장)
            if(user == null ){
                userRepository.save(OAuthAttributes.toEntityforOICD(SocialType.GOOGLE, oidcUser,refreshToken));


                // 회원가입 후에 바로 응답을 보내거나, 리다이렉트를 하지 않고 완료된 메시지 또는 JSON 응답을 클라이언트에 전달할 수 있음
                response.getWriter().write("회원가입 성공");

            }

            response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
            response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);

            jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
            log.info("catch문에서 sendAccessAndRefreshToken 성공");


            return;
        }

        // User의 Role이 GUEST일 경우 회원가입 로직 수행
        if(oAuth2User.getRole() == Role.GUEST) {
            log.info("{} 는 role 이 GUEST ", oAuth2User.getEmail());
            User user = userRepository.findByEmail(oAuth2User.getEmail()).orElseThrow(()->new BusinessException(ExceptionType.USER_NOT_FOUND));

            String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
            String refreshToken = jwtService.createRefreshToken();

            //TODO : 왜 더티채킹이 안되지? 왜 직접 save 를 해줘야되지?
            user.updateRefreshToken(refreshToken);
            user.authorizeUser(); // role 을 USER 로 변경
            userRepository.save(user);




//            response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);

            log.info("accessToken : {}, refreshToken : {}", accessToken,refreshToken);

            // Refresh Token 과 Access Token 을 헤더에 담음
            jwtService.sendAccessAndRefreshToken(response, accessToken, null);

            // 회원가입 후에 바로 응답을 보내거나, 리다이렉트를 하지 않고 완료된 메시지 또는 JSON 응답을 클라이언트에 전달할 수 있음
            response.getWriter().write("회원가입 성공");
            log.info("onAuthenticationSuccess 에서 회원가입 성공");

        }
        else {
            log.info(" onAuthenticationSuccess 에서 로그인 성공");
            // GUEST가 아닌 경우에는 정상적인 로그인 완료 처리를 진행
            log.info("이미 회원 가입 완료된 사용자: {}", oAuth2User.getEmail());

            loginSuccess(response, oAuth2User); // 로그인에 성공한 경우 access, refresh token 등을 처리
        }
    } catch (Exception e) {
        log.error("OAuth2 Login 통한 로그인 실패", e);
    }
}



//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        log.info("OAuth2 Login 성공!");
//        try {
//            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
//
//            // User의 Role이 GUEST일 경우 처음 요청한 회원이므로 회원가입 페이지로 리다이렉트
//            if(oAuth2User.getRole() == Role.GUEST) {
//                String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
//                response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
//                log.info("accessToken : {}",accessToken);
//                jwtService.sendAccessAndRefreshToken(response, accessToken, null);
//                response.sendRedirect("oauth2/sign-up"); // 프론트의 회원가입 추가 정보 입력 폼으로 리다이렉트
//
////                User findUser = userRepository.findByEmail(oAuth2User.getEmail())
////                                .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 유저가 없습니다."));
////                findUser.authorizeUser();
//            } else {
//                log.info("로그인에 성공");
//                loginSuccess(response, oAuth2User); // 로그인에 성공한 경우 access, refresh 토큰 생성
//            }
//        } catch (Exception e) {
//            throw e;
//        }
//
//    }

    // TODO : 소셜 로그인 시에도 무조건 토큰 생성하지 말고 JWT 인증 필터처럼 RefreshToken 유/무에 따라 다르게 처리해보기
    private void loginSuccess(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
        String refreshToken = jwtService.createRefreshToken();
        response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
        response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        log.info("loginSuccess에서 sendAccessAndRefreshToken 성공");
//        jwtService.reIssueAccessToken(response, refreshToken);
//        log.info("loginSuccess에서 reIssueAccessToken 성공");

    }
}
