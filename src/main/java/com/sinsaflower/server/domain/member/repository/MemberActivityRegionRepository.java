package com.sinsaflower.server.domain.member.repository;

import com.sinsaflower.server.domain.member.entity.MemberActivityRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberActivityRegionRepository extends JpaRepository<MemberActivityRegion, Long> {

    // 회원 ID로 활동 지역 조회
    List<MemberActivityRegion> findByMemberId(Long memberId);
    
    // 활성 활동 지역만 조회
    List<MemberActivityRegion> findByMemberIdAndIsActiveTrue(Long memberId);
    
    // 특정 지역의 활동 회원 조회
    List<MemberActivityRegion> findBySidoAndSigunguAndIsActiveTrue(String sido, String sigungu);
    
    // 시/도별 활동 회원 조회
    List<MemberActivityRegion> findBySidoAndIsActiveTrue(String sido);
    
    // 회원의 특정 지역 활동 여부 확인
    boolean existsByMemberIdAndSidoAndSigunguAndIsActiveTrue(Long memberId, String sido, String sigungu);
    
    // 회원의 활동 지역 개수 조회
    long countByMemberIdAndIsActiveTrue(Long memberId);
    
    // 회원의 모든 활동 지역 삭제 (회원 탈퇴 시)
    void deleteByMemberId(Long memberId);
} 