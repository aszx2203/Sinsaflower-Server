package com.sinsaflower.server.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    // SMS 알림 설정
    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean smsOrderCreated = true;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean smsOrderCanceled = true;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean smsDeliveryStarted = true;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean smsDeliveryCompleted = true;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean smsPaymentCompleted = true;

    // 전화 알림 설정
    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean callOrderCreated = false;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean callDeliveryStarted = false;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean callEmergencyOnly = true;

    // 이메일 알림 설정
    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean emailOrderCreated = false;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean emailOrderCanceled = false;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean emailWeeklyReport = true;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean emailMonthlyReport = true;

    // 푸시 알림 설정
    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean pushOrderCreated = true;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean pushDeliveryStarted = true;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean pushSystemNotice = true;

    // 알림 시간 설정
    @Column(length = 5)
    private String notificationStartTime = "09:00";

    @Column(length = 5)
    private String notificationEndTime = "21:00";

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean nightTimeNotification = false;
    
    // 비즈니스 메서드
    public void enableAllSmsNotifications() {
        this.smsOrderCreated = true;
        this.smsOrderCanceled = true;
        this.smsDeliveryStarted = true;
        this.smsDeliveryCompleted = true;
        this.smsPaymentCompleted = true;
    }

    public void disableAllSmsNotifications() {
        this.smsOrderCreated = false;
        this.smsOrderCanceled = false;
        this.smsDeliveryStarted = false;
        this.smsDeliveryCompleted = false;
        this.smsPaymentCompleted = false;
    }

    public boolean isNotificationTimeValid(String currentTime) {
        if (nightTimeNotification) {
            return true;
        }
        return currentTime.compareTo(notificationStartTime) >= 0 &&
                currentTime.compareTo(notificationEndTime) <= 0;
    }
}