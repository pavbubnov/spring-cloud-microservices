package com.javastart.billtest;

import com.javastart.bill.entity.Bill;
import com.javastart.bill.exception.BillCreateException;
import com.javastart.bill.repository.BillRepository;
import com.javastart.bill.rest.AccountServiceClient;
import com.javastart.bill.service.BillService;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private Bill bill;

    @Mock
    private AccountServiceClient accountServiceClient;

    @InjectMocks
    private BillService billService;

    @Test
    public void createBillTest() {

        returnAccountResponseDTO(1l);

        Throwable throwable = assertThrows(BillCreateException.class, () -> {
            billService.createBill(1l, 1l, BigDecimal.valueOf(5000),
                    true, true);
        });
        assertTrue(throwable.getMessage().equals("Account with id: 1 doesn't contain bill with id: 1"));

        Bill billReturn = createBill(1l, 3l, BigDecimal.valueOf(5000), false, true);

        billService.createBill(1l, 3l, BigDecimal.valueOf(5000), false, true);

        Mockito.when(billRepository.getBillsByAccountIdOrderByBillId(1l)).
                thenReturn(Arrays.asList(billReturn));

        Throwable throwable2 = assertThrows(BillCreateException.class, () -> {
            billService.createBill(1l, 3l, BigDecimal.valueOf(5000),
                    true, true);
        });
        assertTrue(throwable2.getMessage().equals("Bill with id: " + 3l + " is already exists"));

        Throwable throwable3 = assertThrows(BillCreateException.class, () -> {
            billService.getBillById(4l);
        });
        assertTrue(throwable3.getMessage().equals("Unable to find bill with id: " + 4l));

        Assertions.assertThat(billService.getBillsByAccountId(2l)).isEqualTo(Arrays.asList());

        Assertions.assertThat(billService.getBillsByAccountId(1l).get(0).getBillId()).isEqualTo(3);

        Throwable throwable4 = assertThrows(BillCreateException.class, () -> {
            billService.updateBill(4l, 1l, BigDecimal.valueOf(5000), true, true);
        });
        assertTrue(throwable4.getMessage().equals("Bill with id: " + 4l + " is not belongs to account with id: " + 1l +
                " or has't created yet"));

        Throwable throwable5 = assertThrows(BillCreateException.class, () -> {
            billService.updateBill(4l, 2l, BigDecimal.valueOf(5000), true, true);
        });
        assertTrue(throwable5.getMessage().equals("Bill with id: " + 4l + " is not belongs to account with id: " + 2l +
                " or has't created yet"));
    }

    private void returnAccountResponseDTO(Long accountId) {
        Mockito.when(accountServiceClient.getAccountById(1l)).
                thenReturn(BillUtils.createAccountResponseDTO(1l, Arrays.asList(3l, 4l, 10l),
                        "Lori@cat.xyz", "Lori", "+123456"));
    }

    private Bill createBill(Long accountId, Long billId, BigDecimal amount, Boolean isDefault,
                            Boolean overdraftEnabled) {

        Bill billReturn = BillUtils.createBill(accountId, billId, amount, isDefault, overdraftEnabled);

        Mockito.when(billRepository.save(ArgumentMatchers.any(Bill.class))).thenReturn(billReturn);

        billService.createBill(accountId, billId, amount, isDefault, overdraftEnabled);

        Mockito.when(accountServiceClient.getAccountById(1l)).thenReturn(
                BillUtils.createAccountResponseDTO(1l, Arrays.asList(3l, 4l, 10l), "Lori@cat.xyz",
                        "Lori", "+123456"));

        return billReturn;

    }


}
