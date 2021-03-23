package com.javastart.depositservice.util;

import com.javastart.deposit.rest.AccountResponseDTO;
import com.javastart.deposit.rest.BillResponseDTO;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class DepositUtil {

    public static AccountResponseDTO createAccountResponseDTO(Long accountId, List<Long> bills, String email,
                                                              String name, String phone) {
        AccountResponseDTO accountResponseDTO = new AccountResponseDTO();
        accountResponseDTO.setAccountId(accountId);
        accountResponseDTO.setBills(bills);
        accountResponseDTO.setCreationDate(OffsetDateTime.now());
        accountResponseDTO.setEmail(email);
        accountResponseDTO.setName(name);
        accountResponseDTO.setPhone(phone);
        return accountResponseDTO;
    }

    public static BillResponseDTO createBillResponseDTO(Long accountId, Long amount, Long billId, Boolean isDefault,
                                                        Boolean isOverdraftEnabled) {
        BillResponseDTO billResponseDTO = new BillResponseDTO();
        billResponseDTO.setAccountId(accountId);
        billResponseDTO.setAmount(BigDecimal.valueOf(amount));
        billResponseDTO.setBillId(billId);
        billResponseDTO.setCreationDate(OffsetDateTime.now());
        billResponseDTO.setIsDefault(isDefault);
        billResponseDTO.setOverdraftEnabled(isOverdraftEnabled);
        return billResponseDTO;
    }

}
