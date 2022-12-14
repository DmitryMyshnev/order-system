package academy.softserve.os.api;

import academy.softserve.os.api.dto.command.CreateAddressCommandDTO;
import academy.softserve.os.mapper.AddressMapper;
import academy.softserve.os.exception.CreateAddressException;
import academy.softserve.os.model.Address;
import academy.softserve.os.service.AddressService;
import academy.softserve.os.service.command.CreateAddressCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AddressControllerCreateAddressTest {
    private MockMvc mockMvc;

    @MockBean
    private AddressService service;

    private final ObjectMapper mapper = new ObjectMapper();
    private CreateAddressCommandDTO commandDTO;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        commandDTO = CreateAddressCommandDTO.builder()
                .city("??????????????")
                .street("??????????????")
                .house("10")
                .room("??????????")
                .build();
    }

    @WithMockUser(value = "someuser", roles = "WORKER")
    @Test
    void givenValidCreateAddressCommandDTO_createAddress_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/admin/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commandDTO)))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(value = "someuser", roles = "ADMIN")
    @Test
    void givenValidCreateAddressCommandDTO_createAddress_shouldCreateNewAddressAndReturnOkResponse() throws Exception {

        var address = Address.builder()
                .id(1L)
                .city("??????????????")
                .street("??????????????")
                .house("10")
                .room("??????????")
                .build();

        when(service.createAddress(any(CreateAddressCommand.class))).thenReturn(address);

        mockMvc.perform(post("/api/admin/address")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(commandDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.city").value("??????????????"))
                .andExpect(jsonPath("$.street").value("??????????????"))
                .andExpect(jsonPath("$.house").value("10"))
                .andExpect(jsonPath("$.room").value("??????????"));

    }

    @WithMockUser(value = "someuser", roles = "ADMIN")
    @Test
    void givenCreateAddressCommandDTOWithNullFieldCity_createAddress_shouldReturnErrorMessageBecauseFieldCityCannotBeNull() throws Exception {

        commandDTO.setCity("");
        String error = "Error";

        when(service.createAddress(any(CreateAddressCommand.class))).thenThrow(new CreateAddressException(error));

        mockMvc.perform(post("/api/admin/address")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(commandDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error created address. " + error));
    }

    @WithMockUser(value = "someuser", roles = "ADMIN")
    @Test
    void givenCreateAddressCommandDTOWithNullFieldStreet_createAddress_shouldReturnErrorMessageBecauseFieldStreetCannotBeNull() throws Exception {

        commandDTO.setStreet("");

        when(service.createAddress(any(CreateAddressCommand.class))).thenThrow(CreateAddressException.class);

        mockMvc.perform(post("/api/admin/address")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(commandDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error created address. null"));
    }

    @WithMockUser(value = "someuser", roles = "ADMIN")
    @Test
    void givenCreateAddressCommandDTOWithNullFieldHouse_createAddress_shouldReturnErrorMessageBecauseFieldHouseCannotBeNull() throws Exception {

        commandDTO.setHouse("");

        when(service.createAddress(any(CreateAddressCommand.class))).thenThrow(CreateAddressException.class);

        mockMvc.perform(post("/api/admin/address")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(commandDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error created address. null"));
    }
}