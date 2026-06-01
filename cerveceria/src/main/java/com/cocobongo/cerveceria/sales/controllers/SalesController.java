package com.cocobongo.cerveceria.sales.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.cocobongo.cerveceria.common.dto.ApiResponse;
import com.cocobongo.cerveceria.sales.dto.RegisterSaleRequest;
import com.cocobongo.cerveceria.sales.dto.SaleResponse;
import com.cocobongo.cerveceria.sales.services.SalesService;
import com.cocobongo.cerveceria.users.entities.UserEntity;
 
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SalesController {
 
    private final SalesService saleService;
 
    /**
     * POST /api/v1/sales — RF-VEN-01..05 + RF-INV-03
     *
     * Registra una venta completa:
     *   - Calcula el total automáticamente
     *   - Descuenta stock por cada producto
     *   - Registra movimiento SALE en inventory_movement
     *   - Asocia opcionalmente un cliente
     *   - Valida que CREDIT requiera cliente
     *
     * Accesible por ADMIN y EMPLOYEE.
     * El usuario autenticado se inyecta automáticamente desde el JWT.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<SaleResponse>> registerSale(
            @Valid @RequestBody RegisterSaleRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {
 
        SaleResponse response = saleService.registerSale(request, currentUser);
        return ResponseEntity
                .status(201)
                .body(ApiResponse.ok("Venta registrada correctamente", response));
    }
 
    /**
     * GET /api/v1/sales/{id} — comprobante interno
     *
     * Retorna el detalle completo de una venta para mostrar como recibo.
     * No es factura legal — solo comprobante interno.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<SaleResponse>> getSale(
            @PathVariable Integer id) {
 
        return ResponseEntity.ok(ApiResponse.ok(saleService.findById(id)));
    }
}
