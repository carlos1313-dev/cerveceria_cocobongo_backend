package com.cocobongo.cerveceria.sales.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.branches.repositories.BranchesRepository;
import com.cocobongo.cerveceria.clients.entities.ClientEntity;
import com.cocobongo.cerveceria.clients.repositories.ClientRepository;
import com.cocobongo.cerveceria.common.exception.BusinessException;
import com.cocobongo.cerveceria.common.exception.ResourceNotFoundException;
import com.cocobongo.cerveceria.inventory.entities.InventoryMovementEntity;
import com.cocobongo.cerveceria.inventory.entities.ProductEntity;
import com.cocobongo.cerveceria.inventory.repositories.InventoryMovementRepository;
import com.cocobongo.cerveceria.inventory.repositories.InventoryRepository;
import com.cocobongo.cerveceria.inventory.repositories.ProductRepository;
import com.cocobongo.cerveceria.inventory.services.InventoryService;
import com.cocobongo.cerveceria.sales.dto.RegisterSaleRequest;
import com.cocobongo.cerveceria.sales.dto.SaleDetailResponse;
import com.cocobongo.cerveceria.sales.dto.SaleItemRequest;
import com.cocobongo.cerveceria.sales.dto.SaleResponse;
import com.cocobongo.cerveceria.sales.entities.PaymentType;
import com.cocobongo.cerveceria.sales.entities.SaleDetailEntity;
import com.cocobongo.cerveceria.sales.entities.SaleEntity;
import com.cocobongo.cerveceria.sales.entities.SaleStatus;
import com.cocobongo.cerveceria.sales.repositories.SaleRepository;
import com.cocobongo.cerveceria.users.entities.UserEntity;

import lombok.RequiredArgsConstructor;
 
@Service
@RequiredArgsConstructor
public class SalesService {
    private final SaleRepository saleRepository;
    private final ClientRepository clientRepository;
    private final BranchesRepository branchRepository;
    //private final InventoryRepository inventoryStockRepository;
    //private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryService inventoryService;
    private final ProductRepository  productLookupRepository;

    // ── RF-VEN-01..05 + RF-INV-03: Registrar venta ───────────────────────────
    //
    // Flujo en una sola @Transactional — rollback total si cualquier paso falla:
    //
    //   FASE 1 — Validaciones y construcción (sin escrituras a BD)
    //     1a. Validar CREDIT → cliente obligatorio
    //     1b. Resolver cliente y sucursal
    //     1c. Por cada item: validar producto activo, validar stock suficiente,
    //         tomar snapshot del precio, construir SaleDetailEntity
    //
    //   FASE 2 — Persistencia
    //     2a. Guardar SaleEntity + detalles (cascade) → obtener idSale generado
    //     2b. Descontar stock de forma atómica (UPDATE ... WHERE stock >= quantity)
    //     2c. Registrar movimiento SALE con id_reference = idSale ya conocido
    //
    // El reorden (guardar venta ANTES de descontar stock) es intencional:
    //   - Permite usar idSale como id_reference en inventory_movement.
    //   - Todo ocurre en la misma transacción: un fallo en 2b o 2c
    //     hace rollback de 2a también.
 
    @Transactional
    public SaleResponse registerSale(RegisterSaleRequest request,
                                     UserEntity currentUser) {
 
        // ── FASE 1: Validaciones y construcción (sin escrituras) ──────────────
 
        // 1a. CREDIT requiere cliente
        if (request.getPaymentType() == PaymentType.CREDIT
                && request.getClientId() == null) {
            throw new BusinessException(
                    "Las ventas a crédito requieren un cliente asociado");
        }
 
        // 1b. Resolver cliente
        ClientEntity client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cliente no encontrado con id: " + request.getClientId()));
            if (!client.getIsActive()) {
                throw new BusinessException(
                        "El cliente está inactivo y no puede realizar compras");
            }
        }
 
        // 1b. Resolver sucursal del usuario autenticado
        if (currentUser.getIdBranch() == null) {
            throw new BusinessException("El usuario no tiene una sucursal asignada");
        }
        BranchEntity branch = branchRepository.findById(currentUser.getIdBranch())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal no encontrada con id: " + currentUser.getIdBranch()));
 
        // 1c. Validar cada item y construir detalles — solo lecturas aquí
        //     Si cualquier producto falla, se lanza excepción antes de escribir nada
        List<SaleDetailEntity> details   = new ArrayList<>();
        BigDecimal             totalSale = BigDecimal.ZERO;
 
