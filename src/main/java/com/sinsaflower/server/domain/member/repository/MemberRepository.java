package com.sinsaflower.server.domain.member.repository;

import com.sinsaflower.server.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    List<Member> findByStatus(Member.MemberStatus status);
    
    // 이름으로 검색
    List<Member> findByNameContaining(String name);
    
    // 닉네임으로 검색
    List<Member> findByNicknameContaining(String nickname);
    
    // 로그인 ID와 상태로 조회
    Optional<Member> findByLoginIdAndStatus(String loginId, Member.MemberStatus status);
    
    // 승인된 파트너 회원 조회
    @Query("SELECT m FROM Member m JOIN m.businessProfile mbp WHERE mbp.approvalStatus = 'APPROVED'")
    List<Member> findApprovedPartners();
    
    // 승인 대기 중인 파트너 회원 조회
    @Query("SELECT m FROM Member m JOIN m.businessProfile mbp WHERE mbp.approvalStatus = 'PENDING' ORDER BY mbp.createdAt ASC")
    List<Member> findPendingPartners();
    
    // 최근 가입 회원 조회 (관리자용)
    List<Member> findTop10ByOrderByCreatedAtDesc();
    
    // 활성 회원 조회
    List<Member> findByStatusOrderByCreatedAtDesc(Member.MemberStatus status);
    
    // 사업자등록번호로 중복 확인 (파트너 회원 전용)
    @Query("SELECT COUNT(m) > 0 FROM Member m JOIN m.businessProfile mbp WHERE mbp.businessNumber = :businessNumber")
    boolean existsByBusinessNumber(@Param("businessNumber") String businessNumber);
} 