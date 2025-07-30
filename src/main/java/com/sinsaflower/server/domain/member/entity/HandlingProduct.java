package com.sinsaflower.server.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.sinsaflower.server.domain.common.BaseTimeEntity;

@Entity
@Table(name = "handling_product", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_member_product_type", 
           columnNames = {"member_id", "product_type"}
       ))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandlingProduct extends BaseTimeEntity {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;

    @Column(nullable = false)
    private Boolean isActive = true;
    public enum ProductType {
        FUNERAL("근조"), 
        FRESH_FLOWER("생화"), 
        ORIENTAL_ORCHID("동양란"), 
        WESTERN_ORCHID("서양란"), 
        ARTIFICIAL_FLOWER("조화"), 
        BONSAI("분재");
        
        private final String description;
        
        ProductType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}