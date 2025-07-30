package com.sinsaflower.server.domain.member.repository;

import com.sinsaflower.server.domain.member.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    // 회원 ID로 알림 설정 조회
    Optional<NotificationSetting> findByMemberId(Long memberId);
    
    // SMS 알림 활성화된 회원 조회
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.smsOrderCreated = true OR ns.smsOrderCanceled = true OR ns.smsDeliveryStarted = true OR ns.smsDeliveryCompleted = true")
    List<NotificationSetting> findBySmsNotificationEnabled();
    
    // 이메일 알림 활성화된 회원 조회
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.emailOrderCreated = true OR ns.emailOrderCanceled = true OR ns.emailWeeklyReport = true OR ns.emailMonthlyReport = true")
    List<NotificationSetting> findByEmailNotificationEnabled();
    
    // 푸시 알림 활성화된 회원 조회
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.pushOrderCreated = true OR ns.pushDeliveryStarted = true OR ns.pushSystemNotice = true")
    List<NotificationSetting> findByPushNotificationEnabled();
    
    // 특정 알림 유형이 활성화된 회원 조회
    List<NotificationSetting> findBySmsOrderCreatedTrue();
    List<NotificationSetting> findByEmailWeeklyReportTrue();
    List<NotificationSetting> findByPushSystemNoticeTrue();
    
    // 야간 알림 허용 회원 조회
    List<NotificationSetting> findByNightTimeNotificationTrue();
    
    // 회원의 알림 설정 존재 여부 확인
    boolean existsByMemberId(Long memberId);
    
    // 회원의 알림 설정 삭제 (회원 탈퇴 시)
    void deleteByMemberId(Long memberId);
} 