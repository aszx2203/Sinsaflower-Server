package com.sinsaflower.server.domain.member.dto;

import com.sinsaflower.server.domain.member.entity.MemberBankAccount;
import lombok.Builder;

@Builder
public record BankAccountResponse(
        String bankName,
        String accountNumber,
        String accountHolder
) {
    public static BankAccountResponse from(MemberBankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }
        return BankAccountResponse.builder()
                .bankName(bankAccount.getBankName())
                .accountNumber(bankAccount.getAccountNumber())
                .build();
    }
}
