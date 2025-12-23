package com.sinsaflower.server.domain.member.dto;

import com.sinsaflower.server.domain.common.Address;
import com.sinsaflower.server.domain.member.entity.MemberBusinessProfile;
import lombok.Builder;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record BusinessProfileResponse(
        String companyAddress,
        Address officeAddress,
        String fax,
        String businessNumber,
        String corpName,
        String ceoName,
        String businessType,
        String businessItem,
        String businessCertFilePath,
        List<BankAccountResponse> bankAccounts,
        Boolean autoProductRegister,
        Boolean canNightDelivery,
        LocalTime deliveryStartTime,
        LocalTime deliveryEndTime,
        MemberBusinessProfile.ApprovalStatus approvalStatus,
        String rejectionReason
) {
    public static BusinessProfileResponse from(MemberBusinessProfile profile) {
        if (profile == null) {
            return null;
        }
        return BusinessProfileResponse.builder()
                .companyAddress(profile.getCompanyAddress())
                .officeAddress(profile.getOfficeAddress())
                .fax(profile.getFax())
                .businessNumber(profile.getBusinessNumber())
                .corpName(profile.getCorpName())
                .ceoName(profile.getCeoName())
                .businessType(profile.getBusinessType())
                .businessItem(profile.getBusinessItem())
                .businessCertFilePath(profile.getBusinessCertFilePath())
                .bankAccounts(profile.getBankAccounts().stream()
                        .map(BankAccountResponse::from)
                        .collect(Collectors.toList()))
                .autoProductRegister(profile.getAutoProductRegister())
                .canNightDelivery(profile.getCanNightDelivery())
                .deliveryStartTime(profile.getDeliveryStartTime())
                .deliveryEndTime(profile.getDeliveryEndTime())
                .approvalStatus(profile.getApprovalStatus())
                .rejectionReason(profile.getRejectionReason())
                .build();
    }
}
