package com.javastart.account.service;

import com.javastart.account.controller.dto.AddBillsRequestDTO;
import com.javastart.account.entity.Account;
import com.javastart.account.exception.AccountNotFoundException;
import com.javastart.account.exception.CreateBillException;
import com.javastart.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Bubnov
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Get account information by account id
     *
     * @param accountId - Id number of requested account
     * @return {@link Account} - full database information about requested account
     * @throws AccountNotFoundException - if unable to find account with necessary id
     */
    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Unable to find account with id: " + accountId));
    }

    /**
     * Create account of the person
     * <p>
     * Not supported null values of params
     * Bills id list shouldn't contain values database already exist
     * Bills id list shouldn't contain the same values
     * Email address should be as "examplename@postname.domen"
     * <p>
     * List of bills will be checked with {@link AccountService#checkBillsId}
     *
     * @param name  - name of person
     * @param email - email address of the person
     * @param phone - phone number of the person
     * @param bills - start bill id values list of person
     * @return id value of the created account
     */
    public Long createAccount(String name, String email, String phone, List<Long> bills) {

        Account account = new Account(name, email, phone, OffsetDateTime.now(), bills);
        checkBillsId(bills);
        return accountRepository.save(account).getAccountId();
    }

    /**
     * Update account of the person
     * <p>
     *
     * @param accountId - Id number of updating account
     * @param name      - name of person
     * @param email     - email address of the person
     * @param phone     - phone number of the person
     * @param bills     - update bill id values list of person
     * @return {@link Account}
     * @see AccountService#createAccount requirements
     * If params didn't changes it is necessary to repeat values
     * <p>
     * List of bills will be checked with {@link AccountService#checkEqualsList}
     * Сorrect addition of new bill id will be ensured with {@link AccountService#createUniqueList}
     */
    public Account updateAccount(Long accountId, String name,
                                 String email, String phone, List<Long> bills) {

        checkEqualsList(bills);
        createUniqueList(getAccountById(accountId).getBills(), bills, new ArrayList<>());

        Account account = new Account();
        account.setAccountId(accountId);
        account.setBills(bills);
        account.setEmail(email);
        account.setPhone(phone);
        account.setName(name);
        account.setCreationDate(getAccountById(accountId).getCreationDate());

        return accountRepository.save(account);
    }

    /**
     * Delete account of the person
     * <p>
     * Exception from {@link AccountService#getAccountById} if unable to find requested account
     *
     * @param accountId Id number of deleting account
     * @return {@link Account} - last database information before deleting
     */
    public Account deleteAccount(Long accountId) {
        Account deletedAccount = getAccountById(accountId);
        deletedAccount.toString();
        accountRepository.deleteById(accountId);
        return deletedAccount;
    }

    /**
     * Add bills id to database already exist values
     * <p>
     * List of additional bills will be checked with {@link AccountService#checkBillsId}
     *
     * @param accountId          - Id number of necessary account
     * @param addBillsRequestDTO - list of additional bills id
     * @return {@link Account}
     */
    public Account addAccountBills(Long accountId, AddBillsRequestDTO addBillsRequestDTO) {

        List<Long> additionalBills = addBillsRequestDTO.getBills();
        Account account = getAccountById(accountId);
        checkBillsId(additionalBills);

        List<Long> allBills = account.getBills();
        allBills.addAll(additionalBills);
        account.setBills(allBills);
        return accountRepository.save(account);
    }


    /**
     * @param bills - list of bills checking for:
     *              a)same Id values {@link AccountService#checkEqualsList}
     *              b)values that already exist database
     * @throws CreateBillException if list of bills contains the values that already exist database with
     *                             information what Id values unacceptable
     */
    private void checkBillsId(List<Long> bills) {

        ArrayList<Long> numberOfRepeatingBills = new ArrayList<>();
        boolean flagOfRepeatingBills = false;

        checkEqualsList(bills);

        for (int j = 0; j < bills.size(); j++) {
            Account accountByBills = accountRepository.findAccountByBills(bills.get(j));
            if (accountByBills != null) {
                flagOfRepeatingBills = true;
                numberOfRepeatingBills.add(bills.get(j));
            }
        }
        if (flagOfRepeatingBills) {
            throw new CreateBillException("There is(are) bills with id :" + numberOfRepeatingBills);
        }
    }

    /**
     * Create list of Id values that database doesn't contains yet
     *
     * @param oldList    - list of Id values that database already exist
     * @param bills      - update list of Id values that database should exist
     * @param uniqueList - replenishing list of Id values that database doesn't contain yet, empty at start.
     *                   Filled list will check with {@link AccountService#checkBillsId}
     */
    private void createUniqueList(List<Long> oldList, List<Long> bills, ArrayList<Long> uniqueList) {

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
    }

    /**
     * Check list of bills doesn't contain the same values
     *
     * @param bills - List of bills checking for the same Id values
     * @throws CreateBillException if list of bills contains same Id values
     */
    private void checkEqualsList(List<Long> bills) {
        for (int k = 0; k < bills.size(); k++) {
            for (int z = k + 1; z < bills.size(); z++) {
                if (bills.get(k) == bills.get(z)) {
                    throw new CreateBillException("You have equals bills, please, check");
                }
            }
        }
    }

}
