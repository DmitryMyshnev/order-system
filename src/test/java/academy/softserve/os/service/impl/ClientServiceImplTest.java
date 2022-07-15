package academy.softserve.os.service.impl;

import academy.softserve.os.model.Client;
import academy.softserve.os.repository.ClientRepository;
import academy.softserve.os.service.ClientService;
import academy.softserve.os.service.command.CreateClientCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

class ClientServiceImplTest {

    private final ClientRepository clientRepository = Mockito.mock(ClientRepository.class);
    private final ClientService  clientService = new ClientServiceImpl(clientRepository);

    @Test
    void givenValidCreateClientCommand_createClient_shouldReturnCreatedClient(){
        var createClientCommand = new CreateClientCommand("Pol");
        
        when(clientRepository.save(any(Client.class))).then(returnsFirstArg());
        var client = clientService.createClient(createClientCommand);
        
        assertEquals("Pol", client.getName());
    }
}