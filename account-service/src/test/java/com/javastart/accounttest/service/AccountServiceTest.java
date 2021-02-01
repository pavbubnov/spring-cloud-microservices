package com.javastart.accounttest.service;

import com.javastart.account.entity.Account;
import com.javastart.account.exception.AccountNotFoundException;
import com.javastart.account.exception.CreateBillExceptiton;
import com.javastart.account.repository.AccountRepository;
import com.javastart.account.service.AccountService;
import com.javastart.accounttest.utils.AccountUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {


    @Mock
    private AccountRepository accountRepository;

    @Mock
    private Account account;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AccountService accountService;

    @Test
    public void getAccountTest() {

        Throwable throwable = assertThrows(CreateBillExceptiton.class, () -> {
            accountService.createAccount("Lori", "Lori@cat.xyz",
                    "+123456", Arrays.asList(1l, 2l, 3l, 2l));
        });
        assertTrue(throwable.getMessage().equals("You have equals bills, please, check"));

        Account accountReturn = createAccount(1l, "Lori", "Lori@cat.xyz",
                "+123456", Arrays.asList(3l, 4l, 10l));

        Throwable throwable2 = assertThrows(CreateBillExceptiton.class, () -> {
            accountService.createAccount("Lori", "Lori@cat.xyz",
                    "+123456", Arrays.asList(1l, 2l, 3l, 4l));
        });
        assertTrue(throwable2.getMessage().equals("There is(are) bills with id :[3, 4]"));

        Assertions.assertThat(accountService.getAccountById(1l).getName()).isEqualTo("Lori");
        Assertions.assertThat(accountService.getAccountById(1l).getAccountId()).isEqualTo(1l);
        Assertions.assertThat(accountService.getAccountById(1l).getBills()).containsExactlyElementsOf(Arrays.asList(3l, 4l, 10l));
        Assertions.assertThat(accountService.getAccountById(1l).getEmail()).isEqualTo("Lori@cat.xyz");
        Assertions.assertThat(accountService.getAccountById(1l).getPhone()).isEqualTo("+123456");
        Assertions.assertThat(accountService.getAccountById(1l).getCreationDate()).isEqualTo(accountReturn.getCreationDate());

        Throwable throwable3 = assertThrows(AccountNotFoundException.class, () -> {
            accountService.getAccountById(2l);
        });

        assertTrue(throwable3.getMessage().equals("Unable to find account with id: 2"));
    }

    @Test
    public void addAccountBillsTest() {

        Account accountReturn = createAccount(1l, "Lori", "Lori@cat.xyz",
                "+123456", Arrays.asList(3l, 4l, 10l));

        Throwable throwable = assertThrows(CreateBillExceptiton.class, () -> {
            accountService.addAccountBills(1l, Arrays.asList(5l, 10l, 11l));
        });

        assertTrue(throwable.getMessage().equals("There is(are) bills with id :[10]"));

        Throwable throwable2 = assertThrows(CreateBillExceptiton.class, () -> {
            accountService.addAccountBills(1l, Arrays.asList(11l, 12l, 11l));
        });

        assertTrue(throwable2.getMessage().equals("You have equals bills, please, check"));

        ArrayList<Long> addBills = new ArrayList<>();
        addBills.add(11l);
        addBills.add(12l);

        accountService.addAccountBills(1l, addBills);

        Assertions.assertThat(accountService.getAccountById(1l).getBills()).
                containsExactlyElementsOf(Arrays.asList(3l, 4l, 10l, 11l, 12l));
    }

    @Test
    public void deleteAccountTest() {
        Account accountReturn = createAccount(1l, "Lori", "Lori@cat.xyz",
                "+123456", Arrays.asList(3l, 4l, 10l));

        Assertions.assertThat(accountService.getAccountById(1l).getName()).isEqualTo("Lori");

        Throwable throwable = assertThrows(AccountNotFoundException.class, () -> {
            accountService.deleteAccount(2l);
        });

    }

    @Test
    public void updateAccountTest() {

        Account loryAccountReturn = createAccount(1l, "Lori", "Lori@cat.xyz",
                "+123456", Arrays.asList(3l, 4l, 10l));

        Throwable throwable = assertThrows(CreateBillExceptiton.class, () -> {
            accountService.updateAccount(1l, "Lori", "Lori@cat.xyz",
                    "+123456", Arrays.asList(5l, 11l, 5l));
        });
        assertTrue(throwable.getMessage().equals("You have equals bills, please, check"));

        Account baxterAccountReturn = createAccount(2l, "Baxter", "baxter@cat.xyz",
                "+78912", Arrays.asList(1l, 2l));

        Throwable throwable2 = assertThrows(CreateBillExceptiton.class, () -> {
            accountService.updateAccount(1l, "Lori", "Lori@cat.xyz",
                    "+123456", Arrays.asList(1l, 11l));
        });

        assertTrue(throwable2.getMessage().equals("There is(are) bills with id :[1]"));

    }

    private Account createAccount(Long accountId, String name, String email, String phone, List<Long> startBillList) {

        ArrayList<Long> startBills = new ArrayList<>();

        for (int i = 0; i < startBillList.size(); i++) {
            startBills.add(startBillList.get(i));
        }

        Account accountReturn = AccountUtils.createAccount(accountId, name, email, phone, startBills);


        Mockito.when(accountRepository.save(ArgumentMatchers.any(Account.class)))
                .thenReturn(accountReturn);

        accountService.createAccount(name, email, phone, startBills);

        Mockito.when(accountRepository.findById(accountId)).thenReturn(java.util.Optional.of(accountReturn));

        return accountReturn;
    }
}
