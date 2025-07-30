package com.sinsaflower.server.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sinsaflower.server.domain.common.BaseTimeEntity;
import com.sinsaflower.server.domain.product.entity.MemberProductPrice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member", indexes = {
    @Index(name = "idx_login_id", columnList = "loginId"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // memberId

    @Column(length = 50, nullable = false, unique = true)
    private String loginId; // 로그인 아이디

    @Column(length = 255, nullable = false)
    private String password; // 비밀번호    

    @Column(length = 100, nullable = false)
    private String name; // 화환명

    @Column(length = 50, nullable = false)
    private String nickname; // 닉네임

    @Column(length = 20, nullable = false)
    private String mobile; // 휴대전화번호

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private MemberStatus status = MemberStatus.PENDING; // 회원 상태

    // 관계 매핑
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MemberBusinessProfile businessProfile; // 사업자 프로필

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private NotificationSetting notificationSetting; // 알림 설정

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<HandlingProduct> handlingProducts = new ArrayList<>(); // 처리 상품

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MemberActivityRegion> activityRegions = new ArrayList<>(); // 활동 지역

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MemberProductPrice> productPrices = new ArrayList<>(); // 상품 가격

    private LocalDateTime lastLoginAt; // 마지막 로그인 일시

    // 비즈니스 메서드
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
    
    public enum MemberStatus {
        PENDING("승인대기"), ACTIVE("활성"), SUSPENDED("정지"), DELETED("삭제");
        
        private final String description;
        
        MemberStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}