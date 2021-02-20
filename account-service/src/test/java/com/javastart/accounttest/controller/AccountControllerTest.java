package com.javastart.accounttest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.account.AccountApplication;
import com.javastart.account.controller.dto.AccountResponseDTO;
import com.javastart.account.controller.dto.AddBillsRequestDTO;
import com.javastart.accounttest.config.SpringH2DatabaseConfig;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AccountApplication.class, SpringH2DatabaseConfig.class})
public class AccountControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private RabbitTemplate rabbitTemplate;


    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private static final String REQUEST_EQUALS = "{\n" +
            "    \"name\": \"Lori\",\n" +
            "    \"email\": \"Lori@cat.xyz\",\n" +
            "    \"phone\": \"+123456\",\n" +
            "    \"bills\": [1,3,3]\n" +
            "}";

    private static final String REQUEST_ONE = "{\n" +
            "    \"name\": \"Lori\",\n" +
            "    \"email\": \"Lori@cat.xyz\",\n" +
            "    \"phone\": \"+123456\",\n" +
            "    \"bills\": [1,3,5]\n" +
            "}";

    private static final String REQUEST_REPEATING = "{\n" +
            "    \"name\": \"Baxter\",\n" +
            "    \"email\": \"Baxter@dog.xyz\",\n" +
            "    \"phone\": \"+78910\",\n" +
            "    \"bills\": [2,3,5,6]\n" +
            "}";

    private static final String REQUEST_TWO = "{\n" +
            "    \"name\": \"Baxter\",\n" +
            "    \"email\": \"Baxter@dog.xyz\",\n" +
            "    \"phone\": \"+78910\",\n" +
            "    \"bills\": [2,4,6]\n" +
            "}";

    private static final String UPDATE_REQUEST_REPEATING = "{\n" +
            "    \"name\": \"Baxter\",\n" +
            "    \"email\": \"BaxterOfficial@dog.xyz\",\n" +
            "    \"phone\": \"+78910\",\n" +
            "    \"bills\": [2,3,4,6]\n" +
            "}";

    private static final String UPDATE_REQUEST_EQUALS = "{\n" +
            "    \"name\": \"Baxter\",\n" +
            "    \"email\": \"BaxterOfficial@dog.xyz\",\n" +
            "    \"phone\": \"+78910\",\n" +
            "    \"bills\": [2,4,6,4]\n" +
            "}";

    private static final String UPDATE_REQUEST = "{\n" +
            "    \"name\": \"Baxter\",\n" +
            "    \"email\": \"BaxterOfficial@dog.xyz\",\n" +
            "    \"phone\": \"+78910\",\n" +
            "    \"bills\": [2,4,6,8]\n" +
            "}";

    private static final String ADD_REQUEST_REPEATING = "{\n" +
            "    \"bills\": [3,10]\n" +
            "}";

    private static final String ADD_REQUEST = "{\n" +
            "    \"bills\": [10, 11]\n" +
            "}";


    @Test
    public void accountTest() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        MvcResult mvcResultEquals = mockMvc.perform(post("/")
                .content(REQUEST_EQUALS).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

        Assertions.assertThat(mvcResultEquals.getResolvedException().getMessage())
                .isEqualTo("You have equals bills, please, check");

        MvcResult mvcResultOne = mockMvc.perform(post("/")
                .content(REQUEST_ONE).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

        MvcResult mvcResultRepeating = mockMvc.perform(post("/")
                .content(REQUEST_REPEATING).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

        Assertions.assertThat(mvcResultRepeating.getResolvedException().getMessage())
                .isEqualTo("There is(are) bills with id :[3, 5]");

        MvcResult mvcResultTwo = mockMvc.perform(post("/")
                .content(REQUEST_TWO).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();


        MvcResult getRequestNotExist = mockMvc.perform(get("/5")).andExpect(status()
                .isNotFound()).andReturn();

        Assertions.assertThat(getRequestNotExist.getResolvedException().getMessage())
                .isEqualTo("Unable to find account with id: " + 5);


        MvcResult getRequestAccountOne = mockMvc.perform(get("/1")).andExpect(status()
                .isOk()).andReturn();

        String body = getRequestAccountOne.getResponse().getContentAsString();
        AccountResponseDTO accountResponseDTOOne = objectMapper.readValue(body, AccountResponseDTO.class);

        Assertions.assertThat(accountResponseDTOOne.getName()).isEqualTo("Lori");
        Assertions.assertThat(accountResponseDTOOne.getBills()).containsExactly(1l,3l,5l);

        mvcResultRepeating = mockMvc.perform(put("/2")
                .content(UPDATE_REQUEST_REPEATING).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

        Assertions.assertThat(mvcResultRepeating.getResolvedException().getMessage())
                .isEqualTo("There is(are) bills with id :[3]");

        mvcResultEquals = mockMvc.perform(put("/2")
                .content(UPDATE_REQUEST_EQUALS).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

        Assertions.assertThat(mvcResultEquals.getResolvedException().getMessage())
                .isEqualTo("You have equals bills, please, check");

        MvcResult mvcResultUpdate = mockMvc.perform(put("/2")
                .content(UPDATE_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

        MvcResult getRequestAccountTwo = mockMvc.perform(get("/2")).andExpect(status()
                .isOk()).andReturn();

        body = getRequestAccountTwo.getResponse().getContentAsString();
        AccountResponseDTO accountResponseDTOTwo = objectMapper.readValue(body, AccountResponseDTO.class);

        Assertions.assertThat(accountResponseDTOTwo.getEmail()).isEqualTo("BaxterOfficial@dog.xyz");
        Assertions.assertThat(accountResponseDTOTwo.getBills()).containsExactly(2l,4l,6l,8l);


        MvcResult mvcResultAdd = mockMvc.perform(patch("/2")
                .content(ADD_REQUEST_REPEATING).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

        Assertions.assertThat(mvcResultAdd.getResolvedException().getMessage())
                .isEqualTo("There is(are) bills with id :[3]");

        mvcResultAdd = mockMvc.perform(patch("/2")
                .content(ADD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

        getRequestAccountTwo = mockMvc.perform(get("/2")).andExpect(status()
                .isOk()).andReturn();

        body = getRequestAccountTwo.getResponse().getContentAsString();
        accountResponseDTOTwo = objectMapper.readValue(body, AccountResponseDTO.class);

        Assertions.assertThat(accountResponseDTOTwo.getBills()).containsExactly(2l,4l,6l,8l,10l,11l);

        MvcResult mvcResultDelete = mockMvc.perform(delete("/3"))
                .andExpect(status().isNotFound()).andReturn();

        Assertions.assertThat(mvcResultDelete.getResolvedException().getMessage())
                .isEqualTo("Unable to find account with id: " + 3);

        mvcResultDelete = mockMvc.perform(delete("/2"))
                .andExpect(status().isOk()).andReturn();

        getRequestAccountTwo = mockMvc.perform(get("/2")).andExpect(status()
                .isNotFound()).andReturn();

        Assertions.assertThat(getRequestAccountTwo.getResolvedException().getMessage())
                .isEqualTo("Unable to find account with id: " + 2);

    }
}