        for (SaleItemRequest item : request.getItems()) {
 
            // Verificar que el producto existe y está activo
            ProductEntity product = productLookupRepository
                    .findActiveById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado o inactivo con id: "
                                    + item.getProductId()));
 
            // Verificar stock suficiente — lectura sin descuento todavía
            inventoryService
                    .findByProductAndBranch(item.getProductId(), currentUser.getIdBranch())
                    .ifPresentOrElse(
                            inv -> {
                                if (inv.getStock() < item.getQuantity()) {
                                    throw new BusinessException(
                                            "Stock insuficiente para '"
                                                    + product.getName()
                                                    + "'. Disponible: " + inv.getStock()
                                                    + ", solicitado: " + item.getQuantity());
                                }
                            },
                            () -> { throw new ResourceNotFoundException(
                                    "El producto '" + product.getName()
                                            + "' no tiene inventario en esta sucursal"); }
                    );
 
            // Snapshot del precio — se congela aquí para que los reportes
            // históricos sean correctos aunque el precio cambie en el futuro
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal  = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalSale = totalSale.add(subtotal);
 
            details.add(SaleDetailEntity.builder()
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build());
        }
 
        // ── FASE 2: Persistencia ──────────────────────────────────────────────
 
        // 2a. Guardar venta + detalles (cascade ALL en SaleEntity.details)
        //     Después de este save(), saved.getIdSale() ya tiene el valor generado
        SaleStatus initialStatus = (request.getPaymentType() == PaymentType.CREDIT)
                ? SaleStatus.PENDING
                : SaleStatus.COMPLETED;
 
        SaleEntity sale = SaleEntity.builder()
                .branch(branch)
                .user(currentUser)
                .client(client)
                .paymentType(request.getPaymentType())
                .status(initialStatus)
                .total(totalSale)
                .build();
 
        details.forEach(sale::addDetail);
 
        SaleEntity saved = saleRepository.save(sale);
 
        // 2b + 2c. Descontar stock y registrar movimiento — ahora sí con idSale
        for (SaleItemRequest item : request.getItems()) {
 
            // Descuento atómico con UPDATE directo
            // WHERE stock >= quantity actúa como segunda línea de defensa:
            // si en este instante otro usuario vendió el mismo producto y
            // dejó stock insuficiente (race condition), retorna 0 filas
            // y @Transactional hace rollback de todo lo anterior
            int updated = inventoryStockRepository.decrementStock(
                    item.getProductId(),
                    currentUser.getIdBranch(),
                    item.getQuantity()
            );
 
            if (updated == 0) {
                throw new BusinessException(
                        "No se pudo descontar stock del producto id: "
                                + item.getProductId()
                                + ". Es posible que otro usuario registrara una venta "
                                + "al mismo tiempo. Intente nuevamente.");
            }
 
            // Registrar movimiento SALE con referencia a la venta recién creada
            inventoryMovementRepository.save(
                    InventoryMovementEntity.builder()
                            .idProduct(item.getProductId())
                            .idBranch(currentUser.getIdBranch())
                            .idUser(currentUser.getIdUser())
                            .type("OUT")
                            .reason("SALE")
                            .quantity(item.getQuantity())
                            .idReference(saved.getIdSale())  // ← trazabilidad completa
                            .build()
            );
        }
 
        return toResponse(saved);
    }
 
    // ── GET /api/v1/sales/{id} — comprobante interno ──────────────────────────
 
    @Transactional(readOnly = true)
    public SaleResponse findById(Integer id) {
        SaleEntity sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta no encontrada con id: " + id));
        return toResponse(sale);
    }
 
    // ── Mapper entidad → DTO ───────────────────────────────────────────────────
 
    private SaleResponse toResponse(SaleEntity sale) {
        SaleResponse response = new SaleResponse();
        response.setIdSale(sale.getIdSale());
        response.setSaleDate(sale.getSaleDate());
        response.setPaymentType(sale.getPaymentType());
        response.setStatus(sale.getStatus());
        response.setTotal(sale.getTotal());
 
        if (sale.getBranch() != null) {
            response.setBranchName(sale.getBranch().getName());
        }
        if (sale.getUser() != null) {
            response.setRegisteredBy(sale.getUser().getName());
        }
        if (sale.getClient() != null) {
            response.setClientName(sale.getClient().getName());
        }
 
        List<SaleDetailResponse> detailResponses = sale.getDetails().stream()
                .map(d -> {
                    SaleDetailResponse dr = new SaleDetailResponse();
                    dr.setIdSaleDetail(d.getIdSaleDetail());
                    dr.setProductId(d.getProduct().getIdProduct());
                    dr.setProductName(d.getProduct().getName());
                    dr.setQuantity(d.getQuantity());
                    dr.setUnitPrice(d.getUnitPrice());
                    dr.setSubtotal(d.getSubtotal());
                    return dr;
                })
                .toList();
 
        response.setDetails(detailResponses);
        return response;
    }
}

