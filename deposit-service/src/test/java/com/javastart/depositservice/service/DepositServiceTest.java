package com.javastart.depositservice.service;

import com.javastart.deposit.controller.dto.DepositResponseDTO;
import com.javastart.deposit.exception.DepositServiceException;
import com.javastart.deposit.repository.DepositRepository;
import com.javastart.deposit.rest.AccountResponseDTO;
import com.javastart.deposit.rest.AccountServiceClient;
import com.javastart.deposit.rest.BillResponseDTO;
import com.javastart.deposit.rest.BillServiceClient;
import com.javastart.deposit.service.DepositService;
import com.javastart.depositservice.util.DepositUtil;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class DepositServiceTest {

    @Mock
    private DepositRepository depositRepository;
    @Mock
    private AccountServiceClient accountServiceClient;
    @Mock
    private BillServiceClient billServiceClient;
    @Mock
    private RabbitTemplate rabbitTemplate;
    //mock не знает, что вернуть при вызове метода


    @InjectMocks
    private DepositService depositService;

    @Test
    public void depositServiceTest_withBillId() {


        BillResponseDTO billResponseDTO = DepositUtil.createBillResponseDTO(1l, 1000l, 1l,
                true, true);
        Mockito.when(billServiceClient.getBillById(ArgumentMatchers.anyLong())).thenReturn(billResponseDTO);
        Mockito.when(accountServiceClient.getAccountById(ArgumentMatchers.anyLong()))
                .thenReturn(DepositUtil.createAccountResponseDTO(1l, Arrays.asList(1l, 2l, 3l),
                        "lory.cat@xyz", "Lori", "+123456"));
        DepositResponseDTO deposit = depositService.deposit(null, 1L, BigDecimal.valueOf(1000));
        Assertions.assertThat(deposit.getMail()).isEqualTo("lory.cat@xyz");
    }

    @Test(expected = DepositServiceException.class)
    public void depositServiceTest_exception() {
        depositService.deposit(null, null, BigDecimal.valueOf(1000));
    }

}
