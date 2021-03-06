package com.javastart.billtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.bill.BillApplication;
import com.javastart.bill.controller.dto.BillResponseDTO;
import com.javastart.bill.rest.AccountServiceClient;
import com.javastart.billtest.config.SpringH2DatabaseConfig;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BillApplication.class, SpringH2DatabaseConfig.class})
public class BillControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;


    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private static final String REQUEST_ONE = "{\n" +
            "    \"account_id\": 1,\n" +
            "    \"bill_id\": 1,\n" +
            "    \"amount\": 3000,\n" +
            "    \"is_default\": false,\n" +
            "    \"is_overdraft_enabled\": false\n" +
            "}";

    private static final String REQUEST_TWO = "{\n" +
            "    \"account_id\": 1,\n" +
            "    \"bill_id\": 3,\n" +
            "    \"amount\": 3000,\n" +
            "    \"is_default\": true,\n" +
            "    \"is_overdraft_enabled\": false\n" +
            "}";

    private static final String REQUEST_THREE = "{\n" +
            "    \"account_id\": 1,\n" +
            "    \"bill_id\": 1,\n" +
            "    \"amount\": 3000,\n" +
            "    \"is_default\": true,\n" +
            "    \"is_overdraft_enabled\": false\n" +
            "}";

    private static final String REQUEST_REDEFAULT_ERROR = "{\n" +
            "    \"account_id\": 1,\n" +
            "    \"bill_id\": 1,\n" +
            "    \"amount\": 3000,\n" +
            "    \"is_default\": false,\n" +
            "    \"is_overdraft_enabled\": false\n" +
            "}";


    @Test
    public void billTest() throws Exception {

        Mockito.when(accountServiceClient.getAccountById(1l))
                .thenReturn(BillUtils.createAccountResponseDTO(1l, Arrays.asList(1l, 3l, 5l),
                        "lory.cat@xyz", "Lori", "+123456"));

        MvcResult mvcResultOne = mockMvc.perform(post("/")
                .content(REQUEST_ONE).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status()
                .isOk())
                .andReturn();

        String body = mvcResultOne.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        Long returnBillId = objectMapper.readValue(body, Long.class);
        Assertions.assertThat(returnBillId).isEqualTo(1l);


        MvcResult getRequestBillOne = mockMvc.perform(get("/1")).andExpect(status()
                .isOk()).andReturn();

        body = getRequestBillOne.getResponse().getContentAsString();
        BillResponseDTO billResponseDTO = objectMapper.readValue(body, BillResponseDTO.class);

        Assertions.assertThat(billResponseDTO.getAmount().longValue()).isEqualTo(3000l);
        Assertions.assertThat(billResponseDTO.getIsDefault()).isEqualTo(true);

        mockMvc.perform(post("/")
                .content(REQUEST_TWO).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status()
                .isOk());

        getRequestBillOne = mockMvc.perform(get("/1")).andExpect(status()
                .isOk()).andReturn();

        MvcResult getRequestBillThree = mockMvc.perform(get("/3")).andExpect(status()
                .isOk()).andReturn();

        body = getRequestBillOne.getResponse().getContentAsString();
        billResponseDTO = objectMapper.readValue(body, BillResponseDTO.class);
        Assertions.assertThat(billResponseDTO.getIsDefault()).isEqualTo(false);

        body = getRequestBillThree.getResponse().getContentAsString();
        billResponseDTO = objectMapper.readValue(body, BillResponseDTO.class);
        Assertions.assertThat(billResponseDTO.getIsDefault()).isEqualTo(true);

        mockMvc.perform(put("/1")
                .content(REQUEST_THREE).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status()
                .isOk());

        getRequestBillOne = mockMvc.perform(get("/1")).andExpect(status()
                .isOk()).andReturn();

        getRequestBillThree = mockMvc.perform(get("/3")).andExpect(status()
                .isOk()).andReturn();

        body = getRequestBillOne.getResponse().getContentAsString();
        billResponseDTO = objectMapper.readValue(body, BillResponseDTO.class);
        Assertions.assertThat(billResponseDTO.getIsDefault()).isEqualTo(true);

        body = getRequestBillThree.getResponse().getContentAsString();
        billResponseDTO = objectMapper.readValue(body, BillResponseDTO.class);
        Assertions.assertThat(billResponseDTO.getIsDefault()).isEqualTo(false);

        getRequestBillThree = mockMvc.perform(put("/1")
                .content(REQUEST_REDEFAULT_ERROR).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status()
                .isNotFound()).andReturn();

        Assertions.assertThat(getRequestBillThree.getResolvedException().getMessage())
                .isEqualTo("Your account must contain default bill");
    }
}
