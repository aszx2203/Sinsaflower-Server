package com.sinsaflower.server.domain.admin.repository;

import com.sinsaflower.server.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByLoginId(String loginId);
    
    boolean existsByLoginId(String loginId);
    
} 