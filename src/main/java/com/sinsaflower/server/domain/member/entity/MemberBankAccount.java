package com.sinsaflower.server.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_bank_account")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // memberBankAccountId

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_profile_id", nullable = false)
    private MemberBusinessProfile businessProfile; // 사업자 프로필

    @Column(length = 50, nullable = false)
    private String bankName; // 은행명

    @Column(length = 50, nullable = false)
    private String accountNumber; // 계좌번호

    @Column(length = 50, nullable = false)
    private String accountOwner; // 예금주

    @Column(length = 500)
    private String bankCertFilePath; // 은행 인증서 경로

    @Column(nullable = false)
    private Boolean isPrimary = false; // 기본 계좌 여부

    @Column(nullable = false)
    private Boolean isActive = true; // 활성 여부

} 