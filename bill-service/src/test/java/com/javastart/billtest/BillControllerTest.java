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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
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
            "    \"bill_id\": 3,\n" +
            "    \"amount\": 3000,\n" +
            "    \"is_default\": false,\n" +
            "    \"is_overdraft_enabled\": false\n" +
            "}";

    @Test
    public void createBillOkTest() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockAccount();
        String body = createBillOk(REQUEST_ONE).getResponse().getContentAsString();
        Long returnBillId = objectMapper.readValue(body, Long.class);
        Assertions.assertThat(returnBillId).isEqualTo(1l);

    }

    @Test
    public void getBillOkTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockAccount();

        createBillOk(REQUEST_ONE);

        String body = getRequestOk("/1").getResponse().getContentAsString();
        BillResponseDTO billResponseDTO = objectMapper.readValue(body, BillResponseDTO.class);

        Assertions.assertThat(billResponseDTO.getAmount().longValue()).isEqualTo(3000l);
        Assertions.assertThat(billResponseDTO.getIsDefault()).isEqualTo(true);
    }

    @Test
    public void reDefaultCreateTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockAccount();

        createBillOk(REQUEST_ONE);
        createBillOk(REQUEST_TWO);

        String body = getRequestOk("/1").getResponse().getContentAsString();
        BillResponseDTO billResponseDTO = objectMapper.readValue(body, BillResponseDTO.class);
        Assertions.assertThat(billResponseDTO.getIsDefault()).isEqualTo(false);

        body = getRequestOk("/3").getResponse().getContentAsString();
        billResponseDTO = objectMapper.readValue(body, BillResponseDTO.class);
        Assertions.assertThat(billResponseDTO.getIsDefault()).isEqualTo(true);
    }

    @Test
    public void reDefaultUpdateTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockAccount();

        createBillOk(REQUEST_ONE);
        createBillOk(REQUEST_TWO);

        mockMvc.perform(put("/1")
                .content(REQUEST_THREE).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status()
                .isOk());

        String body = getRequestOk("/1").getResponse().getContentAsString();
        BillResponseDTO billResponseDTO = objectMapper.readValue(body, BillResponseDTO.class);
        Assertions.assertThat(billResponseDTO.getIsDefault()).isEqualTo(true);

        body = getRequestOk("/3").getResponse().getContentAsString();
        billResponseDTO = objectMapper.readValue(body, BillResponseDTO.class);
        Assertions.assertThat(billResponseDTO.getIsDefault()).isEqualTo(false);
    }


    @Test
    public void reDefaultExceptionTest() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockAccount();

        createBillOk(REQUEST_ONE);
        createBillOk(REQUEST_TWO);

        MvcResult getRequestBillThree = mockMvc.perform(put("/3")
                .content(REQUEST_REDEFAULT_ERROR).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status()
                .isNotFound()).andReturn();

        Assertions.assertThat(getRequestBillThree.getResolvedException().getMessage())
                .isEqualTo("Your account must contain default bill");
    }

    private MvcResult createBillOk(String request) throws Exception {
        return mockMvc.perform(post("/")
                .content(request).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status()
                .isOk())
                .andReturn();
    }

    private void mockAccount() {
        Mockito.when(accountServiceClient.getAccountById(1l))
                .thenReturn(BillUtils.createAccountResponseDTO(1l, Arrays.asList(1l, 3l, 5l),
                        "lory.cat@xyz", "Lori", "+123456"));
    }

    private MvcResult getRequestOk(String id) throws Exception {
        return mockMvc.perform(get(id)).andExpect(status()
                .isOk()).andReturn();
    }

}
