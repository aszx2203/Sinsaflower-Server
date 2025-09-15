package com.sinsaflower.server.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sinsaflower.server.domain.common.BaseTimeEntity;
import com.sinsaflower.server.domain.member.constants.MemberConstants;
import com.sinsaflower.server.domain.product.entity.MemberProductPrice;
import com.sinsaflower.server.global.exception.InvalidRequestException;

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

    public boolean isPending() {
        return status == MemberStatus.PENDING;
    }

    public boolean isSuspended() {
        return status == MemberStatus.SUSPENDED;
    }

    public boolean isDeleted() {
        return status == MemberStatus.DELETED;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 회원 상태 변경 (비즈니스 규칙 검증 포함)
     */
    public void updateStatus(MemberStatus newStatus) {
        if (!isValidStatusTransition(this.status, newStatus)) {
            throw new InvalidRequestException(MemberConstants.Messages.INVALID_STATUS_TRANSITION);
        }
        this.status = newStatus;
    }

    /**
     * 회원 승인
     */
    public void approve() {
        if (this.status != MemberStatus.PENDING) {
            throw new InvalidRequestException("승인 대기 상태의 회원만 승인할 수 있습니다.");
        }
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 회원 정지
     */
    public void suspend() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new InvalidRequestException("활성 상태의 회원만 정지할 수 있습니다.");
        }
        this.status = MemberStatus.SUSPENDED;
    }

    /**
     * 회원 정지 해제
     */
    public void unsuspend() {
        if (this.status != MemberStatus.SUSPENDED) {
            throw new InvalidRequestException("정지 상태의 회원만 정지 해제할 수 있습니다.");
        }
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 비밀번호 변경 (현재 비밀번호 검증 포함)
     */
    public void changePassword(String currentPassword, String newPassword, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(currentPassword, this.password)) {
            throw new InvalidRequestException(MemberConstants.Messages.INVALID_PASSWORD);
        }
        this.password = passwordEncoder.encode(newPassword);
    }

    /**
     * 회원 정보 유효성 검증
     */
    public void validateMemberInfo() {
        if (loginId == null || loginId.length() < MemberConstants.Validation.MIN_LOGIN_ID_LENGTH 
            || loginId.length() > MemberConstants.Validation.MAX_LOGIN_ID_LENGTH) {
            throw new InvalidRequestException(MemberConstants.Messages.INVALID_LOGIN_ID_LENGTH);
        }
        
        if (name == null || name.length() < MemberConstants.Validation.MIN_NAME_LENGTH 
            || name.length() > MemberConstants.Validation.MAX_NAME_LENGTH) {
            throw new InvalidRequestException(MemberConstants.Messages.INVALID_NAME_LENGTH);
        }
        
        if (mobile == null || mobile.length() != MemberConstants.Validation.MOBILE_LENGTH) {
            throw new InvalidRequestException(MemberConstants.Messages.INVALID_MOBILE_FORMAT);
        }
    }

    /**
     * 로그인 가능 여부 확인
     */
    public boolean canLogin() {
        return isActive() && !getIsDeleted();
    }

    /**
     * 수정 가능 여부 확인
     */
    public boolean canBeModified() {
        return (isActive() || isPending()) && !getIsDeleted();
    }

    // 소프트 삭제 처리
    public void softDelete(String deletedBy) {
        this.status = MemberStatus.DELETED;
        super.softDelete(deletedBy);
    }

    /**
     * 상태 변경 유효성 검증
     */
    private boolean isValidStatusTransition(MemberStatus currentStatus, MemberStatus newStatus) {
        if (currentStatus == newStatus) {
            return false; // 동일한 상태로 변경 불가
        }
        
        return switch (currentStatus) {
            case PENDING -> newStatus == MemberStatus.ACTIVE || newStatus == MemberStatus.DELETED;
            case ACTIVE -> newStatus == MemberStatus.SUSPENDED || newStatus == MemberStatus.DELETED;
            case SUSPENDED -> newStatus == MemberStatus.ACTIVE || newStatus == MemberStatus.DELETED;
            case DELETED -> false; // 삭제된 회원은 상태 변경 불가
        };
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