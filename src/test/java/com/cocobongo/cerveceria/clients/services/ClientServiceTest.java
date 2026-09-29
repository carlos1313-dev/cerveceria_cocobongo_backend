package com.cocobongo.cerveceria.clients.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cocobongo.cerveceria.clients.dto.ClientRequestDTO;
import com.cocobongo.cerveceria.clients.dto.ClientResponseDTO;
import com.cocobongo.cerveceria.clients.dto.InstallmentRequestDTO;
import com.cocobongo.cerveceria.clients.dto.InstallmentResponseDTO;
import com.cocobongo.cerveceria.clients.entities.ClientEntity;
import com.cocobongo.cerveceria.clients.entities.InstallmentEntity;
import com.cocobongo.cerveceria.clients.repositories.ClientRepository;
import com.cocobongo.cerveceria.clients.repositories.InstallmentRepository;
import com.cocobongo.cerveceria.common.exception.BusinessException;
import com.cocobongo.cerveceria.common.exception.ResourceNotFoundException;
import com.cocobongo.cerveceria.sales.entities.SaleEntity;
import com.cocobongo.cerveceria.sales.repositories.SaleRepository;
import com.cocobongo.cerveceria.users.entities.UserEntity;
import com.cocobongo.cerveceria.users.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void findAll_shouldReturnMappedClients() {
        ClientEntity client = clientEntity(1, "Ana", "0412-1234567", "ana@test.com", new BigDecimal("150.00"), true);
        when(clientRepository.findAll()).thenReturn(List.of(client));

        List<ClientResponseDTO> result = clientService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdClient()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Ana");
        assertThat(result.get(0).getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void findById_whenClientExists_shouldReturnDto() {
        ClientEntity client = clientEntity(2, "Luis", "0414-1111111", "luis@test.com", new BigDecimal("320.50"), true);
        when(clientRepository.findById(2)).thenReturn(Optional.of(client));

        ClientResponseDTO result = clientService.findById(2);

        assertThat(result.getName()).isEqualTo("Luis");
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("320.50"));
    }

    @Test
    void findById_whenClientDoesNotExist_shouldThrowResourceNotFoundException() {
        when(clientRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado");
    }

    @Test
    void validateClientActive_whenClientIsInactive_shouldThrowBusinessException() {
        ClientEntity client = clientEntity(3, "Pedro", null, "pedro@test.com", BigDecimal.ZERO, false);
        when(clientRepository.findById(3)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> clientService.validateClientActive(3))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    void createClients_whenBalanceIsNull_shouldSetZero() {
        ClientRequestDTO request = new ClientRequestDTO();
        request.setName("María");
        request.setEmail("maria@test.com");
        request.setBalance(null);
        request.setIsActive(true);

        when(clientRepository.save(any(ClientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientResponseDTO result = clientService.createClients(request);

        assertThat(result.getName()).isEqualTo("María");
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(clientRepository).save(any(ClientEntity.class));
    }

    @Test
    void createClients_whenNameIsBlank_shouldThrowBusinessException() {
        ClientRequestDTO request = new ClientRequestDTO();
        request.setName("   ");

        assertThatThrownBy(() -> clientService.createClients(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nombre del cliente");
    }

    @Test
    void updateClientBalance_shouldAddAmountAndPersistNewBalance() {
        ClientEntity client = clientEntity(4, "Sofía", null, "sofia@test.com", new BigDecimal("100.00"), true);
        when(clientRepository.findById(4)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(ClientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientResponseDTO result = clientService.updateClientBalance(4, new BigDecimal("50.00"));

        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
        verify(clientRepository).save(any(ClientEntity.class));
    }

    @Test
    void updateClientBalance_whenResultWouldBeNegative_shouldThrowBusinessException() {
        ClientEntity client = clientEntity(5, "José", null, "jose@test.com", new BigDecimal("40.00"), true);
        when(clientRepository.findById(5)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> clientService.updateClientBalance(5, new BigDecimal("-50.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("saldo resultante");
    }

    @Test
    void addInstallment_whenValid_shouldReduceClientBalanceAndReturnResponse() {
        ClientEntity client = clientEntity(6, "Laura", null, "laura@test.com", new BigDecimal("500.00"), true);
        UserEntity user = userEntity(10, "Cajero");
        SaleEntity sale = saleEntity(20);

        InstallmentRequestDTO request = new InstallmentRequestDTO();
        request.setIdClient(6);
        request.setIdUser(10);
        request.setIdSale(20);
        request.setAmount(new BigDecimal("150.00"));
        request.setPaymentDate(LocalDateTime.now());
        request.setNotes("Pago parcial");

        when(clientRepository.findById(6)).thenReturn(Optional.of(client));
        when(userRepository.findById(10)).thenReturn(Optional.of(user));
        when(saleRepository.findById(20)).thenReturn(Optional.of(sale));
        when(installmentRepository.save(any(InstallmentEntity.class))).thenAnswer(invocation -> {
            InstallmentEntity entity = invocation.getArgument(0);
            entity.setIdInstallment(99);
            return entity;
        });
        when(clientRepository.save(any(ClientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InstallmentResponseDTO result = clientService.addInstallment(6, request, null);

        assertThat(result.getIdClient()).isEqualTo(6);
        assertThat(result.getIdUser()).isEqualTo(10);
        assertThat(result.getIdSale()).isEqualTo(20);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(client.getBalance()).isEqualByComparingTo(new BigDecimal("350.00"));
        verify(installmentRepository).save(any(InstallmentEntity.class));
        verify(clientRepository).save(any(ClientEntity.class));
    }

    @Test
    void addInstallment_whenAmountExceedsBalance_shouldThrowBusinessException() {
        ClientEntity client = clientEntity(7, "Nora", null, "nora@test.com", new BigDecimal("80.00"), true);
        UserEntity user = userEntity(11, "Supervisor");

        InstallmentRequestDTO request = new InstallmentRequestDTO();
        request.setIdClient(7);
        request.setIdUser(11);
        request.setAmount(new BigDecimal("90.00"));
        request.setPaymentDate(LocalDateTime.now());

        when(clientRepository.findById(7)).thenReturn(Optional.of(client));
        when(userRepository.findById(11)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> clientService.addInstallment(7, request, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no puede exceder el saldo");
    }

    private ClientEntity clientEntity(Integer id, String name, String telephone, String email, BigDecimal balance, boolean active) {
        return ClientEntity.builder()
                .idClient(id)
                .name(name)
                .telephone(telephone)
                .email(email)
                .balance(balance)
                .isActive(active)
                .build();
    }

    private UserEntity userEntity(Integer id, String name) {
        UserEntity user = new UserEntity();
        user.setIdUser(id);
        user.setName(name);
        user.setEmail(name.toLowerCase() + "@test.com");
        user.setPasswordHash("hash");
        return user;
    }

    private SaleEntity saleEntity(Integer id) {
        SaleEntity sale = new SaleEntity();
        sale.setIdSale(id);
        sale.setTotal(new BigDecimal("750.00"));
        return sale;
    }
}
