package com.sinsaflower.server.domain.member.repository;

import com.sinsaflower.server.domain.member.entity.Member;
import com.sinsaflower.server.domain.member.entity.Member.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인 ID로 회원 조회
    Optional<Member> findByLoginId(String loginId);
    
    // 로그인 ID 중복 확인
    boolean existsByLoginId(String loginId);
    
    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);
    
    
    // 전화번호 중복 확인
    boolean existsByMobile(String mobile);
    
    // 개별 필드로 회원 조회
    Optional<Member> findByNickname(String nickname);
    Optional<Member> findByMobile(String mobile);
    
    // 회원 상태별 조회
    List<Member> findByStatus(MemberStatus status);
    Page<Member> findByStatus(MemberStatus status, Pageable pageable);
    
    // 로그인 ID와 상태로 조회
    Optional<Member> findByLoginIdAndStatus(String loginId, MemberStatus status);
    
    // 삭제되지 않은 회원만 조회
    @Query("SELECT DISTINCT m FROM Member m LEFT JOIN FETCH m.businessProfile bp LEFT JOIN FETCH bp.bankAccounts WHERE m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Member> findAllActive();
    
    @Query(value = "SELECT DISTINCT m FROM Member m LEFT JOIN FETCH m.businessProfile bp LEFT JOIN FETCH bp.bankAccounts WHERE m.isDeleted = false ORDER BY m.createdAt DESC",
           countQuery = "SELECT count(m) FROM Member m WHERE m.isDeleted = false")
    Page<Member> findAllActive(Pageable pageable);
    
    // 활성 회원 중 로그인 ID로 조회
    @Query("SELECT m FROM Member m WHERE m.loginId = :loginId AND m.isDeleted = false")
    Optional<Member> findActiveByLoginId(@Param("loginId") String loginId);
    
    // 이름으로 검색 (삭제되지 않은 것만)
    @Query("SELECT m FROM Member m WHERE m.name LIKE %:name% AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Member> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT m FROM Member m WHERE m.name LIKE %:name% AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Member> findByNameContaining(@Param("name") String name, Pageable pageable);
    
    // 닉네임으로 검색 (삭제되지 않은 것만)
    @Query("SELECT m FROM Member m WHERE m.nickname LIKE %:nickname% AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Member> findByNicknameContaining(@Param("nickname") String nickname);
    
    // 로그인 ID로 검색 (삭제되지 않은 것만)
    @Query("SELECT m FROM Member m WHERE m.loginId LIKE %:loginId% AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Member> findByLoginIdContaining(@Param("loginId") String loginId);
    
    // 상태별 + 검색 조합
    @Query("SELECT m FROM Member m WHERE m.status = :status AND m.name LIKE %:name% AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Member> findByStatusAndNameContaining(@Param("status") MemberStatus status, @Param("name") String name, Pageable pageable);
    
    @Query("SELECT m FROM Member m WHERE m.status = :status AND m.loginId LIKE %:loginId% AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Member> findByStatusAndLoginIdContaining(@Param("status") MemberStatus status, @Param("loginId") String loginId, Pageable pageable);
    
    // 최근 가입 회원 조회
    @Query("SELECT m FROM Member m WHERE m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Member> findTop10ByOrderByCreatedAtDesc(Pageable pageable);
    
    // 특정 기간 가입 회원 조회
    @Query("SELECT m FROM Member m WHERE m.createdAt BETWEEN :startDate AND :endDate AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Member> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT m FROM Member m WHERE m.createdAt BETWEEN :startDate AND :endDate AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Member> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    // 지역별 회원 검색 (활동 지역 기반)
    @Query("SELECT DISTINCT m FROM Member m " +
           "JOIN m.activityRegions ar " +
           "WHERE ar.sido = :sido AND m.status = 'ACTIVE' AND m.isDeleted = false " +
           "ORDER BY m.createdAt DESC")
    Page<Member> findByActivityRegionSido(@Param("sido") String sido, Pageable pageable);
    
    @Query("SELECT DISTINCT m FROM Member m " +
           "JOIN m.activityRegions ar " +
           "WHERE ar.sido = :sido AND ar.sigungu = :sigungu AND m.status = 'ACTIVE' AND m.isDeleted = false " +
           "ORDER BY m.createdAt DESC")
    Page<Member> findByActivityRegionSidoAndSigungu(@Param("sido") String sido, @Param("sigungu") String sigungu, Pageable pageable);
    
    // 취급 상품별 회원 검색
    @Query("SELECT DISTINCT m FROM Member m " +
           "JOIN m.handlingProducts hp " +
           "WHERE CAST(hp.productType AS string) LIKE %:productName% AND m.status = 'ACTIVE' AND m.isDeleted = false " +
           "ORDER BY m.createdAt DESC")
    Page<Member> findByHandlingProductName(@Param("productName") String productName, Pageable pageable);
    
    // 복합 검색 쿼리들
    @Query("SELECT DISTINCT m FROM Member m " +
           "LEFT JOIN m.activityRegions ar " +
           "LEFT JOIN m.handlingProducts hp " +
           "WHERE (:name IS NULL OR m.name LIKE %:name%) " +
           "AND (:sido IS NULL OR ar.sido = :sido) " +
           "AND (:sigungu IS NULL OR ar.sigungu = :sigungu) " +
           "AND (:productName IS NULL OR CAST(hp.productType AS string) LIKE %:productName%) " +
           "AND m.status = 'ACTIVE' AND m.isDeleted = false " +
           "ORDER BY m.createdAt DESC")
    Page<Member> findByCombinedSearch(
        @Param("name") String name,
        @Param("sido") String sido,
        @Param("sigungu") String sigungu,
        @Param("productName") String productName,
        Pageable pageable);
    
    // 지역별 회원 수 통계
    @Query("SELECT ar.sido, COUNT(DISTINCT m) FROM Member m " +
           "JOIN m.activityRegions ar " +
           "WHERE m.status = 'ACTIVE' AND m.isDeleted = false " +
           "GROUP BY ar.sido " +
           "ORDER BY COUNT(DISTINCT m) DESC")
    List<Object[]> countMembersByRegion();
    
    // 취급 상품별 회원 수 통계  
    @Query("SELECT hp.productType, COUNT(DISTINCT m) FROM Member m " +
           "JOIN m.handlingProducts hp " +
           "WHERE m.status = 'ACTIVE' AND m.isDeleted = false " +
           "GROUP BY hp.productType " +
           "ORDER BY COUNT(DISTINCT m) DESC")
    List<Object[]> countMembersByProduct();
    
    // 기본 통계용 쿼리
    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = :status AND m.isDeleted = false")
    long countByStatus(@Param("status") MemberStatus status);
    
    // 사업자등록번호로 중복 확인 (파트너 회원 전용)
    @Query("SELECT COUNT(m) > 0 FROM Member m LEFT JOIN m.businessProfile mbp WHERE mbp.businessNumber = :businessNumber")
    boolean existsByBusinessNumber(@Param("businessNumber") String businessNumber);
} 