package com.sinsaflower.server.domain.member.repository;

import com.sinsaflower.server.domain.member.entity.MemberBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MemberBankAccountRepository extends JpaRepository<MemberBankAccount, Long> {
    
    // 사업자 프로필 ID로 조회
    List<MemberBankAccount> findByBusinessProfileId(Long businessProfileId);
    
    // 사업자 프로필 ID와 활성 상태로 조회
    List<MemberBankAccount> findByBusinessProfileIdAndIsActiveTrue(Long businessProfileId);
    
    // 기본 계좌 조회
    Optional<MemberBankAccount> findByBusinessProfileIdAndIsPrimaryTrue(Long businessProfileId);
    
    // 계좌번호로 조회
    Optional<MemberBankAccount> findByAccountNumber(String accountNumber);
    
    // 계좌 중복 확인
    boolean existsByAccountNumber(String accountNumber);
    
    // 사업자별 기본 계좌 존재 여부 확인
    boolean existsByBusinessProfileIdAndIsPrimaryTrue(Long businessProfileId);
    
    // 회원 ID로 계좌 조회
    @Query("SELECT mba FROM MemberBankAccount mba " +
           "WHERE mba.businessProfile.member.id = :memberId " +
           "AND mba.isActive = true")
    List<MemberBankAccount> findByMemberId(@Param("memberId") Long memberId);
    
    // 회원 ID로 기본 계좌 조회
    @Query("SELECT mba FROM MemberBankAccount mba " +
           "WHERE mba.businessProfile.member.id = :memberId " +
           "AND mba.isPrimary = true " +
           "AND mba.isActive = true")
    Optional<MemberBankAccount> findPrimaryAccountByMemberId(@Param("memberId") Long memberId);
    
    // 사업자 프로필 ID로 삭제
    void deleteByBusinessProfileId(Long businessProfileId);
    
    // 비활성화된 계좌 조회
    List<MemberBankAccount> findByBusinessProfileIdAndIsActiveFalse(Long businessProfileId);
} 