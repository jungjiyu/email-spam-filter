package com.email.spamfilter.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return hierarchy;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                /**
                 * .requestMatchers("/path명") // 딱 그 path 대해 접근 설정
                 * .requestMatchers(HttpMethod.XXXX , "/path명") // 딱 그 path && 그 httpmethod 대해 접근 설정
                 * .requestMatchers("/특정path명/**"") // 특정path의모든하위경로 대해 접근 설정
                 * .requestMatchers("/path명1", "/path명2", "/path명3") // 여러 path 대해 접근 설정
                 *  anyRequest() // 보통 가장 마지막에 명시, 그 이외의 모든 request 대해 적용
                 * .requestMatchers( .... ).permitAll() // 모든 권한 대해 접근 허용
                 * .requestMatchers( .... ).hasRole("권한명") // 특정 권한만 접근 허용
                 */
                .authorizeHttpRequests((auth) -> auth
                        .anyRequest().permitAll());

//        http.oauth2Login(oauth -> oauth
//				.authorizationEndpoint(authorizationEndpointConfig -> authorizationEndpointConfig
//						.authorizationRequestResolver(auth2AuthorizationRequestResolver(clientRegistrationRepository))
//				)
//				.userInfoEndpoint(
//						userInfoEndpointConfig -> userInfoEndpointConfig
//								.userService(oauthUserService)
//				)
//				.successHandler(oauthLoginSuccessHandler())
//		);
//
//                http.addFilterBefore(new JwtAuthorizationFilter(tokenProvider, authenticationManager), UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(e -> e
//                        .authenticationEntryPoint(customAuthenticationEntryPoint)
//                        .accessDeniedHandler(customAccessDeniedHandler))
//        ;

        return http.build();
    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(TokenProvider tokenProvider) {
//        return new ProviderManager(tokenProvider);
//    }
}
