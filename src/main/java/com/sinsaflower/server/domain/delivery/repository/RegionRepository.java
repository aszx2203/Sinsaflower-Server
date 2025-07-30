package com.sinsaflower.server.domain.delivery.repository;

import com.sinsaflower.server.domain.delivery.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByIsActiveTrue();
    
    List<Region> findBySidoAndIsActiveTrue(String sido);
    
    Optional<Region> findBySidoAndSigungu(String sido, String sigungu);
    
    Optional<Region> findBySidoAndSigunguAndIsActiveTrue(String sido, String sigungu);
    
    List<Region> findByZipcodeStartingWith(String zipcode);
    
    boolean existsBySidoAndSigungu(String sido, String sigungu);
} 