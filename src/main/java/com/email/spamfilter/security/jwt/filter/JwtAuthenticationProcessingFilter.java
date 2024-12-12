package com.email.spamfilter.security.jwt.filter;

import com.email.spamfilter.user.entity.User;
import com.email.spamfilter.security.jwt.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Jwt 인증 필터
 * "/login" 이외의 URI 요청이 왔을 때 처리하는 필터
 *
 * 기본적으로 사용자는 요청 헤더에 AccessToken만 담아서 요청
 * AccessToken 만료 시에만 RefreshToken을 요청 헤더에 AccessToken과 함께 요청
 *
 * 1. RefreshToken이 없고, AccessToken이 유효한 경우 -> 인증 성공 처리, RefreshToken을 재발급하지는 않는다.
 * 2. RefreshToken이 없고, AccessToken이 없거나 유효하지 않은 경우 -> 인증 실패 처리, 403 ERROR
 * 3. RefreshToken이 있는 경우 -> DB의 RefreshToken과 비교하여 일치하면 AccessToken 재발급, RefreshToken 재발급(RTR 방식)
 *                              인증 성공 처리는 하지 않고 실패 처리
 *
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URL = "/login"; // "/login"으로 들어오는 요청은 Filter 작동 X

    private final JwtService jwtService;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().equals(NO_CHECK_URL) || request.getRequestURI().startsWith("/oauth2")  ) {
            log.info("로그인 관련 요청이므로 필터를 건너뛰고 다음 필터로 진행합니다.");
            filterChain.doFilter(request, response); // "/login" 요청이 들어오면, 다음 필터 호출
            return; // return으로 이후 현재 필터 진행 막기 (안해주면 아래로 내려가서 계속 필터 진행시킴)
        }

        // 사용자 요청 헤더에서 RefreshToken 추출
        // -> RefreshToken이 없거나 유효하지 않다면(DB에 저장된 RefreshToken과 다르다면) null을 반환
        // 사용자의 요청 헤더에 RefreshToken이 있는 경우는, AccessToken이 만료되어 요청한 경우밖에 없다.
        // 따라서, 위의 경우를 제외하면 추출한 refreshToken은 모두 null
        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        // 리프레시 토큰이 요청 헤더에 존재했다면, 사용자가 AccessToken이 만료되어서
        // RefreshToken까지 보낸 것이므로 리프레시 토큰이 DB의 리프레시 토큰과 일치하는지 판단 후,
        // 일치한다면 AccessToken을 재발급해준다.
        if (refreshToken != null) {
            jwtService.reIssueAccessToken(response, refreshToken);
            return; // RefreshToken을 보낸 경우에는 AccessToken을 재발급 하고 인증 처리는 하지 않게 하기위해 바로 return으로 필터 진행 막기
        }

        // RefreshToken이 없거나 유효하지 않다면, AccessToken을 검사하고 인증을 처리하는 로직 수행
        // AccessToken이 없거나 유효하지 않다면, 인증 객체가 담기지 않은 상태로 다음 필터로 넘어가기 때문에 403 에러 발생
        // AccessToken이 유효하다면, 인증 객체가 담긴 상태로 다음 필터로 넘어가기 때문에 인증 성공
        if (refreshToken == null) {
            checkAccessTokenAndAuthentication(request, response, filterChain);
        }
    }



    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {


        // AccessToken을 추출하여 검증 및 인증 처리
        jwtService.extractAccessToken(request)
                .flatMap(jwtService::authenticateAccessToken)
                .ifPresent(this::saveAuthentication);

        log.info("checkAccessTokenAndAuthentication() 실행 완료, 다음 필터로 진입 시도");

        // 다음 필터로 요청을 전달
        filterChain.doFilter(request, response);
    }

    /**
     * [인증 허가 메소드]
     * 파라미터의 유저 : 우리가 만든 회원 객체 / 빌더의 유저 : UserDetails의 User 객체
     *
     * new UsernamePasswordAuthenticationToken()로 인증 객체인 Authentication 객체 생성
     * UsernamePasswordAuthenticationToken의 파라미터
     * 1. 위에서 만든 UserDetailsUser 객체 (유저 정보)
     * 2. credential(보통 비밀번호로, 인증 시에는 보통 null로 제거)
     * 3. Collection < ? extends GrantedAuthority>로,
     * UserDetails의 User 객체 안에 Set<GrantedAuthority> authorities이 있어서 getter로 호출한 후에,
     * new NullAuthoritiesMapper()로 GrantedAuthoritiesMapper 객체를 생성하고 mapAuthorities()에 담기
     *
     * SecurityContextHolder.getContext()로 SecurityContext를 꺼낸 후,
     * setAuthentication()을 이용하여 위에서 만든 Authentication 객체에 대한 인증 허가 처리
     */
    public void saveAuthentication(User user) {
        log.info("saveAuthentication() 호출됨");

        // 소셜 로그인 전용이라 생password 로직 생략함
//        String password = user.getPassword();
//        if (password == null) { // 소셜 로그인 유저의 비밀번호 임의로 설정 하여 소셜 로그인 유저도 인증 되도록 설정
//            password = PasswordUtil.generateRandomPassword();
//        }

        log.info("saveAuthentication() 에서 받은 유저 : {} ", user.toString());
        log.info("user.getEmail(): {} ,  user.getRole().name() : {}", user.getEmail(), user.getRole().name());

        // ver1
        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // email 을 user name 으로 저장
                .password("") // User.builder() 쓸 때 password 값이 null 이면 에러 발생하게 되있어서 의미없는 값이라도 설정해줘야된다
                .roles(user.getRole().name())
                .build();

        log.info("userDetailsUser 객체 생성: {}",userDetailsUser.toString());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetailsUser,
                        null,
                        userDetailsUser.getAuthorities()
                );

        log.info("Authentication 객체 생성 완료: {}", authentication);



        // ver2
/*
        // GrantedAuthority 설정
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // OAuth2 인증 토큰 생성
        OAuth2AuthenticationToken authentication =
                new OAuth2AuthenticationToken(
                        new DefaultOAuth2User(
                                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                                Map.of("email", user.getEmail()), // 사용자 속성
                                "email" // 사용자 식별 키
                        ),
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                        registrationId
                );
        log.info("Authentication 객체 생성 완료: {}", authentication);

  */

        // OAuth2로 로그인 인증을 수행하면, Principal은 OAuth2User 객체로써 SecurityContext에 인증된 Authentication으로 저장된다..?
            // 그러니까 principal 은 oAuth2User 로 캐스팅이 가능해진다?



        // SecurityContextHolder에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("authentication 객체 securityContextHodler에 저장됨");
    }
}
