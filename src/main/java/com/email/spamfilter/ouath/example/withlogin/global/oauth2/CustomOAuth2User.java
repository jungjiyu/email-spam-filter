package com.email.spamfilter.ouath.example.withlogin.global.oauth2;

import com.email.spamfilter.ouath.example.withlogin.domain.user.Role;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;



/**
 * 리소스서버에게 받아온 정보 +α 를 담을 DTO
 */
@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    // 리소스 서버로부터 받은 email 정보를 저장하기 위함
    private String email;
    // (리소스 서버에서의 로그인 성공 이후) 회원가입로직 수행 필요 여부를 판별하기 위함
    private Role role;
    // 리소스 서버로부터 받은 email 정보를 저장하기 위함
    private String nickName;


    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes, String nameAttributeKey,
                            String email, Role role, String nickName) {
        super(authorities, attributes, nameAttributeKey);
        this.email = email;
        this.role = role;
        this.nickName=nickName;
    }


}
