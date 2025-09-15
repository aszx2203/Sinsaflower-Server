package com.sinsaflower.server.domain.member.repository;

import com.sinsaflower.server.domain.member.entity.MemberBusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MemberBusinessProfileRepository extends JpaRepository<MemberBusinessProfile, Long> {
    
    // 회원 ID로 조회
    Optional<MemberBusinessProfile> findByMemberId(Long memberId);
    
    // 사업자등록번호로 조회
    Optional<MemberBusinessProfile> findByBusinessNumber(String businessNumber);
    
    // 사업자등록번호 중복 확인
    boolean existsByBusinessNumber(String businessNumber);
    
    // 승인 상태로 조회
    List<MemberBusinessProfile> findByApprovalStatus(MemberBusinessProfile.ApprovalStatus approvalStatus);
    
    // TODO: 필요시 다시 추가
    // List<MemberBusinessProfile> findByApprovalStatusOrderByCreatedAtAsc(MemberBusinessProfile.ApprovalStatus approvalStatus);
    
    // 지역별 사업자 조회 (사업장 주소 기준)
    @Query("SELECT mbp FROM MemberBusinessProfile mbp " +
           "WHERE mbp.officeAddress.sido = :sido " +
           "AND mbp.officeAddress.sigungu = :sigungu " +
           "AND mbp.approvalStatus = :approvalStatus")
    List<MemberBusinessProfile> findByOfficeAddressRegion(@Param("sido") String sido, @Param("sigungu") String sigungu, @Param("approvalStatus") MemberBusinessProfile.ApprovalStatus approvalStatus);
    
    // 법인명으로 검색
    List<MemberBusinessProfile> findByCorpNameContainingIgnoreCase(String corpName);
    
    // 대표자명으로 검색
    List<MemberBusinessProfile> findByCeoNameContainingIgnoreCase(String ceoName);
    
    // 야간 배송 가능한 사업자 조회
    List<MemberBusinessProfile> findByCanNightDeliveryTrueAndApprovalStatus(MemberBusinessProfile.ApprovalStatus approvalStatus);
    
    // 회원 ID로 삭제
    void deleteByMemberId(Long memberId);
} 