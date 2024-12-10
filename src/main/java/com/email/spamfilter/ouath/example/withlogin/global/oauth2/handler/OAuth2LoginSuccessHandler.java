package com.email.spamfilter.ouath.example.withlogin.global.oauth2.handler;

import com.email.spamfilter.ouath.example.withlogin.domain.user.Role;
import com.email.spamfilter.ouath.example.withlogin.domain.user.User;
import com.email.spamfilter.ouath.example.withlogin.domain.user.dto.UserSignUpDto;
import com.email.spamfilter.ouath.example.withlogin.domain.user.repository.UserRepository;
import com.email.spamfilter.ouath.example.withlogin.domain.user.service.UserService;
import com.email.spamfilter.ouath.example.withlogin.global.jwt.service.JwtService;
import com.email.spamfilter.ouath.example.withlogin.global.oauth2.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
//@Transactional
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;



    public void tempsignUp(UserSignUpDto userSignUpDto) throws Exception {
        log.info("tempsignUp 호출됨");

        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        if (userRepository.findByNickname(userSignUpDto.getNickname()).isPresent()) {
            throw new Exception("이미 존재하는 닉네임입니다.");
        }

        // password 설정은 아직 안함
        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .nickname(userSignUpDto.getNickname())
                .age(userSignUpDto.getAge())
                .city(userSignUpDto.getCity())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    log.info("OAuth2 Login 성공!");
    try {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // User의 Role이 GUEST일 경우 처음 요청한 회원이므로 회원가입 페이지로 리다이렉트
        if(oAuth2User.getRole() == Role.GUEST) {

            // 회원가입 처리 로직
            UserSignUpDto userSignUpDto = new UserSignUpDto();
            userSignUpDto.setEmail(oAuth2User.getEmail());
            userSignUpDto.setNickname(oAuth2User.getNickName());
            // 추가적인 정보들 (ex: age, city 등)도 채울 수 있습니다.

            // 회원가입 서비스 호출
//            userService.signUp(userSignUpDto);
            this.tempsignUp(userSignUpDto);



            String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
            response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
            log.info("accessToken : {}", accessToken);
//            response.sendRedirect("/sign-up");  // 리다이렉트 경로 수정
            // Refresh Token 생성 및 추가
            jwtService.sendAccessAndRefreshToken(response, accessToken, null);
            // 회원가입 후에 바로 응답을 보내거나, 리다이렉트를 하지 않고 완료된 메시지 또는 JSON 응답을 클라이언트에 전달할 수 있음
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("회원가입 성공");
            log.info("onAuthenticationSuccess 에서 회원가입 성공");

        }
        else {
            log.info("로그인에 성공");
            // GUEST가 아닌 경우에는 정상적인 로그인 완료 처리를 진행 (필요 시 추가 로직)
            log.info("이미 회원 가입 완료된 사용자: {}", oAuth2User.getEmail());

            loginSuccess(response, oAuth2User); // 로그인에 성공한 경우 access, refresh token 등을 처리
        }
    } catch (Exception e) {
        log.error("OAuth2 Login 실패", e);
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
        jwtService.reIssueAccessToken(response, refreshToken);
    }
}
