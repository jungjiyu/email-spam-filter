package com.email.spamfilter.ouath.example.withlogin.global.jwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.email.spamfilter.global.exception.BusinessException;
import com.email.spamfilter.global.exception.ExceptionType;
import com.email.spamfilter.ouath.example.withlogin.domain.user.User;
import com.email.spamfilter.ouath.example.withlogin.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Enumeration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
@Transactional
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    /**
     * JWT의 Subject와 Claim으로 email 사용 -> 클레임의 name을 "email"으로 설정
     * JWT의 헤더에 들어오는 값 : 'Authorization(Key) = Bearer {토큰} (Value)' 형식
     */
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String BEARER = "Bearer ";

    private final UserRepository userRepository;

    /**
     * AccessToken 생성 메소드
     */
    public String createAccessToken(String email) {
        Date now = new Date();
        return JWT.create() // JWT 토큰을 생성하는 빌더 반환
                .withSubject(ACCESS_TOKEN_SUBJECT) // JWT의 Subject 지정 -> AccessToken이므로 AccessToken
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod)) // 토큰 만료 시간 설정

                //클레임으로는 저희는 email 하나만 사용합니다.
                //추가적으로 식별자나, 이름 등의 정보를 더 추가하셔도 됩니다.
                //추가하실 경우 .withClaim(클래임 이름, 클래임 값) 으로 설정해주시면 됩니다
                .withClaim(EMAIL_CLAIM, email)
                .sign(Algorithm.HMAC512(secretKey)); // HMAC512 알고리즘 사용, application-jwt.yml에서 지정한 secret 키로 암호화
    }

    /**
     * RefreshToken 생성
     * RefreshToken은 Claim에 email도 넣지 않으므로 withClaim() X
     */
    public String createRefreshToken() {
        Date now = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    /**
     * AccessToken 헤더에 실어서 보내기
     */
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        response.setHeader(accessHeader, accessToken);
        log.info("재발급된 Access Token : {}", accessToken);
    }

    /**
     * AccessToken + RefreshToken 헤더에 실어서 보내기
     */
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
        log.info("Access Token, Refresh Token 헤더 설정 완료");

    }

    /**
     * 헤더에서 RefreshToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * 헤더에서 AccessToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
//    public Optional<String> extractAccessToken(HttpServletRequest request) {
//        log.info("extractAccessToken( ) 호출됨");
//        return Optional.ofNullable(request.getHeader(accessHeader))
//                .filter(refreshToken -> refreshToken.startsWith(BEARER))
//                .map(refreshToken -> refreshToken.replace(BEARER, ""));
//    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        log.info("extractAccessToken() 호출됨");
        String requestUri = request.getRequestURI();
        log.info("Request URI: {}", requestUri);

        // OAuth2 인증 요청인 경우, 액세스 토큰을 추출하지 않음
        if (requestUri.startsWith("/oauth2/authorization")  ) {
            log.warn("OAuth2 인증 요청 경로에서 액세스 토큰을 추출하지 않음. 요청 URI: {}", requestUri);
            return Optional.empty();
        }
        else if(requestUri.startsWith("/favicon.ico")){
            log.warn("favicon.ico 대해서는 액세스 토큰을 추출하지 않음. 요청 URI: {}", requestUri);
            return Optional.empty();

        }

        // Authorization 헤더에서 값을 추출
        String accessHeaderValue = request.getHeader(accessHeader);
        if (accessHeaderValue == null) {
            log.warn("Authorization 헤더가 없습니다.");
            return Optional.empty();
        }

        if (!accessHeaderValue.startsWith(BEARER)) {
            log.warn("Authorization 헤더가 'Bearer '로 시작하지 않습니다. 해당 값: {}", accessHeaderValue);
            return Optional.empty();
        }

        String accessToken = accessHeaderValue.replace(BEARER, "").trim();
        if (accessToken.isEmpty()) {
            log.warn("추출된 AccessToken이 비어 있습니다.");
            return Optional.empty();
        }

        log.info("추출된 AccessToken: {}", accessToken);
        return Optional.of(accessToken);
    }





    /**
     * JWT verifier 생성 ( AccessToken의 유효성을 검증 )
     * verify 실패 시 JWTVerificationException throw
     * @param token
     * @return
     */
    private DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.HMAC512(secretKey))
                .build()
                .verify(token);
    }

    /**
     * AccessToken에서 Email 관련 cliam 추출
     */
    public Optional<String> extractEmail(String accessToken) {
        log.info("extractEmail() 호출됨");
        String email = verifyToken(accessToken).getClaim(EMAIL_CLAIM).asString();
        log.info("extracted Email : {} " ,email);
        return Optional.ofNullable(email);
    }

    /**
     * AccessToken 의 유효성 여부를 boolean 값으로 반환
     */
    public boolean isTokenValid(String token) {
        log.info("isTokenValid() 호출됨");
        return verifyToken(token) != null ;
    }

    /**
     * AccessToken 헤더 설정
     */
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    /**
     * RefreshToken 헤더 설정
     */
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }

    /**
     * Access Token 재발급
     * RefreshToken 을 활용해 검증 및 RefreshTokne 도 재발급 받고 update 함
     */
    public void reIssueAccessToken(HttpServletResponse response, String refreshToken) {
        log.info("reIssueAccessToken() 호출됨");

        // RefreshToken으로 사용자 조회
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

        // RefreshToken 및 AccessToken 재발급
        String reIssuedRefreshToken = createRefreshToken();
        String reIssuedAccessToken = createAccessToken(user.getEmail());

        // 사용자 엔티티의 RefreshToken 업데이트
        user.updateRefreshToken(reIssuedRefreshToken);

        // sendAccessAndRefreshToken을 통해 새 토큰 반환
        sendAccessAndRefreshToken(response, reIssuedAccessToken, reIssuedRefreshToken);

        log.info("새로운 AccessToken 및 RefreshToken 발급 완료");
        log.info("AccessToken: {}, RefreshToken: {}", reIssuedAccessToken, reIssuedRefreshToken);

    }

    public Optional<User> authenticateAccessToken(String accessToken) {
        return extractEmail(accessToken)
                .flatMap(userRepository::findByEmail);
    }




}
