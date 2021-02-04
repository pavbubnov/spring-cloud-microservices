package com.javastart.account.service;

import com.javastart.account.entity.Account;
import com.javastart.account.exception.AccountNotFoundException;
import com.javastart.account.exception.CreateBillExceptiton;
import com.javastart.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private ArrayList<Long> numberOfBills = new ArrayList<>();

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Unable to find account with id: " + accountId));
    }

    public Long createAccount(String name, String email, String phone, List<Long> bills) {

        Account account = new Account(name, email, phone, OffsetDateTime.now(), bills);
        checkBillsId(bills);
        addToGeneralBillList(bills);

        return accountRepository.save(account).getAccountId();
    }

    public Account updateAccount(Long accountId, String name,
                                 String email, String phone, List<Long> bills) {

        addToGeneralBillList(createUniqueList(getAccountById(accountId).getBills(), bills, new ArrayList<>()));

        Account account = new Account();
        account.setAccountId(accountId);
        account.setBills(bills);
        account.setEmail(email);
        account.setPhone(phone);
        account.setName(name);
        account.setCreationDate(getAccountById(accountId).getCreationDate());

        return accountRepository.save(account);
    }

    public String deleteAccount(Long accountId) {
        Account deletedAccount = getAccountById(accountId);
        accountRepository.deleteById(accountId);
        return "Account with id: " + accountId + " was deleted";
    }

    public Account addAccountBills(Long accountId, List<Long> additionalBills) {

        Account account = getAccountById(accountId);
        checkBillsId(additionalBills);

        List<Long> allBills = account.getBills();
        allBills.addAll(additionalBills);
        account.setBills(allBills);
        return accountRepository.save(account);
    }

    private void checkBillsId(List<Long> bills) {

        ArrayList<Long> numberOfRepeatingBills = new ArrayList<>();
        boolean flagOfRepeatingBills = false;
        boolean flagOfEqualsBills = false;

        for (int k = 0; k < bills.size(); k++) {
            for (int z = k + 1; z < bills.size(); z++) {
                if (bills.get(k) == bills.get(z)) {
                    throw new CreateBillExceptiton("You have equals bills, please, check");
                }
            }
        }

        for (int i = 0; i < numberOfBills.size(); i++) {
            for (int j = 0; j < bills.size(); j++) {
                if (bills.get(j) == numberOfBills.get(i)) {
                    flagOfRepeatingBills = true;
                    numberOfRepeatingBills.add(bills.get(j));
                }
            }
        }
        if (flagOfRepeatingBills) {
            throw new CreateBillExceptiton("There is(are) bills with id :" + numberOfRepeatingBills);
        }
    }

    private void addToGeneralBillList(List<Long> bills) {
        for (int i = 0; i < bills.size(); i++) {
            numberOfBills.add(bills.get(i));
        }
    }

    private ArrayList<Long> createUniqueList(List<Long> oldList, List<Long> bills, ArrayList<Long> uniqueList) {

        for (int i = 0; i < bills.size(); i++) {
            boolean uniqueFlag = true;
            for (int j = 0; j < oldList.size(); j++) {
                if (oldList.get(j) == bills.get(i)) {
                    uniqueFlag = false;
                }
            }
            if (uniqueFlag) {
                uniqueList.add(bills.get(i));
            }
        }

        if (uniqueList != null) {
            checkBillsId(uniqueList);
        }

        return uniqueList;

    }

}
