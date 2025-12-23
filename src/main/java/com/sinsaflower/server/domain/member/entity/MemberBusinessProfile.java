package com.sinsaflower.server.domain.member.entity;

import com.sinsaflower.server.domain.common.Address;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member_business_profile", indexes = {
    @Index(name = "idx_business_number", columnList = "businessNumber")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberBusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // memberBusinessProfileId

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member; // 회원


    private String companyAddress; // 회사 주소

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "sido", column = @Column(name = "office_sido")),
        @AttributeOverride(name = "sigungu", column = @Column(name = "office_sigungu")),
        @AttributeOverride(name = "eupmyeondong", column = @Column(name = "office_eupmyeondong")),
        @AttributeOverride(name = "detail", column = @Column(name = "office_detail")),
        @AttributeOverride(name = "zipcode", column = @Column(name = "office_zipcode"))
    })
    private Address officeAddress; // 회사 주소

    @Column(length = 20)
    private String fax; // 팩스번호

    // 사업자 정보
    @Column(length = 12, nullable = false, unique = true)
    private String businessNumber; // 사업자등록번호

    @Column(length = 100, nullable = false)
    private String corpName; // 법인명

    @Column(length = 50, nullable = false)
    private String ceoName; // 대표자명

    @Column(length = 100)
    private String businessType; // 업태

    @Column(length = 100)
    private String businessItem; // 종목

    @Column(length = 500)
    private String businessCertFilePath; // 사업자등록증 경로

    @Column(length = 2000)
    private String memo; // memo

    // 계좌 정보는 별도 엔티티로 분리
    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MemberBankAccount> bankAccounts = new ArrayList<>(); // 계좌 정보

    //     // 배송 설정
    @Column(nullable = true)
    private Boolean autoProductRegister = false; // 자동 상품 등록 여부

    @Column(nullable = true)
    private Boolean canNightDelivery = false; // 야간 배송 가능 여부

    private LocalTime deliveryStartTime; // 배송 시작 시간
    private LocalTime deliveryEndTime; // 배송 종료 시간

    // 승인 정보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING; // 승인 상태

    private LocalDateTime approvedAt; // 승인 일시
    private String approvedBy; // 승인자
    private String rejectionReason; // 거절 사유

    // 비즈니스 메서드
    public void approve(String approver) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approver;
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.rejectionReason = reason;
        this.approvedAt = null;
        this.approvedBy = null;
    }

    public boolean isApproved() {
        return approvalStatus == ApprovalStatus.APPROVED;
    }

    public enum ApprovalStatus {
        PENDING("승인대기"), APPROVED("승인완료"), REJECTED("승인거부");
        
        private final String description;
        
        ApprovalStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 