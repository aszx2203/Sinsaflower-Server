package com.sinsaflower.server.domain.product.repository;

import com.sinsaflower.server.domain.product.entity.MemberProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberProductPriceRepository extends JpaRepository<MemberProductPrice, Long> {

    // 회원별 가격 정보 조회
    List<MemberProductPrice> findByMemberId(Long memberId);
    
    // 회원 + 지역별 가격 정보 조회
    List<MemberProductPrice> findByMemberIdAndSidoAndSigungu(Long memberId, String sido, String sigungu);
    
    // 회원 + 지역 + 상품 카테고리별 가격 정보 조회
    Optional<MemberProductPrice> findByMemberIdAndSidoAndSigunguAndCategoryName(
        Long memberId, String sido, String sigungu, String categoryName);
    
    // 회원의 취급 가능한 지역별 상품 조회
    List<MemberProductPrice> findByMemberIdAndIsAvailableTrue(Long memberId);
    
    // 특정 지역의 취급 가능한 회원 조회
    List<MemberProductPrice> findBySidoAndSigunguAndIsAvailableTrue(String sido, String sigungu);
    
    // 특정 상품 카테고리의 취급 가능한 회원 조회
    List<MemberProductPrice> findByCategoryNameAndIsAvailableTrue(String categoryName);
    
    // 회원별 가격 매트릭스 조회 (지역 + 상품 카테고리)
    @Query("SELECT mpp FROM MemberProductPrice mpp " +
           "WHERE mpp.member.id = :memberId " +
           "ORDER BY mpp.sido, mpp.sigungu, mpp.categoryName")
    List<MemberProductPrice> findMemberPriceMatrix(@Param("memberId") Long memberId);
    
    // 회원의 모든 가격 정보 삭제 (회원 탈퇴 시)
    void deleteByMemberId(Long memberId);
    
    // 특정 지역의 모든 가격 정보 삭제
    void deleteBySidoAndSigungu(String sido, String sigungu);
    
    // 특정 상품 카테고리의 모든 가격 정보 삭제
    void deleteByCategoryName(String categoryName);
    
    // 회원 + 지역 + 상품 카테고리 조합 존재 여부 확인
    boolean existsByMemberIdAndSidoAndSigunguAndCategoryName(
        Long memberId, String sido, String sigungu, String categoryName);
    
    // 시도별 회원 조회
    List<MemberProductPrice> findBySidoAndIsAvailableTrue(String sido);
    
    // 회원의 특정 지역 가격 정보 조회
    List<MemberProductPrice> findByMemberIdAndSido(Long memberId, String sido);
    
    // 회원의 특정 카테고리 가격 정보 조회
    List<MemberProductPrice> findByMemberIdAndCategoryName(Long memberId, String categoryName);
    
    // 특정 지역의 특정 카테고리 회원 조회
    List<MemberProductPrice> findBySidoAndSigunguAndCategoryNameAndIsAvailableTrue(
        String sido, String sigungu, String categoryName);
} 