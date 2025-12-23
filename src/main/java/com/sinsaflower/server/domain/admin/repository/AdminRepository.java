package com.sinsaflower.server.domain.admin.repository;

import com.sinsaflower.server.domain.admin.entity.Admin;
import com.sinsaflower.server.domain.admin.entity.Admin.AdminStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 기본 조회
    Optional<Admin> findByLoginId(String loginId);
    
    // 활성 관리자만 조회
    Optional<Admin> findByLoginIdAndStatus(String loginId, AdminStatus status);
    
    // 중복 확인
    boolean existsByLoginId(String loginId);
    
    // 상태별 조회
    List<Admin> findByStatus(AdminStatus status);
    
    Page<Admin> findByStatus(AdminStatus status, Pageable pageable);
    
    // 삭제되지 않은 관리자만 조회
    @Query("SELECT a FROM Admin a WHERE a.isDeleted = false ORDER BY a.createdAt DESC")
    List<Admin> findAllActive();
    
    @Query("SELECT a FROM Admin a WHERE a.isDeleted = false ORDER BY a.createdAt DESC")
    Page<Admin> findAllActive(Pageable pageable);
    
    // 이름으로 검색 (삭제되지 않은 것만)
    @Query("SELECT a FROM Admin a WHERE a.name LIKE %:name% AND a.isDeleted = false ORDER BY a.createdAt DESC")
    List<Admin> findByNameContaining(@Param("name") String name);
    
    // 로그인 ID로 검색 (삭제되지 않은 것만)
    @Query("SELECT a FROM Admin a WHERE a.loginId LIKE %:loginId% AND a.isDeleted = false ORDER BY a.createdAt DESC")
    List<Admin> findByLoginIdContaining(@Param("loginId") String loginId);
    
    // 통계용 쿼리
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.status = :status AND a.isDeleted = false")
    long countByStatus(@Param("status") AdminStatus status);
    
} 