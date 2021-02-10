package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.transfer.TransferApplication;
import com.javastart.transfer.controller.dto.TransferResponseDTO;
import com.javastart.transfer.entity.Transfer;
import com.javastart.transfer.repository.TransferRepository;
import com.javastart.transfer.rest.AccountServiceClient;
import com.javastart.transfer.rest.BillServiceClient;
import config.SpringH2DatabaseConfig;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TransferApplication.class, SpringH2DatabaseConfig.class})
public class TransferControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TransferRepository transferRepository;

    @MockBean
    private BillServiceClient billServiceClient;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;


    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private static final String REQUEST = "{\n" +
            "    \"senderBillId\": 3,\n" +
            "    \"recipientBillId\": 4,\n" +
            "    \"amount\": 3000.00\n" +
            "}";

    @Test
    public void createDeposit() throws Exception {


        Mockito.when(billServiceClient.getBillById(3l)).thenReturn(
                TransferUtil.createBillResponseDTO(
                        1l, 5000l, 3l, true, true));
        Mockito.when(billServiceClient.getBillById(4l)).thenReturn(
                TransferUtil.createBillResponseDTO(
                        1l, 5000l, 4l, true, true));
        Mockito.when(accountServiceClient.getAccountById(1l))
                .thenReturn(TransferUtil.createAccountResponseDTO(1l, Arrays.asList(3l, 4l, 10l),
                        "lory.cat@xyz", "Lori", "+123456"));
        MvcResult mvcResult = mockMvc.perform(post("/transfers")
                .content(REQUEST).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status()
                .isOk())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString();
        List<Transfer> transfers = transferRepository.getTransfersBySenderBillId(3l);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        TransferResponseDTO transferResponseDTO = objectMapper.readValue(body, TransferResponseDTO.class);

        Assertions.assertThat(transferResponseDTO.getAmount()).isEqualTo(transfers.get(0).getAmount());
    }

}
