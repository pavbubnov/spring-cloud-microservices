package com.javastart.accounttest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.account.AccountApplication;
import com.javastart.account.controller.dto.AccountResponseDTO;
import com.javastart.accounttest.config.SpringH2DatabaseConfig;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AccountApplication.class, SpringH2DatabaseConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class AccountControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setupBefore() {

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

    @Test()
    public void equalsBillsTest() throws Exception {

        Assertions.assertThat(createAccountIsBadRequest(REQUEST_EQUALS).getResolvedException().getMessage())
                .isEqualTo("You have equals bills, please, check");
    }

    @Test
    public void repeatingBillsTest() throws Exception {
        createAccountIsOk(REQUEST_ONE);
        Assertions.assertThat(createAccountIsBadRequest(REQUEST_REPEATING).getResolvedException().getMessage())
                .isEqualTo("There is(are) bills with id :[3, 5]");
    }

    @Test
    public void unableToFindBillTest() throws Exception {
        createAccountIsOk(REQUEST_ONE);
        createAccountIsOk(REQUEST_TWO);

        MvcResult getRequestNotExist = mockMvc.perform(get("/5")).andExpect(status()
                .isNotFound()).andReturn();

        Assertions.assertThat(getRequestNotExist.getResolvedException().getMessage())
                .isEqualTo("Unable to find account with id: " + 5);
    }

    @Test
    public void getTestAccountOneOk() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        createAccountIsOk(REQUEST_ONE);

        String body = getRequestOK("/1").getResponse().getContentAsString();
        AccountResponseDTO accountResponseDTOOne = objectMapper.readValue(body, AccountResponseDTO.class);

        Assertions.assertThat(accountResponseDTOOne.getName()).isEqualTo("Lori");
        Assertions.assertThat(accountResponseDTOOne.getBills()).containsExactly(1l, 3l, 5l);
    }

    @Test
    public void updateExceptionTest() throws Exception {
        createAccountIsOk(REQUEST_ONE);
        createAccountIsOk(REQUEST_TWO);

        MvcResult mvcResultRepeating = mockMvc.perform(put("/2")
                .content(UPDATE_REQUEST_REPEATING).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

        Assertions.assertThat(mvcResultRepeating.getResolvedException().getMessage())
                .isEqualTo("There is(are) bills with id :[3]");

        MvcResult mvcResultEquals = mockMvc.perform(put("/2")
                .content(UPDATE_REQUEST_EQUALS).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

        Assertions.assertThat(mvcResultEquals.getResolvedException().getMessage())
                .isEqualTo("You have equals bills, please, check");

    }


    @Test
    public void updateOkTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        createAccountIsOk(REQUEST_ONE);
        createAccountIsOk(REQUEST_TWO);

        MvcResult mvcResultUpdate = mockMvc.perform(put("/2")
                .content(UPDATE_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();


        String body = getRequestOK("/2").getResponse().getContentAsString();
        AccountResponseDTO accountResponseDTOTwo = objectMapper.readValue(body, AccountResponseDTO.class);

        Assertions.assertThat(accountResponseDTOTwo.getEmail()).isEqualTo("BaxterOfficial@dog.xyz");
        Assertions.assertThat(accountResponseDTOTwo.getBills()).containsExactly(2l, 4l, 6l, 8l);

    }

    @Test
    public void addBillsExceptionTest() throws Exception {
        createAccountIsOk(REQUEST_ONE);
        createAccountIsOk(REQUEST_TWO);

        MvcResult mvcResultAdd = mockMvc.perform(patch("/2")
                .content(ADD_REQUEST_REPEATING).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();

        Assertions.assertThat(mvcResultAdd.getResolvedException().getMessage())
                .isEqualTo("There is(are) bills with id :[3]");
    }

    @Test
    public void addBillsOkTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        createAccountIsOk(REQUEST_ONE);
        createAccountIsOk(REQUEST_TWO);

        MvcResult mvcResultAdd = mockMvc.perform(patch("/2")
                .content(ADD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

        String body = getRequestOK("/2").getResponse().getContentAsString();
        AccountResponseDTO accountResponseDTOTwo = objectMapper.readValue(body, AccountResponseDTO.class);

        Assertions.assertThat(accountResponseDTOTwo.getBills()).containsExactly(2l, 4l, 6l, 10l, 11l);

    }

    @Test
    public void deleteExceptionTest() throws Exception {
        createAccountIsOk(REQUEST_ONE);
        createAccountIsOk(REQUEST_TWO);

        MvcResult mvcResultDelete = mockMvc.perform(delete("/3"))
                .andExpect(status().isNotFound()).andReturn();

        Assertions.assertThat(mvcResultDelete.getResolvedException().getMessage())
                .isEqualTo("Unable to find account with id: " + 3);

    }

    @Test
    public void deleteOkTest() throws Exception {
        createAccountIsOk(REQUEST_ONE);
        createAccountIsOk(REQUEST_TWO);

        MvcResult mvcResultDelete = mockMvc.perform(delete("/2"))
                .andExpect(status().isOk()).andReturn();

        MvcResult getRequestAccountTwo = mockMvc.perform(get("/2")).andExpect(status()
                .isNotFound()).andReturn();

        Assertions.assertThat(getRequestAccountTwo.getResolvedException().getMessage())
                .isEqualTo("Unable to find account with id: " + 2);
    }

    private MvcResult createAccountIsOk(String request) throws Exception {
        return mockMvc.perform(post("/")
                .content(request).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
    }

    private MvcResult createAccountIsBadRequest(String request) throws Exception {
        return mockMvc.perform(post("/")
                .content(request).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andReturn();
    }

    private MvcResult getRequestOK(String id) throws Exception {
        return mockMvc.perform(get(id)).andExpect(status()
                .isOk()).andReturn();
    }


}
