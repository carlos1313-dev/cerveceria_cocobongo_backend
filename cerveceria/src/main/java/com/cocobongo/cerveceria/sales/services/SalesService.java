package com.cocobongo.cerveceria.sales.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.branches.services.BranchesService;
import com.cocobongo.cerveceria.clients.entities.ClientEntity;
import com.cocobongo.cerveceria.clients.repositories.ClientRepository;
import com.cocobongo.cerveceria.common.exception.BusinessException;
import com.cocobongo.cerveceria.common.exception.ResourceNotFoundException;
import com.cocobongo.cerveceria.inventory.dto.InventoryResponseDTO;
import com.cocobongo.cerveceria.inventory.entities.ProductEntity;
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
    private final BranchesService branchService;
    private final InventoryService inventoryService;
    private final ProductRepository productRepository; // Inyectado correctamente

    @Transactional
    public SaleResponse registerSale(RegisterSaleRequest request, UserEntity currentUser) {
 
        // ── 1. Validar CREDIT requiere cliente ─────────────────────────────────
        if (request.getPaymentType() == PaymentType.CREDIT && request.getClientId() == null) {
            throw new BusinessException("Las ventas a crédito requieren un cliente asociado");
        }
 
        // Resolver cliente si viene en el request
        ClientEntity client = null;
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cliente no encontrado con id: " + request.getClientId()));
 
            if (!client.getIsActive()) {
                throw new BusinessException("El cliente está inactivo y no puede realizar compras");
            }
        }
 
        // Resolver sucursal del usuario que registra la venta
        if (currentUser.getIdBranch() == null) {
            throw new BusinessException("El usuario no tiene una sucursal asignada");
        }
        
        branchService.findBranch(currentUser.getIdBranch());
        
        BranchEntity branch = new BranchEntity();
        branch.setIdBranch(currentUser.getIdBranch());
 
        // ── 2. Procesar cada item: stock + movimiento ──────────────────────────
        List<SaleDetailEntity> details = new ArrayList<>();
        BigDecimal totalSale = BigDecimal.ZERO;
 
        for (SaleItemRequest item : request.getItems()) {
 
            ProductEntity product = inventoryService.findActiveProductById(item.getProductId());
 
            List<InventoryResponseDTO> inventoryList = inventoryService
                    .findByProductAndBranch(item.getProductId(), currentUser.getIdBranch());
            
            if (inventoryList.isEmpty()) {
                throw new ResourceNotFoundException(
                        "El producto '" + product.getName() + "' no tiene inventario en esta sucursal");
            }
            
            InventoryResponseDTO inventory = inventoryList.get(0);
            if (inventory.getStock() < item.getQuantity()) {
                throw new BusinessException(
                        "Stock insuficiente para '" + product.getName()
                                + "'. Disponible: " + inventory.getStock()
                                + ", solicitado: " + item.getQuantity());
            }
 
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
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

        if (request.getPaymentType() == PaymentType.CREDIT && client != null) {
            BigDecimal currentBalance = client.getBalance() != null ? client.getBalance() : BigDecimal.ZERO;
            client.setBalance(currentBalance.add(totalSale));
            clientRepository.save(client);
        }
 
        // ── 4. Persistir la venta ──────────────────────────────────────────────
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
 
        // Descontar stock y registrar movimiento de manera segura
        for (SaleItemRequest item : request.getItems()) {
 
            int updated = inventoryService.decrementStock(
                    item.getProductId(),
                    currentUser.getIdBranch(),
                    item.getQuantity()
            );
 
            if (updated == 0) {
                throw new BusinessException(
                        "No se pudo descontar stock del producto id: " + item.getProductId()
                                + ". Es posible que otro usuario registrara una venta al mismo tiempo.");
            }
 
            inventoryService.recordSaleMovement(
                    item.getProductId(),
                    currentUser.getIdBranch(),
                    currentUser.getIdUser(),
                    item.getQuantity(),
                    saved.getIdSale()
            );
        }
 
        return toResponse(saved);
    }
 
    @Transactional(readOnly = true)
    public SaleResponse findById(Integer id) {
        SaleEntity sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
        return toResponse(sale);
    }
 
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