package academy.softserve.os.service;

import academy.softserve.os.exception.CreateAddressException;
import academy.softserve.os.exception.CreateEquipmentException;
import academy.softserve.os.model.Address;
import academy.softserve.os.model.Client;
import academy.softserve.os.model.Equipment;
import academy.softserve.os.repository.AddressRepository;
import academy.softserve.os.repository.ClientRepository;
import academy.softserve.os.repository.EquipmentRepository;
import academy.softserve.os.service.command.CreateAddressCommand;
import academy.softserve.os.service.command.CreateEquipmentCommand;
import liquibase.pro.packaged.E;
import liquibase.pro.packaged.O;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EquipmentServiceTest {
    private EquipmentRepository equipmentRepository;
    private ClientRepository clientRepository;
    private AddressRepository addressRepository;
    private EquipmentService equipmentService;

    private Address address;
    private Equipment equipment;
    private CreateEquipmentCommand command;
    private Client client;

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);
        addressRepository = mock(AddressRepository.class);
        equipmentRepository = mock(EquipmentRepository.class);
        equipmentService = new EquipmentService(equipmentRepository, clientRepository, addressRepository);
        address = Address.builder()
                .id(1L)
                .city("ХАРКІВ")
                .street("СУМСКА ВУЛ.")
                .house("БУД.23")
                .room("ВІТАЛЬНЯ")
                .build();
        client = Client.builder()
                .id(2L)
                .name("Client")
                .build();
        command = CreateEquipmentCommand.builder()
                .description("Description")
                .addressId(1L)
                .clientId(2L)
                .build();
        equipment = Equipment.builder()
                .id(1L)
                .description("Equipment")
                .client(client)
                .address(address)
                .build();
    }

    @Test
    void givenValidCreateEquipmentCommand_createEquipment_shouldReturnCreatedEquipment() {
        //given
        //when
        when(addressRepository.findById(any(Long.class))).thenReturn(Optional.of(address));
        when(clientRepository.findById(any(Long.class))).thenReturn(Optional.of(client));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);
        when(equipmentRepository.findByDescriptionAndClientAndAddress(
                equipment.getDescription(),
                equipment.getClient(),
                equipment.getAddress()
        )).thenReturn(Optional.empty());

        var equipment1 = equipmentService.createEquipment(command);
        //then
        assertEquals(equipment1.get().getDescription(), equipment.getDescription());
    }

    @Test
    void givenCreateEquipmentCommandWithNotAddressField_createEquipment_shouldReturnException() {
        //given
        //when
        when(addressRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        when(clientRepository.findById(any(Long.class))).thenReturn(Optional.of(client));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);
        when(equipmentRepository.findByDescriptionAndClientAndAddress(
                equipment.getDescription(),
                equipment.getClient(),
                equipment.getAddress()
        )).thenReturn(Optional.empty());
        //then
        CreateEquipmentException thrown = Assertions.assertThrows(CreateEquipmentException.class,
                () -> equipmentService.createEquipment(command));
        assertEquals("Create equipment error. Address ID not found.", thrown.getMessage());
    }

    @Test
    void givenCreateEquipmentCommandWithNullAddressField_createEquipment_shouldReturnException() {
        //given
        //when
        when(addressRepository.findById(any(Long.class))).thenThrow(new InvalidDataAccessApiUsageException(""));
        when(clientRepository.findById(any(Long.class))).thenReturn(Optional.of(client));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);
        when(equipmentRepository.findByDescriptionAndClientAndAddress(
                equipment.getDescription(),
                equipment.getClient(),
                equipment.getAddress()
        )).thenReturn(Optional.empty());
        //then
        CreateEquipmentException thrown = Assertions.assertThrows(CreateEquipmentException.class,
                () -> equipmentService.createEquipment(command));
        assertEquals("Create equipment error. Address ID error.", thrown.getMessage());
    }

    @Test
    void givenCreateEquipmentCommandWithNotClientField_createEquipment_shouldReturnException() {
        //given
        //when
        when(addressRepository.findById(any(Long.class))).thenReturn(Optional.of(address));
        when(clientRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);
        when(equipmentRepository.findByDescriptionAndClientAndAddress(
                equipment.getDescription(),
                equipment.getClient(),
                equipment.getAddress()
        )).thenReturn(Optional.empty());
        //then
        CreateEquipmentException thrown = Assertions.assertThrows(CreateEquipmentException.class,
                () -> equipmentService.createEquipment(command));
        assertEquals("Create equipment error. Client ID not found.", thrown.getMessage());
    }

    @Test
    void givenCreateEquipmentCommandWithNullClientField_createEquipment_shouldReturnException() {
        //given
        //when
        when(addressRepository.findById(any(Long.class))).thenReturn(Optional.of(address));
        when(clientRepository.findById(any(Long.class))).thenThrow(new InvalidDataAccessApiUsageException(""));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);
        when(equipmentRepository.findByDescriptionAndClientAndAddress(
                equipment.getDescription(),
                equipment.getClient(),
                equipment.getAddress()
        )).thenReturn(Optional.empty());
        //then
        CreateEquipmentException thrown = Assertions.assertThrows(CreateEquipmentException.class,
                () -> equipmentService.createEquipment(command));
        assertEquals("Create equipment error. Client ID error.", thrown.getMessage());
    }

    @Test
    void givenCreateEquipmentCommandWithNullDescriptionField_createEquipment_shouldReturnException() {
        //given
        command.setDescription(null);
        //when
        when(addressRepository.findById(any(Long.class))).thenReturn(Optional.of(address));
        when(clientRepository.findById(any(Long.class))).thenReturn(Optional.of(client));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);
        when(equipmentRepository.findByDescriptionAndClientAndAddress(
                equipment.getDescription(),
                equipment.getClient(),
                equipment.getAddress()
        )).thenReturn(Optional.empty());
        //then
        CreateEquipmentException thrown = Assertions.assertThrows(CreateEquipmentException.class,
                () -> equipmentService.createEquipment(command));
        assertEquals("Create equipment error. Description not present.", thrown.getMessage());
    }

    @Test
    void givenCreateEquipmentCommandWithEmptyDescriptionField_createEquipment_shouldReturnException() {
        //given
        command.setDescription(" ");
        //when
        when(addressRepository.findById(any(Long.class))).thenReturn(Optional.of(address));
        when(clientRepository.findById(any(Long.class))).thenReturn(Optional.of(client));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);
        when(equipmentRepository.findByDescriptionAndClientAndAddress(
                equipment.getDescription(),
                equipment.getClient(),
                equipment.getAddress()
        )).thenReturn(Optional.empty());
        //then
        CreateEquipmentException thrown = Assertions.assertThrows(CreateEquipmentException.class,
                () -> equipmentService.createEquipment(command));
        assertEquals("Create equipment error. Description not present.", thrown.getMessage());
    }

    @Test
    void givenDescriptionNullAndEmpty_findEquipment_returnListAllEquipment() {
        //given
        var equipment1 = Equipment.builder()
                .id(1L)
                .description("Condition 2")
                .build();
        var equipment2 = Equipment.builder()
                .id(1L)
                .description("Watercoller")
                .build();
        var equipment3 = Equipment.builder()
                .id(1L)
                .description("Condition 1")
                .build();
        var equipments = List.of(equipment1, equipment2, equipment3);
        //when
        when(equipmentRepository.findAll()).thenReturn(equipments);
        var result = equipmentService.findEquipment(null);
        assertEquals(equipments.size(), result.size());
        result = equipmentService.findEquipment("");
        assertEquals(equipments.size(), result.size());
    }

    static Stream<Arguments> initParameters() {
        return Stream.of(
                Arguments.of("con", 2),
                Arguments.of("1", 1),
                Arguments.of("HVAC", 0)
        );
    }

    @ParameterizedTest
    @MethodSource("initParameters")
    void givenFindEquipmentByDescriptionExample_findEquipment_returnListEquipment(String testExample, Integer size) throws Exception{
        //given
        var equipment1 = Equipment.builder()
                .id(1L)
                .description("Condition 2")
                .build();
        var equipment2 = Equipment.builder()
                .id(1L)
                .description("Watercoller")
                .build();
        var equipment3 = Equipment.builder()
                .id(1L)
                .description("Condition 1")
                .build();
        var equipments = List.of(equipment1, equipment2, equipment3);

        var equipmentTest = Equipment.builder()
                .description(testExample).build();
        var example = Example.of(equipmentTest);

        var answer = new Answer<List<Equipment>>() {
            @Override
            public List<Equipment> answer(InvocationOnMock invocation) throws Throwable {
                var ex = invocation.getArgument(0, Example.class);
                var testString = ((Equipment) ex.getProbe()).getDescription().toLowerCase();
                var reslist = equipments
                        .stream().filter(e -> e.getDescription()
                                .toLowerCase().contains(testString))
                        .collect(Collectors.toList());
                return reslist;
            }
        };
        //when
//        when(equipmentRepository.findAll()).thenReturn(equipments);
        when(equipmentRepository.findAll(example)).then(answer);

        var result = equipmentService.findEquipment(equipmentTest.getDescription());
        assertEquals(size, result.size());
    }
}