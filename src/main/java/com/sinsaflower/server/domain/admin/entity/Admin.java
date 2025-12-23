package com.sinsaflower.server.domain.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.sinsaflower.server.domain.common.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin", indexes = {
    @Index(name = "idx_admin_login_id", columnList = "loginId"),
    @Index(name = "idx_admin_status", columnList = "status")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String loginId;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private AdminStatus status = AdminStatus.ACTIVE; // 관리자 상태


    private LocalDateTime lastLoginAt; // 마지막 로그인 일시

//    @Enumerated(EnumType.STRING)
//    @Column(name = "role", length = 20, nullable = false)
//    private AdminRole role;

    // 비즈니스 메서드
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == AdminStatus.ACTIVE;
    }

    public void activate() {
        this.status = AdminStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = AdminStatus.INACTIVE;
    }

    // 관리자 상태 enum
    public enum AdminStatus {
        ACTIVE("활성"),
        INACTIVE("비활성"),
        SUSPENDED("정지");

        private final String description;

        AdminStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 