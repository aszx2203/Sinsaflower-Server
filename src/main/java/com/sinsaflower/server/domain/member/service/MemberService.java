package com.sinsaflower.server.domain.member.service;

import com.sinsaflower.server.domain.member.constants.MemberConstants;
import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.dto.MemberSignupRequest;
import com.sinsaflower.server.domain.member.dto.MemberSignupRequest.ActivityRegionRequest;
import com.sinsaflower.server.domain.member.entity.*;
import com.sinsaflower.server.domain.member.entity.Member.MemberStatus;
import com.sinsaflower.server.domain.member.repository.*;
import com.sinsaflower.server.domain.product.entity.MemberProductPrice;
import com.sinsaflower.server.domain.product.repository.MemberProductPriceRepository;
import com.sinsaflower.server.domain.common.Address;
import com.sinsaflower.server.global.exception.ResourceNotFoundException;
import com.sinsaflower.server.global.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberBusinessProfileRepository businessProfileRepository;
    private final MemberBankAccountRepository bankAccountRepository;
    private final MemberProductPriceRepository productPriceRepository;
    private final MemberActivityRegionRepository activityRegionRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final FileUploadService fileUploadService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 가입 처리
     */
    @Transactional
    public MemberResponse signUp(MemberSignupRequest request) {
        log.info("회원 가입 요청 처리: {}", request.getLoginId());

        // 1. 중복 검증
        validateDuplicateSignup(request);

        // 2. 회원 엔티티 생성
        Member member = createMember(request);

        // 3. 사업자 프로필 생성
        MemberBusinessProfile businessProfile = createBusinessProfile(member, request.getBusinessProfile());

        // 4. 계좌 정보 생성
        if (isValidBankInfo(request.getBusinessProfile())) {
            createBankAccount(businessProfile, request.getBusinessProfile());
        }

        // 5. 활동 지역 생성
        createActivityRegions(member, request.getActivityRegion());

        // 6. 상품 가격 정보 생성
        createProductPrices(member, request);

        log.info("회원 가입 완료: {} (ID: {})", member.getLoginId(), member.getId());
        return convertToResponse(member);
    }

    /**
     * 회원 정보 조회
     */
    @Transactional(readOnly = true)
    public MemberResponse getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + memberId));

        return convertToResponse(member);
    }

    /**
     * 로그인 ID로 회원 조회
     */
    @Transactional(readOnly = true)
    public MemberResponse getMemberByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + loginId));

        return convertToResponse(member);
    }

    /**
     * 로그인 ID 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isLoginIdDuplicate(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    /**
     * 사업자등록번호 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isBusinessNumberDuplicate(String businessNumber) {
        return businessProfileRepository.existsByBusinessNumber(businessNumber);
    }

    /**
     * 승인 대기 중인 멤버 목록 조회
     */
    @Transactional(readOnly = true)
    public List<MemberResponse> getPendingMembers() {
        List<Member> pendingMembers = memberRepository.findByStatus(Member.MemberStatus.PENDING);
        return pendingMembers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 회원 가입 중복 검증
     */
    private void validateDuplicateSignup(MemberSignupRequest request) {
        if (isLoginIdDuplicate(request.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 로그인 ID입니다: " + request.getLoginId());
        }

        if (isBusinessNumberDuplicate(request.getBusinessProfile().getBusinessNumber())) {
            throw new IllegalArgumentException("이미 등록된 사업자등록번호입니다: " + request.getBusinessProfile().getBusinessNumber());
        }
    }

    /**
     * 회원 엔티티 생성
     */
    private Member createMember(MemberSignupRequest request) {
        Member member = Member.builder()
                .loginId(request.getLoginId())
                .password(request.getPassword())
                .name(request.getName())
                .nickname(request.getNickname())
                .mobile(request.getMobile())
                .status(Member.MemberStatus.PENDING)
                .build();

        member.encodePassword(passwordEncoder);
        return memberRepository.save(member);
    }

    /**
     * 사업자 프로필 생성
     */
    private MemberBusinessProfile createBusinessProfile(Member member, MemberSignupRequest.BusinessProfileRequest request) {
        MemberBusinessProfile profile = MemberBusinessProfile.builder()
                .member(member)
                .businessNumber(request.getBusinessNumber())
                .corpName(request.getCorpName())
                .ceoName(request.getCeoName())
                .businessType(request.getBusinessType())
                .businessItem(request.getBusinessItem())
                .fax(request.getFax())
                .companyAddress(request.getCompanyAddress())
                .officeAddress(convertToAddress(request.getOfficeAddress()))
                .approvalStatus(MemberBusinessProfile.ApprovalStatus.PENDING)
                .build();

        // 파일 업로드 처리
        if (request.getBusinessCertFile() != null && !request.getBusinessCertFile().isEmpty()) {
            try {
                String filePath = fileUploadService.saveFile(request.getBusinessCertFile(), "business-cert");
                profile.setBusinessCertFilePath(filePath);
            } catch (Exception e) {
                log.error("사업자등록증 파일 업로드 실패: {}", e.getMessage());
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
            }
        }

        return businessProfileRepository.save(profile);
    }

    /**
     * 계좌 정보 생성
     */
    private void createBankAccount(MemberBusinessProfile profile, MemberSignupRequest.BusinessProfileRequest request) {
        MemberBankAccount account = MemberBankAccount.builder()
                .businessProfile(profile)
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .accountOwner(request.getAccountOwner())
                .isPrimary(true)
                .isActive(true)
                .build();

        // 통장 사본 파일 업로드
        if (request.getBankCertFile() != null && !request.getBankCertFile().isEmpty()) {
            try {
                String filePath = fileUploadService.saveFile(request.getBankCertFile(), "bank-cert");
                account.setBankCertFilePath(filePath);
            } catch (Exception e) {
                log.error("통장사본 파일 업로드 실패: {}", e.getMessage());
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
            }
        }

        bankAccountRepository.save(account);
    }

    /**
     * 활동 지역 생성
     */
    private void createActivityRegions(Member member, ActivityRegionRequest region) {
        if (region == null) return;

        MemberActivityRegion activityRegion = MemberActivityRegion.builder()
        .member(member)
        .sido(region.getSido())
        .sigungu(region.getSigungu())
        .build();

        activityRegionRepository.save(activityRegion);
    }

    /**
     * 상품 가격 정보 생성성
     */
    private void createProductPrices(Member member, MemberSignupRequest request) {
        List<MemberProductPrice> allPrices = new ArrayList<>();

        if (request.getRegionalPricings() != null && !request.getRegionalPricings().isEmpty()) {
            List<MemberProductPrice> hierarchicalPrices = request.getRegionalPricings().values().stream()
                .flatMap(regional -> regional.getCategoryPrices().stream()
                    .map(categoryPrice -> MemberProductPrice.builder()
                        .member(member)
                        .sido(regional.getSido())
                        .sigungu(regional.getSigungu())
                        .categoryName(categoryPrice.getCategoryName())
                        .price(new BigDecimal(categoryPrice.getPrice()))
                        .isAvailable(categoryPrice.getIsAvailable())
                        .build()))
                .collect(Collectors.toList());
            allPrices.addAll(hierarchicalPrices);
        }
        
        if (!allPrices.isEmpty()) {
            productPriceRepository.saveAll(allPrices);
            log.info("상품 가격 정보 생성 완료: {} 개", allPrices.size());
        }
    }

    /**
     * 알림 설정 생성
     */
    private void createNotificationSetting(Member member) {
        NotificationSetting setting = NotificationSetting.builder()
                .member(member)
                .smsOrderCreated(true)
                .smsOrderCanceled(true)
                .smsDeliveryStarted(true)
                .smsDeliveryCompleted(true)
                .smsPaymentCompleted(true)
                .emailOrderCreated(false)
                .emailOrderCanceled(false)
                .emailWeeklyReport(true)
                .emailMonthlyReport(true)
                .pushOrderCreated(true)
                .pushDeliveryStarted(true)
                .pushSystemNotice(true)
                .build();

        notificationSettingRepository.save(setting);
    }

    /**
     * 주소 변환
     */
    private Address convertToAddress(MemberSignupRequest.AddressRequest request) {
        if (request == null) return null;

        return Address.builder()
                .sido(request.getSido())
                .sigungu(request.getSigungu())
                .eupmyeondong(request.getEupmyeondong())
                .detail(request.getDetail())
                .zipcode(request.getZipcode())
                .build();
    }

    /**
     * 은행 정보 유효성 검증
     */
    private boolean isValidBankInfo(MemberSignupRequest.BusinessProfileRequest request) {
        return request.getBankName() != null && !request.getBankName().trim().isEmpty()
                && request.getAccountNumber() != null && !request.getAccountNumber().trim().isEmpty()
                && request.getAccountOwner() != null && !request.getAccountOwner().trim().isEmpty();
    }

    /**
     * 응답 DTO 변환
     */
    private MemberResponse convertToResponse(Member member) {
        return MemberResponse.from(member);
    }

    /**
     * 화환명으로 회원 검색
     */
    public Page<MemberResponse> searchMembersByName(String name, Pageable pageable) {
        Page<Member> members = memberRepository.findByNameContaining(name, pageable);
        return members.map(this::convertToResponse);
    }

    /**
     * 지역별 회원 검색
     */
    public Page<MemberResponse> searchMembersByRegion(String sido, String sigungu, String eupmyeondong, Pageable pageable) {
        Page<Member> members;

        if (sigungu != null && !sigungu.trim().isEmpty()) {
            members = memberRepository.findByActivityRegionSidoAndSigungu(sido, sigungu, pageable);
        } else {
            // 시도만 검색
            members = memberRepository.findByActivityRegionSido(sido, pageable);
        }
        
        return members.map(this::convertToResponse);
    }

    /**
     * 취급 상품별 회원 검색
     */
    public Page<MemberResponse> searchMembersByProduct(String productName, Pageable pageable) {
        Page<Member> members = memberRepository.findByHandlingProductName(productName, pageable);
        return members.map(this::convertToResponse);
    }

    /**
     * 복합 검색 (화환명 + 지역 + 상품)
     */
    public Page<MemberResponse> searchMembersCombined(String name, String sido, String sigungu, String productName, Pageable pageable) {
        // null이나 빈 문자열을 null로 정규화
        String normalizedName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String normalizedSido = (sido != null && !sido.trim().isEmpty()) ? sido.trim() : null;
        String normalizedSigungu = (sigungu != null && !sigungu.trim().isEmpty()) ? sigungu.trim() : null;
        String normalizedProductName = (productName != null && !productName.trim().isEmpty()) ? productName.trim() : null;
        
        Page<Member> members = memberRepository.findByCombinedSearch(
            normalizedName, normalizedSido, normalizedSigungu, normalizedProductName, pageable);
        return members.map(this::convertToResponse);
    }

    /**
     * 지역별 회원 수 통계
     */
    public Map<String, Long> getMemberStatisticsByRegion() {
        List<Object[]> results = memberRepository.countMembersByRegion();
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (String) result[0],  // sido
                    result -> (Long) result[1]     // count
                ));
    }

    /**
     * 취급 상품별 회원 수 통계
     */
    public Map<String, Long> getMemberStatisticsByProduct() {
        List<Object[]> results = memberRepository.countMembersByProduct();
        return results.stream()
                .collect(Collectors.toMap(
                    result -> result[0].toString(), 
                    result -> (Long) result[1]     // count
                ));
    }

    /**
     * 비밀번호 변경 (본인만)
     */
    @Transactional
    public void changePassword(Long memberId, String currentPassword, String newPassword) {
        log.info("비밀번호 변경 요청: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .filter(m -> !m.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(MemberConstants.Messages.MEMBER_NOT_FOUND + ": " + memberId));

        member.changePassword(currentPassword, newPassword, passwordEncoder);
        memberRepository.save(member);

        log.info("비밀번호 변경 완료: {}", memberId);
    }

    /**
     * 모든 활성 회원 조회 (관리자용)
     */
    public Page<MemberResponse> getAllActiveMembers(Pageable pageable) {
        Page<Member> members = memberRepository.findAllActive(pageable);
        
        // --- 디버깅 로그 추가 ---
        members.getContent().forEach(member -> {
            if (member.getBusinessProfile() == null) {
                log.info("Member ID {}: BusinessProfile is NULL after fetch.", member.getId());
            } else {
                log.info("Member ID {}: BusinessProfile FOUND. CorpName: {}", member.getId(), member.getBusinessProfile().getCorpName());
            }
        });
        // ----------------------

        return members.map(this::convertToResponse);
    }

    /**
     * 상태별 회원 조회 (관리자용)
     */
    public Page<MemberResponse> getMembersByStatus(MemberStatus status, Pageable pageable) {
        Page<Member> members = memberRepository.findByStatus(status, pageable);
        return members.map(this::convertToResponse);
    }
} 