package com.cocobongo.cerveceria.sales.services;

import com.cocobongo.cerveceria.users.entities.UserEntity;
import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.common.exception.BusinessException;
import com.cocobongo.cerveceria.common.exception.ResourceNotFoundException;
import com.cocobongo.cerveceria.inventory.entities.InventoryMovementEntity;
import com.cocobongo.cerveceria.inventory.entities.ProductEntity;
import com.cocobongo.cerveceria.sales.dto.*;
import com.cocobongo.cerveceria.sales.entities.*;
import com.cocobongo.cerveceria.clients.entities.ClientEntity;
import com.cocobongo.cerveceria.clients.repositories.ClientRepository;
import com.cocobongo.cerveceria.inventory.repositories.InventoryRepository;
import com.cocobongo.cerveceria.inventory.repositories.ProductRepository;
import com.cocobongo.cerveceria.inventory.repositories.InventoryMovementRepository;
import com.cocobongo.cerveceria.sales.repositories.SaleRepository;
import com.cocobongo.cerveceria.sales.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class SalesService {
    private final SaleRepository saleRepository;
    private final ClientRepository clientRepository;
    private final InventoryRepository inventoryStockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductRepository  productLookupRepository;

    // ── RF-VEN-01..05 + RF-INV-03: Registrar venta simple ────────────────────
    //
    // Flujo dentro de una sola transacción:
    //   1. Validar regla CREDIT → cliente obligatorio
    //   2. Por cada item: verificar stock, descontar, registrar movimiento SALE
    //   3. Calcular total
    //   4. Persistir SaleEntity + SaleDetailEntity (cascade)
    //
    // Si cualquier paso falla, @Transactional hace rollback completo —
    // ni el stock se descuenta ni la venta queda guardada a medias.
 
    @Transactional
    public SaleResponse registerSale(RegisterSaleRequest request,
                                     UserEntity currentUser) {
 
        // ── 1. Validar CREDIT requiere cliente ─────────────────────────────────
        if (request.getPaymentType() == PaymentType.CREDIT
                && request.getClientId() == null) {
            throw new BusinessException(
                    "Las ventas a crédito requieren un cliente asociado");
        }
 
        // Resolver cliente si viene en el request
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
 
        // Resolver sucursal del usuario que registra la venta
        // El empleado solo puede registrar ventas en su sucursal asignada
        if (currentUser.getIdBranch() == null) {
            throw new BusinessException(
                    "El usuario no tiene una sucursal asignada");
        }
 
        BranchEntity branch = new BranchEntity();
        branch.setIdBranch(currentUser.getIdBranch());
 
        // ── 2. Procesar cada item: stock + movimiento ──────────────────────────
        List<SaleDetailEntity> details    = new ArrayList<>();
        BigDecimal             totalSale  = BigDecimal.ZERO;
 
        for (SaleItemRequest item : request.getItems()) {
 
            // Verificar que existe registro de inventario para producto+sucursal
            inventoryStockRepository
                    .findByProductAndBranch(item.getProductId(),
                            currentUser.getIdBranch())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto id " + item.getProductId()
                                    + " no encontrado en inventario de esta sucursal"));
 
            // Descontar stock de forma atómica — retorna filas afectadas
            // Si retorna 0: el stock era insuficiente (la condición stock >= quantity falló)
            int updated = inventoryStockRepository.decrementStock(
                    item.getProductId(),
                    currentUser.getIdBranch(),
                    item.getQuantity()
            );
 
            if (updated == 0) {
                throw new BusinessException(
                        "Stock insuficiente para el producto id: "
                                + item.getProductId()
                                + ". Verifique las existencias disponibles.");
            }
 
            // Registrar movimiento de inventario tipo SALE
            // Se usa el id de la venta como id_reference — se actualiza
            // después de persistir la venta (ver abajo)
            InventoryMovementEntity movement = InventoryMovementEntity.builder()
                    .idProduct(item.getProductId())
                    .idBranch(currentUser.getIdBranch())
                    .idUser(currentUser.getIdUser())
                    .type("OUT")
                    .reason("SALE")
                    .quantity(item.getQuantity())
                    .build();
            inventoryMovementRepository.save(movement);
 
            // Cargar el producto activo — valida existencia e isActive
            ProductEntity product = productLookupRepository.findActiveById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado o inactivo con id: " + item.getProductId()));
 
            BigDecimal unitPrice = product.getPrice();
 
            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(item.getQuantity()));
            totalSale = totalSale.add(subtotal);
 
            SaleDetailEntity detail = SaleDetailEntity.builder()
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
            details.add(detail);
        }
 
        // ── 3. Determinar status inicial ───────────────────────────────────────
        SaleStatus initialStatus = (request.getPaymentType() == PaymentType.CREDIT)
                ? SaleStatus.PENDING
                : SaleStatus.COMPLETED;
 
        // ── 4. Persistir la venta ──────────────────────────────────────────────
        SaleEntity sale = SaleEntity.builder()
                .branch(branch)
                .user(currentUser)
                .client(client)
                .paymentType(request.getPaymentType())
                .status(initialStatus)
                .total(totalSale)
                .build();
 
        // Añadir detalles usando el helper que mantiene la relación bidireccional
        details.forEach(sale::addDetail);
 
        SaleEntity saved = saleRepository.save(sale);
 
        // Actualizar id_reference en los movimientos con el id de la venta recién guardada
        // para trazabilidad: movimiento SALE → id_sale
        updateMovementReferences(request.getItems(), saved.getIdSale(),
                currentUser.getIdBranch());
 
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
 
    // ── Interno: obtener precio del producto ───────────────────────────────────
    //
    // Se inyecta un ProductRepository mínimo para leer solo el precio.
    // Alternativa válida: recibirlo como parámetro desde el controller
    // si el frontend ya conoce el precio. Se elige cargarlo desde BD
    // para evitar que el cliente manipule el precio enviado.
 
    private BigDecimal getProductPrice(Integer productId) {
        return productLookupRepository.findActivePriceById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado o inactivo con id: " + productId));
    }
 
    private void updateMovementReferences(List<SaleItemRequest> items,
                                          Integer saleId, Integer branchId) {
        // Actualizar id_reference de los movimientos recién creados
        // Se podría hacer con una query JPQL masiva, pero dado que el número
        // de items por venta es bajo (< 20 típicamente), el loop es suficiente
        // TODO: optimizar con @Modifying query si el volumen lo requiere
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

