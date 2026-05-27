package com.cocobongo.cerveceria.clients.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final InstallmentRepository installmentRepository;
    private final UserRepository userRepository;
    private final SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> findAll() {
        return clientRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public ClientResponseDTO createClients(ClientRequestDTO request) {
        validateClientRequest(request);

        Boolean isActive = request.getIsActive();
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }

        ClientEntity client = ClientEntity.builder()
                .name(request.getName())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .balance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO)
                .isActive(isActive)
                .build();

        clientRepository.save(client);
        return toResponseDTO(client);
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO findById(Integer id) {
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + id));
        return toResponseDTO(client);
    }

    @Transactional
    public ClientResponseDTO updateClient(Integer id, ClientRequestDTO request) {
        validateClientRequest(request);

        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + id));

        client.setName(request.getName());
        client.setTelephone(request.getTelephone());
        client.setEmail(request.getEmail());
        if (request.getIsActive() != null) {
            client.setIsActive(request.getIsActive());
        }

        clientRepository.save(client);
        return toResponseDTO(client);
    }

    @Transactional
    public void deleteClient(Integer id) {
        clientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getClientsWithPendingBalances() {
        return clientRepository.findAll().stream()
                .filter(client -> client.getBalance() != null
                        && client.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public ClientResponseDTO updateClientBalance(Integer id, ClientRequestDTO request) {
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + id));

        if (request.getBalance() == null) {
            throw new BusinessException("El saldo es obligatorio para actualizar el balance");
        }
        if (request.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El saldo no puede ser negativo");
        }

        client.setBalance(request.getBalance());
        clientRepository.save(client);
        return toResponseDTO(client);
    }

    @Transactional
    public InstallmentResponseDTO addInstallment(Integer clientId, InstallmentRequestDTO request, Integer idUserLogged) {
        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + clientId));

        Integer userId = idUserLogged != null ? idUserLogged : (request.getIdUser() != null ? request.getIdUser().intValue() : null);
        if (userId == null) {
            throw new BusinessException("Usuario no especificado para la cuota");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        SaleEntity sale = null;
        if (request.getIdSale() != null) {
            sale = saleRepository.findById(request.getIdSale().intValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + request.getIdSale()));
        }

        BigDecimal amount = request.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto debe ser mayor que cero");
        }

        if (client.getBalance() == null) {
            client.setBalance(BigDecimal.ZERO);
        }

        if (amount.compareTo(client.getBalance()) > 0) {
            throw new BusinessException("El monto de la cuota no puede exceder el saldo del cliente");
        }

        InstallmentEntity.InstallmentEntityBuilder b = InstallmentEntity.builder()
            .client(client)
            .user(user)
            .sale(sale)
            .amount(amount)
            .notes(request.getNotes());

        if (request.getPaymentDate() != null) {
            b.paymentDate(request.getPaymentDate());
        }

        InstallmentEntity installment = b.build();

        installmentRepository.save(installment);

        client.setBalance(client.getBalance().subtract(amount));
        clientRepository.save(client);

        InstallmentResponseDTO resp = new InstallmentResponseDTO();
        resp.setIdInstallment(Long.valueOf(installment.getIdInstallment()));
        resp.setIdClient(Long.valueOf(client.getIdClient()));
        resp.setIdUser(Long.valueOf(user.getIdUser()));
        resp.setIdSale(sale != null ? Long.valueOf(sale.getIdSale()) : null);
        resp.setAmount(installment.getAmount());
        resp.setPaymentDate(installment.getPaymentDate());
        resp.setNotes(installment.getNotes());

        return resp;
    }

    private void validateClientRequest(ClientRequestDTO request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException("El nombre del cliente es obligatorio");
        }
    }

    private ClientResponseDTO toResponseDTO(ClientEntity client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setIdClient(Long.valueOf(client.getIdClient()));
        dto.setName(client.getName());
        dto.setTelephone(client.getTelephone());
        dto.setEmail(client.getEmail());
        dto.setBalance(client.getBalance());
        dto.setIsActive(client.getIsActive());
        return dto;
    }
}

