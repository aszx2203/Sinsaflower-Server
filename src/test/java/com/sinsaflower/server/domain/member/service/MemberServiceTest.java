package com.sinsaflower.server.domain.member.service;

import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.dto.MemberSignupRequest;
import com.sinsaflower.server.domain.member.entity.*;
import com.sinsaflower.server.domain.member.repository.*;
import com.sinsaflower.server.domain.product.entity.MemberProductPrice;
import com.sinsaflower.server.domain.product.repository.MemberProductPriceRepository;
import com.sinsaflower.server.global.service.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위 테스트")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private MemberBusinessProfileRepository businessProfileRepository;
    
    @Mock
    private MemberBankAccountRepository bankAccountRepository;
    
    @Mock
    private MemberProductPriceRepository productPriceRepository;
    
    @Mock
    private MemberActivityRegionRepository activityRegionRepository;
    
    @Mock
    private NotificationSettingRepository notificationSettingRepository;
    
    @Mock
    private FileUploadService fileUploadService;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    private MemberSignupRequest validSignupRequest;
    private Member savedMember;
    private MemberBusinessProfile savedBusinessProfile;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        validSignupRequest = createValidSignupRequest();
        savedMember = createSavedMember();
        savedBusinessProfile = createSavedBusinessProfile();
    }

    @Test
    @DisplayName("회원 가입 성공 테스트 - 7단계 프로세스 검증")
    void signUp_Success() throws IOException {
        // given
        given(memberRepository.existsByLoginId(validSignupRequest.getLoginId())).willReturn(false);
        given(businessProfileRepository.existsByBusinessNumber(
            validSignupRequest.getBusinessProfile().getBusinessNumber())).willReturn(false);
        given(passwordEncoder.encode(validSignupRequest.getPassword())).willReturn("encoded_password");
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(businessProfileRepository.save(any(MemberBusinessProfile.class))).willReturn(savedBusinessProfile);
        given(fileUploadService.saveFile(any(MultipartFile.class), eq("business-cert")))
            .willReturn("/uploads/business-cert/test-file.pdf");

        // when
        MemberResponse response = memberService.signUp(validSignupRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getLoginId()).isEqualTo(validSignupRequest.getLoginId());
        assertThat(response.getName()).isEqualTo(validSignupRequest.getName());
        assertThat(response.getStatus()).isEqualTo(Member.MemberStatus.PENDING.getDescription());

        // verify 7단계 프로세스
        then(memberRepository).should().existsByLoginId(validSignupRequest.getLoginId());
        then(businessProfileRepository).should().existsByBusinessNumber(
            validSignupRequest.getBusinessProfile().getBusinessNumber());
        then(memberRepository).should().save(any(Member.class));
        then(businessProfileRepository).should().save(any(MemberBusinessProfile.class));
        then(bankAccountRepository).should().save(any(MemberBankAccount.class));
        then(activityRegionRepository).should().saveAll(anyList());
        then(productPriceRepository).should().saveAll(anyList());
        then(notificationSettingRepository).should().save(any(NotificationSetting.class));
    }

    @Test
    @DisplayName("중복 로그인 ID로 회원 가입 실패 테스트")
    void signUp_DuplicateLoginId_ThrowsException() {
        // given
        given(memberRepository.existsByLoginId(validSignupRequest.getLoginId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.signUp(validSignupRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 로그인 ID입니다");

        // verify - 중복 체크 후 더 이상 진행하지 않음
        then(memberRepository).should().existsByLoginId(validSignupRequest.getLoginId());
        then(memberRepository).should(never()).save(any(Member.class));
    }

    @Test
    @DisplayName("중복 사업자등록번호로 회원 가입 실패 테스트")
    void signUp_DuplicateBusinessNumber_ThrowsException() {
        // given
        given(memberRepository.existsByLoginId(validSignupRequest.getLoginId())).willReturn(false);
        given(businessProfileRepository.existsByBusinessNumber(
            validSignupRequest.getBusinessProfile().getBusinessNumber())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.signUp(validSignupRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 등록된 사업자등록번호입니다");

        then(memberRepository).should().existsByLoginId(validSignupRequest.getLoginId());
        then(businessProfileRepository).should().existsByBusinessNumber(
            validSignupRequest.getBusinessProfile().getBusinessNumber());
        then(memberRepository).should(never()).save(any(Member.class));
    }

    @Test
    @DisplayName("파일 업로드 실패 시 예외 발생 테스트")
    void signUp_FileUploadFailure_ThrowsException() throws IOException {
        // given
        given(memberRepository.existsByLoginId(validSignupRequest.getLoginId())).willReturn(false);
        given(businessProfileRepository.existsByBusinessNumber(
            validSignupRequest.getBusinessProfile().getBusinessNumber())).willReturn(false);
        given(passwordEncoder.encode(validSignupRequest.getPassword())).willReturn("encoded_password");
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(fileUploadService.saveFile(any(MultipartFile.class), eq("business-cert")))
            .willThrow(new RuntimeException("파일 업로드 실패"));

        // when & then
        assertThatThrownBy(() -> memberService.signUp(validSignupRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 업로드 중 오류가 발생했습니다");

        then(memberRepository).should().save(any(Member.class));
        then(fileUploadService).should().saveFile(any(MultipartFile.class), eq("business-cert"));
    }

    @Test
    @DisplayName("회원 정보 조회 성공 테스트")
    void getMemberInfo_Success() {
        // given
        Long memberId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.of(savedMember));

        // when
        MemberResponse response = memberService.getMemberInfo(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(savedMember.getId());
        assertThat(response.getLoginId()).isEqualTo(savedMember.getLoginId());
        assertThat(response.getName()).isEqualTo(savedMember.getName());

        then(memberRepository).should().findById(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 실패 테스트")
    void getMemberInfo_NotFound_ThrowsException() {
        // given
        Long memberId = 999L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMemberInfo(memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다");

        then(memberRepository).should().findById(memberId);
    }

    @Test
    @DisplayName("로그인 ID로 회원 조회 성공 테스트")
    void getMemberByLoginId_Success() {
        // given
        String loginId = "testuser";
        given(memberRepository.findByLoginId(loginId)).willReturn(Optional.of(savedMember));

        // when
        MemberResponse response = memberService.getMemberByLoginId(loginId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getLoginId()).isEqualTo(loginId);

        then(memberRepository).should().findByLoginId(loginId);
    }

    @Test
    @DisplayName("로그인 ID 중복 확인 테스트")
    void isLoginIdDuplicate_ReturnsTrue() {
        // given
        String loginId = "testuser";
        given(memberRepository.existsByLoginId(loginId)).willReturn(true);

        // when
        boolean isDuplicate = memberService.isLoginIdDuplicate(loginId);

        // then
        assertThat(isDuplicate).isTrue();
        then(memberRepository).should().existsByLoginId(loginId);
    }

    @Test
    @DisplayName("사업자등록번호 중복 확인 테스트")
    void isBusinessNumberDuplicate_ReturnsTrue() {
        // given
        String businessNumber = "123-45-67890";
        given(businessProfileRepository.existsByBusinessNumber(businessNumber)).willReturn(true);

        // when
        boolean isDuplicate = memberService.isBusinessNumberDuplicate(businessNumber);

        // then
        assertThat(isDuplicate).isTrue();
        then(businessProfileRepository).should().existsByBusinessNumber(businessNumber);
    }

    // === 테스트 데이터 생성 헬퍼 메서드들 ===

    private MemberSignupRequest createValidSignupRequest() {
        MemberSignupRequest request = new MemberSignupRequest();
        request.setLoginId("testuser");
        request.setPassword("password123");
        request.setName("테스트화환");
        request.setNickname("테스트닉네임");
        request.setMobile("010-1234-5678");

        // 사업자 프로필 정보
        MemberSignupRequest.BusinessProfileRequest businessProfile = new MemberSignupRequest.BusinessProfileRequest();
        businessProfile.setBusinessNumber("123-45-67890");
        businessProfile.setCorpName("테스트화환 주식회사");
        businessProfile.setCeoName("홍길동");
        businessProfile.setBusinessType("농업");
        businessProfile.setBusinessItem("화훼재배업");
        businessProfile.setBankName("국민은행");
        businessProfile.setAccountNumber("123456-78-901234");
        businessProfile.setAccountOwner("홍길동");

        // 주소 정보
        String companyAddress = "강원 춘천시 456번지 24708";

        MemberSignupRequest.AddressRequest officeAddress = new MemberSignupRequest.AddressRequest();
        officeAddress.setSido("강원");
        officeAddress.setSigungu("춘천시");
        officeAddress.setDetail("456번지");
        officeAddress.setZipcode("24708");

        // 파일 (Mock)
        businessProfile.setBusinessCertFile(mock(MultipartFile.class));
        businessProfile.setBankCertFile(mock(MultipartFile.class));

        request.setBusinessProfile(businessProfile);

        // 활동 지역
        MemberSignupRequest.ActivityRegionRequest activityRegion = new MemberSignupRequest.ActivityRegionRequest();
        activityRegion.setSido("강원");
        activityRegion.setSigungu("춘천시");
        request.setActivityRegion(activityRegion);

        return request;
    }

    private Member createSavedMember() {
        return Member.builder()
                .id(1L)
                .loginId("testuser")
                .password("encoded_password")
                .name("테스트화환")
                .nickname("테스트닉네임")
                .mobile("010-1234-5678")
                .status(Member.MemberStatus.PENDING)
                .build();
    }

    private MemberBusinessProfile createSavedBusinessProfile() {
        return MemberBusinessProfile.builder()
                .id(1L)
                .member(savedMember)
                .businessNumber("123-45-67890")
                .corpName("테스트화환 주식회사")
                .ceoName("홍길동")
                .approvalStatus(MemberBusinessProfile.ApprovalStatus.PENDING)
                .build();
    }

    @Test 
    @DisplayName("새로운 계층적 구조(RegionalPricingRequest) 처리 테스트")
    void createProductPrices_WithHierarchicalStructure() {
        // given
        MemberSignupRequest request = createHierarchicalSignupRequest();
        
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(businessProfileRepository.save(any(MemberBusinessProfile.class))).willReturn(savedBusinessProfile);
        given(activityRegionRepository.save(any(MemberActivityRegion.class))).willReturn(mock(MemberActivityRegion.class));
        given(productPriceRepository.saveAll(anyList())).willReturn(List.of(mock(MemberProductPrice.class)));
        given(notificationSettingRepository.save(any(NotificationSetting.class))).willReturn(mock(NotificationSetting.class));
        
        // when
        memberService.signUp(request);
        
        // then
        then(productPriceRepository).should().saveAll(argThat(prices -> {
            List<MemberProductPrice> priceList = (List<MemberProductPrice>) prices;
            // 서울 강남구: 축하(50), 근조(45) = 2개
            // 부산 해운대구: 관엽(30) = 1개
            // 총 3개 레코드가 생성되어야 함
            assertThat(priceList).hasSize(3);
            
            // 서울 강남구 데이터 검증
            List<MemberProductPrice> seoulPrices = priceList.stream()
                .filter(p -> "서울".equals(p.getSido()) && "강남구".equals(p.getSigungu()))
                .collect(Collectors.toList());
            assertThat(seoulPrices).hasSize(2);
            
            // 부산 해운대구 데이터 검증  
            List<MemberProductPrice> busanPrices = priceList.stream()
                .filter(p -> "부산".equals(p.getSido()) && "해운대구".equals(p.getSigungu()))
                .collect(Collectors.toList());
            assertThat(busanPrices).hasSize(1);
            
            return true;
        }));
    }

    private MemberSignupRequest createHierarchicalSignupRequest() {
        MemberSignupRequest request = new MemberSignupRequest();
        request.setLoginId("hierarchical_test");
        request.setPassword("password123");
        request.setName("계층구조테스트화환");
        request.setNickname("계층테스트");
        request.setMobile("010-1111-2222");

        // 사업자 프로필 (간단히)
        MemberSignupRequest.BusinessProfileRequest businessProfile = new MemberSignupRequest.BusinessProfileRequest();
        businessProfile.setBusinessNumber("111-22-33444");
        businessProfile.setCorpName("계층테스트 주식회사");
        businessProfile.setCeoName("김테스트");
        businessProfile.setCompanyAddress("서울 강남구 테스트로 123");
        
        MemberSignupRequest.AddressRequest officeAddress = new MemberSignupRequest.AddressRequest();
        officeAddress.setSido("서울");
        officeAddress.setSigungu("강남구");
        officeAddress.setDetail("테스트로 123");
        officeAddress.setZipcode("12345");
        businessProfile.setOfficeAddress(officeAddress);
        
        request.setBusinessProfile(businessProfile);

        // 활동 지역
        MemberSignupRequest.ActivityRegionRequest activityRegion = new MemberSignupRequest.ActivityRegionRequest();
        activityRegion.setSido("서울");
        activityRegion.setSigungu("강남구");
        request.setActivityRegion(activityRegion);

        // 새로운 계층적 구조로 상품 가격 설정
        List<MemberSignupRequest.RegionalPricingRequest> regionalPricings = new ArrayList<>();
        
        // 서울 강남구 지역
        MemberSignupRequest.RegionalPricingRequest seoulPricing = new MemberSignupRequest.RegionalPricingRequest();
        seoulPricing.setSido("서울");
        seoulPricing.setSigungu("강남구");
        
        List<MemberSignupRequest.CategoryPriceRequest> seoulCategories = new ArrayList<>();
        
        MemberSignupRequest.CategoryPriceRequest celebrationPrice = new MemberSignupRequest.CategoryPriceRequest();
        celebrationPrice.setCategoryName("축하");
        celebrationPrice.setPrice(50);
        celebrationPrice.setIsAvailable(true);
        seoulCategories.add(celebrationPrice);
        
        MemberSignupRequest.CategoryPriceRequest funeralPrice = new MemberSignupRequest.CategoryPriceRequest();
        funeralPrice.setCategoryName("근조");
        funeralPrice.setPrice(45);
        funeralPrice.setIsAvailable(true);
        seoulCategories.add(funeralPrice);
        
        seoulPricing.setCategoryPrices(seoulCategories);
        regionalPricings.add(seoulPricing);
        
        // 부산 해운대구 지역
        MemberSignupRequest.RegionalPricingRequest busanPricing = new MemberSignupRequest.RegionalPricingRequest();
        busanPricing.setSido("부산");
        busanPricing.setSigungu("해운대구");
        
        List<MemberSignupRequest.CategoryPriceRequest> busanCategories = new ArrayList<>();
        
        MemberSignupRequest.CategoryPriceRequest foliagePrice = new MemberSignupRequest.CategoryPriceRequest();
        foliagePrice.setCategoryName("관엽");
        foliagePrice.setPrice(30);
        foliagePrice.setIsAvailable(true);
        busanCategories.add(foliagePrice);
        
        busanPricing.setCategoryPrices(busanCategories);
        regionalPricings.add(busanPricing);
        
        return request;
    }
} 