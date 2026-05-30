package com.cocobongo.cerveceria.inventory.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cocobongo.cerveceria.common.dto.ApiResponse;
import com.cocobongo.cerveceria.inventory.dto.InventoryMovementRequestDTO;
import com.cocobongo.cerveceria.inventory.dto.InventoryMovementResponseDTO;
import com.cocobongo.cerveceria.inventory.dto.InventoryResponseDTO;
import com.cocobongo.cerveceria.inventory.dto.ProductCreatedResponseDTO;
import com.cocobongo.cerveceria.inventory.dto.ProductRequestDTO;
import com.cocobongo.cerveceria.inventory.dto.ProductResponseDTO;
import com.cocobongo.cerveceria.inventory.dto.ProviderRequestDTO;
import com.cocobongo.cerveceria.inventory.dto.ProviderResponseDTO;
import com.cocobongo.cerveceria.inventory.services.InventoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // PROVIDERS — /api/v1/providers
    // RF-INV-02 — Solo ADMIN

    @GetMapping("/providers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProviderResponseDTO>> findAllProviders(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(inventoryService.findAllProviders(search));
    }

    @GetMapping("/providers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderResponseDTO> findProviderById(@PathVariable Integer id) {
        return ResponseEntity.ok(inventoryService.findProviderById(id));
    }

    @PostMapping("/providers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderResponseDTO> createProvider(
            @Valid @RequestBody ProviderRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createProvider(request));
    }

    @PutMapping("/providers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderResponseDTO> updateProvider(
            @PathVariable Integer id,
            @Valid @RequestBody ProviderRequestDTO request) {
        return ResponseEntity.ok(inventoryService.updateProvider(id, request));
    }

    @DeleteMapping("/providers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProvider(@PathVariable Integer id) {
        inventoryService.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }

    // INVENTORY — /api/v1/inventory
    // RF-INV-05

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<InventoryResponseDTO>> findInventoryByBranch(
            @RequestParam Integer idBranch,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(inventoryService.findByBranch(idBranch, search));
    }

    @GetMapping("/inventory/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryResponseDTO>> findInventoryByProduct(
            @PathVariable("productId") Integer idProduct) {
        return ResponseEntity.ok(inventoryService.findByProduct(idProduct));
    }

    // INVENTORY MOVEMENT — /api/v1/inventory/entries y /movements
    // RF-INV-04

    @PostMapping("/inventory/entries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryMovementResponseDTO> registerEntry(
            @Valid @RequestBody InventoryMovementRequestDTO request,
            @AuthenticationPrincipal Integer idUserLogged) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.registerEntry(request, idUserLogged));
    }

    @GetMapping("/inventory/movements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryMovementResponseDTO>> findMovements(
            @RequestParam(required = false) Integer idProduct,
            @RequestParam(required = false) Integer idBranch,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(
                inventoryService.findMovements(idProduct, idBranch, type, reason, from, to));
    }

    // PRODUCT - /api/v1/inventory/products
    // RF-INV-01, RF-INV-02

    @GetMapping("/inventory/products")
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getProductByIdAndBranch(
            @RequestParam String value,
            @RequestParam Integer idBranch, Pageable pageable) {
        try {
            Integer id = Integer.valueOf(value);

            // si era número
            return ResponseEntity.ok(ApiResponse.ok(inventoryService.findProductByIdAndBranch(id, idBranch, pageable)));

        } catch (NumberFormatException e) {

            // si era texto
            return ResponseEntity
                    .ok(ApiResponse.ok(inventoryService.findProductByNameAndBranch(value, idBranch, pageable)));
        }
    }

    @GetMapping("/inventory/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.findProductById(id)));
    }

    @PostMapping("/inventory/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductCreatedResponseDTO>> createProduct(
            @Valid @RequestBody ProductRequestDTO request) {
        // Devuelve el producto creado junto con la sucursal y el inventario inicial.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(inventoryService.createProduct(request)));
    }

    @PutMapping("/inventory/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(@PathVariable Integer id,
            @RequestBody ProductRequestDTO request) {
                return ResponseEntity.ok(ApiResponse.ok(inventoryService.updateProduct(request, id)));
    }

}