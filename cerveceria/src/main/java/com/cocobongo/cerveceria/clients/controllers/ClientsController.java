package com.cocobongo.cerveceria.clients.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cocobongo.cerveceria.clients.dto.ClientRequestDTO;
import com.cocobongo.cerveceria.clients.dto.ClientResponseDTO;
import com.cocobongo.cerveceria.clients.dto.InstallmentRequestDTO;
import com.cocobongo.cerveceria.clients.dto.InstallmentResponseDTO;
import com.cocobongo.cerveceria.clients.services.ClientService;
import com.cocobongo.cerveceria.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("api/v1/clients")
@RequiredArgsConstructor
public class ClientsController {

    private final ClientService clientService;

    // CLIENTS — GET /api/v1/clients
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClientResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(clientService.findAll()));
    }

    // CLIENTS — POST /api/v1/clients
    // RF-CLI-01
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponseDTO>> create(
        @Valid @RequestBody ClientRequestDTO request) {

        ClientResponseDTO newClient = clientService.createClients(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Cliente creado", newClient));
    }

    // CLIENTS — GET /api/v1/clients/{id}
    // RF-CLI-01
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponseDTO>> getById(@PathVariable Integer id) {
        ClientResponseDTO client = clientService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(client));
    }

    // CLIENTS — PUT /api/v1/clients/{id}
    // RF-CLI-01
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponseDTO>> update(
        @PathVariable Integer id, @Valid @RequestBody ClientRequestDTO request) {

        ClientResponseDTO updatedClient = clientService.updateClient(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cliente actualizado", updatedClient));
    }

    // CLIENTS — DELETE /api/v1/clients/{id}
    // RF-CLI-01
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.ok("Cliente eliminado"));
    }

    // CLIENTS — GET /api/v1/clients/balance
    // RF-CLI-03
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<List<ClientResponseDTO>>> getClientsWithPendingBalances() {
        List<ClientResponseDTO> clientsWithBalance = clientService.getClientsWithPendingBalances();
        return ResponseEntity.ok(ApiResponse.ok(clientsWithBalance));
    }

    // CLIENTS — PUT /api/v1/clients/{id}/installments
    // RF-CLI-06
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @PostMapping("/{id}/installments")
    public ResponseEntity<ApiResponse<InstallmentResponseDTO>> addInstallment(
        @PathVariable Integer id,
        @Valid @RequestBody InstallmentRequestDTO request,
        @AuthenticationPrincipal Integer idUserLogged) {

        // Ensure path id matches request idClient
        request.setIdClient(Long.valueOf(id));
        InstallmentResponseDTO installment = clientService.addInstallment(id, request, idUserLogged);
        return ResponseEntity.status(201).body(ApiResponse.ok("Cuota agregada", installment));
    }

    // CLIENTS — GET /api/v1/clients/{id}/accountStatus
    // RF-CLI-07
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping("/{id}/accountStatus")
    public ResponseEntity<ApiResponse<BigDecimal>> getAccountStatus(@PathVariable Integer id) {
        ClientResponseDTO client = clientService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(client.getAccountStatus()));
    }
}