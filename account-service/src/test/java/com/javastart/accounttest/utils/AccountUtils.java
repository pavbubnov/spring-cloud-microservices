package com.javastart.accounttest.utils;

import com.javastart.account.entity.Account;

import java.time.OffsetDateTime;
import java.util.List;

public class AccountUtils {

    public static Account createAccount(Long accountId, String name, String email, String phone, List<Long> bills) {

        Account account = new Account();
        account.setAccountId(accountId);
        account.setName(name);
        account.setEmail(email);
        account.setPhone(phone);
        account.setCreationDate(OffsetDateTime.now());
        account.setBills(bills);
        return account;
    }



}
