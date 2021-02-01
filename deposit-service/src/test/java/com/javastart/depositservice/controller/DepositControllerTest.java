package com.javastart.depositservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.deposit.DepositApplication;
import com.javastart.deposit.controller.dto.DepositResponseDTO;
import com.javastart.deposit.entity.Deposit;
import com.javastart.deposit.repository.DepositRepository;
import com.javastart.deposit.rest.AccountResponseDTO;
import com.javastart.deposit.rest.AccountServiceClient;
import com.javastart.deposit.rest.BillResponseDTO;
import com.javastart.deposit.rest.BillServiceClient;
import com.javastart.depositservice.config.SpringH2DatabaseConfig;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DepositApplication.class, SpringH2DatabaseConfig.class})
public class DepositControllerTest {

    //будет запускаться приложение, и мы будем выполнять запрос

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context; //тестовый контекст

    @Autowired //тк база данных уже есть
    private DepositRepository depositRepository;

    @MockBean
    private BillServiceClient billServiceClient;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    //инициализация MockMvc

    @Before //перед тестом
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private static final String REQUEST = "{\n" +
            "    \"billId\": 1,\n" +
            "    \"amount\": 3000\n" +
            "}";

    @Test
    public void createDeposit() throws Exception {
        BillResponseDTO billResponseDTO = createBillResponseDTO();
        Mockito.when(billServiceClient.getBillById(ArgumentMatchers.anyLong())).thenReturn(billResponseDTO);
        Mockito.when(accountServiceClient.getAccountById(ArgumentMatchers.anyLong())).thenReturn(createAccountResponseDTO());
        MvcResult mvcResult = mockMvc.perform(post("/deposits")
                .content(REQUEST).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status()
                .isOk())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
        List<Deposit> deposits = depositRepository.findDepositByEmail("lory.cat@xyz");
        ObjectMapper objectMapper = new ObjectMapper();
        DepositResponseDTO depositResponseDTO = objectMapper.readValue(body, DepositResponseDTO.class);

        Assertions.assertThat(depositResponseDTO.getMail()).isEqualTo(deposits.get(0).getEmail());
        Assertions.assertThat(depositResponseDTO.getAmount()).isEqualTo(BigDecimal.valueOf(3000));
    }

    //поместить в util public дз
    //написать несколько спринг бут тестов для контролеров
    private AccountResponseDTO createAccountResponseDTO() {
        AccountResponseDTO accountResponseDTO = new AccountResponseDTO();
        accountResponseDTO.setAccountId(1l);
        accountResponseDTO.setBills(Arrays.asList(1l, 2l, 3l));
        accountResponseDTO.setCreationDate(OffsetDateTime.now());
        accountResponseDTO.setEmail("lory.cat@xyz");
        accountResponseDTO.setName("Lori");
        accountResponseDTO.setPhone("+123456");
        return accountResponseDTO;
    }

    private BillResponseDTO createBillResponseDTO() {
        BillResponseDTO billResponseDTO = new BillResponseDTO();
        billResponseDTO.setAccountId(1L);
        billResponseDTO.setAccountId(1l);
        billResponseDTO.setAmount(BigDecimal.valueOf(1000));
        billResponseDTO.setBillId(1l);
        billResponseDTO.setCreationDate(OffsetDateTime.now());
        billResponseDTO.setIsDefault(true);
        billResponseDTO.setOverdraftEnabled(true);
        return billResponseDTO;
    }
}
