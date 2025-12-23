package com.sinsaflower.server.global.security;

import com.sinsaflower.server.domain.admin.entity.Admin;
import com.sinsaflower.server.domain.member.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 관리자와 파트너 모두를 처리할 수 있는 통합 UserDetails 구현체
 */
@Getter
public class CustomUserDetails implements UserDetails {
    
    // 사용자 타입 상수
    public static final String USER_TYPE_ADMIN = "ADMIN";
    public static final String USER_TYPE_PARTNER = "PARTNER";
    
    // 권한 상수
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_PARTNER = "ROLE_PARTNER";
    
    private final Long userId;
    private final String username;
    private final String password;
    private final String name;
    private final String nickname;
    private final String userType;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;
    
    // 관리자 생성자
    public CustomUserDetails(Admin admin) {
        this.userId = admin.getId();
        this.username = admin.getLoginId();
        this.password = admin.getPassword();
        this.name = admin.getName();
        this.nickname = admin.getName(); // 관리자는 별도 닉네임이 없으므로 name을 사용
        this.userType = USER_TYPE_ADMIN;
        this.enabled = true; // 관리자는 항상 활성상태
        this.authorities = List.of(new SimpleGrantedAuthority(ROLE_ADMIN));
    }
    
    // 파트너 생성자
    public CustomUserDetails(Member member) {
        this.userId = member.getId();
        this.username = member.getLoginId();
        this.password = member.getPassword();
        this.name = member.getName();
        this.nickname = member.getNickname();
        this.userType = USER_TYPE_PARTNER;
        this.enabled = Member.MemberStatus.ACTIVE.equals(member.getStatus());
        this.authorities = List.of(new SimpleGrantedAuthority(ROLE_PARTNER));
    }
    
    // JWT 토큰을 위한 생성자
    public CustomUserDetails(Long userId, String username, String userType,
                             Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = ""; // JWT 인증에서는 비밀번호 불필요
        this.name = ""; // 기존 로직 호환성을 위해 name은 비워둠
        this.nickname = ""; // 기존 로직 호환성을 위해 nickname은 비워둠
        this.userType = userType;
        this.enabled = true;
        this.authorities = authorities;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return USER_TYPE_ADMIN.equals(userType);
    }
    
    /**
     * 파트너 여부 확인
     */
    public boolean isPartner() {
        return USER_TYPE_PARTNER.equals(userType);
    }
    
    /**
     * 특정 권한 보유 여부 확인
     */
    public boolean hasAuthority(String authority) {
        return authorities.stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
    
    /**
     * 관리자 권한 보유 여부 확인
     */
    public boolean hasAdminRole() {
        return hasAuthority(ROLE_ADMIN);
    }
    
    /**
     * 파트너 권한 보유 여부 확인
     */
    public boolean hasPartnerRole() {
        return hasAuthority(ROLE_PARTNER);
    }
} 