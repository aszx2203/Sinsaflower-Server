package com.sinsaflower.server.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_activity_region")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberActivityRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // memberActivityRegionId

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 회원

    @Column(length = 50, nullable = false)
    private String sido; // 시/도

    @Column(length = 50, nullable = false)
    private String sigungu; // 시/군/구

    // @Column(nullable = true)
    // private Boolean isPrimary = false;

    @Column(nullable = true)
    private Boolean isActive = true; // 활성 여부
} 