package com.sinsaflower.server.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@Getter @Setter
@Schema(description = "파트너 회원 가입 요청", example = """
    {
        "loginId": "partner123",
        "password": "password123!",
        "name": "신사꽃농장",
        "nickname": "신사꽃",
        "mobile": "010-1234-5678",
        "activityRegion": {
            "sido": "강원",
            "sigungu": "고성군"
        },
        "businessProfile": {
            "businessNumber": "123-45-67890",
            "corpName": "신사화환 주식회사", 
            "ceoName": "홍길동",
            "businessType": "농업",
            "businessItem": "화훼재배업",
            "fax": "033-123-4567",
            "companyAddress": "강원도 고성군 현내면 123번지",
            "officeAddress": {
                "sido": "강원",
                "sigungu": "고성군", 
                "eupmyeondong": "현내면",
                "detail": "123번지",
                "zipcode": "24708"
            },
            "bankName": "국민은행",
            "accountNumber": "123456-78-901234",
            "accountOwner": "홍길동"
        },
        "regionalPricings": [
            {
                "sido": "강원",
                "sigungu": "춘천시",
                "categoryPrices": [
                    {
                        "categoryName": "축하",
                        "price": 47,
                        "isAvailable": true
                    },
                    {
                        "categoryName": "근조",
                        "price": 47,
                        "isAvailable": true
                    },
                    {
                        "categoryName": "관엽",
                        "price": 50,
                        "isAvailable": true
                    }
                ]
            },
            {
                "sido": "경기도",
                "sigungu": "남양주시", 
                "categoryPrices": [
                    {
                        "categoryName": "축하",
                        "price": 45,
                        "isAvailable": true
                    },
                    {
                        "categoryName": "근조",
                        "price": 45,
                        "isAvailable": false
                    }
                ]
            }
        ]
    }
    """)
public class MemberSignupRequest {
    
    @Schema(description = "로그인 ID", example = "partner123", required = true)
    private String loginId;         // 아이디
    
    @Schema(description = "비밀번호", example = "password123!", required = true)
    private String password;        // 비밀번호
    
    @Schema(description = "상호명", example = "신사꽃농장", required = true)
    private String name;            // 이름(상상호)
    
    @Schema(description = "닉네임", example = "신사꽃", required = true)
    private String nickname;        // 닉네임
    
    @Schema(description = "휴대전화번호", example = "010-1234-5678", required = true)
    private String mobile;          // 휴대전화번호

    // @Schema(description = "이메일", example = "partner@sinsaflower.com")
    // private String email;           // 이메일
    
    @Schema(description = "활동 지역")
    private ActivityRegionRequest activityRegion;
    
    @Schema(description = "사업자 상세 정보", required = true)
    private BusinessProfileRequest businessProfile;
    
    @Schema(description = "지역별 상품 가격 설정 목록")
    private Map<String, RegionalPricingRequest> regionalPricings;
    
    
    @Getter @Setter
    @Schema(description = "지역별 상품 가격 설정 정보")
    public static class RegionalPricingRequest {
        @Schema(description = "시/도", example = "강원", required = true)
        private String sido;
        
        @Schema(description = "시/군/구", example = "춘천시", required = true)
        private String sigungu;
        
        @Schema(description = "해당 지역의 카테고리별 가격 목록", required = true)
        private List<CategoryPriceRequest> categoryPrices;
    }
    
    @Getter @Setter
    @Schema(description = "상품 카테고리별 가격 정보")
    public static class CategoryPriceRequest {
        @Schema(description = "상품 카테고리명", example = "축하", required = true)
        private String categoryName;
        
        @Schema(description = "화환 가격 (천원 단위)", example = "47", required = true)
        private Integer price;
        
        @Schema(description = "취급 가능 여부", example = "true", required = true)
        private Boolean isAvailable;
    }
    
    @Getter @Setter
    @Schema(description = "활동 지역 정보")
    public static class ActivityRegionRequest {
        @Schema(description = "시/도", example = "강원", required = true)
        private String sido;         // 시/도 (강원)
        
        @Schema(description = "시/군/구", example = "고성군", required = true)
        private String sigungu;      // 시/군/구 (고성군, 삼척시 등)
    }
    
    @Getter @Setter
    @Schema(description = "사업자 상세 정보")
    public static class BusinessProfileRequest {
        @Schema(description = "사업자등록번호", example = "123-45-67890", required = true)
        private String businessNumber;
        
        @Schema(description = "법인명", example = "신사화환 주식회사", required = true)
        private String corpName;
        
        @Schema(description = "대표자 성명", example = "홍길동", required = true)
        private String ceoName;
        
        @Schema(description = "업태", example = "농업")
        private String businessType;
        
        @Schema(description = "종목", example = "화훼재배업")
        private String businessItem;
        
        @Schema(description = "팩스번호", example = "033-123-4567")
        private String fax;
        
        @Schema(description = "사업장 주소", required = true)
        private String companyAddress;
        
        @Schema(description = "화환 실제 주소", required = true)
        private AddressRequest officeAddress;
        
        // @Schema(description = "배송 시작 시간", example = "09:00")
        // private String deliveryStartTime;
        
        // @Schema(description = "배송 종료 시간", example = "18:00")
        // private String deliveryEndTime;
        
        // @Schema(description = "야간 배송 가능 여부", example = "false")
        // private Boolean canNightDelivery;
        
        @Schema(description = "은행명", example = "국민은행")
        private String bankName;
        
        @Schema(description = "계좌번호", example = "123456-78-901234")
        private String accountNumber;
        
        @Schema(description = "예금주", example = "홍길동")
        private String accountOwner;
        
        @Schema(description = "사업자등록증 파일")
        private MultipartFile businessCertFile;
        
        @Schema(description = "통장사본 파일")
        private MultipartFile bankCertFile;
    }
    
    @Getter @Setter
    @Schema(description = "주소 정보")
    public static class AddressRequest {
        @Schema(description = "시/도", example = "강원", required = true)
        private String sido;
        
        @Schema(description = "시/군/구", example = "고성군", required = true)
        private String sigungu;
        
        @Schema(description = "읍/면/동", example = "현내면")
        private String eupmyeondong;
        
        @Schema(description = "상세 주소", example = "123번지", required = true)
        private String detail;
        
        @Schema(description = "우편번호", example = "24708", required = true)
        private String zipcode;
    }
} 