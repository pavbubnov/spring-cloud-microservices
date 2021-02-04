package com.javastart.billtest;

import com.javastart.bill.entity.Bill;
import com.javastart.bill.rest.AccountResponseDTO;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class BillUtils {

    public static Bill createBill(Long accountId, Long billId, BigDecimal amount, Boolean isDefault,
                                  Boolean overdraftEnabled) {

        Bill bill = new Bill();

        bill.setBillId(billId);
        bill.setAccountId(accountId);
        bill.setAmount(amount);
        bill.setIsDefault(isDefault);
        bill.setCreationDate(OffsetDateTime.now());
        bill.setOverdraftEnabled(overdraftEnabled);

        return bill;
    }

    public static AccountResponseDTO createAccountResponseDTO(Long accountId, List<Long> bills, String email, String name,
                                                              String phone) {
        AccountResponseDTO accountResponseDTO = new AccountResponseDTO();
        accountResponseDTO.setAccountId(accountId);
        accountResponseDTO.setBills(bills);
        accountResponseDTO.setCreationDate(OffsetDateTime.now());
        accountResponseDTO.setEmail(email);
        accountResponseDTO.setName(name);
        accountResponseDTO.setPhone(phone);
        return accountResponseDTO;
    }


}
