package com.cocobongo.cerveceria.sales.services;
 
import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.branches.services.BranchesService;
import com.cocobongo.cerveceria.clients.entities.ClientEntity;
import com.cocobongo.cerveceria.clients.services.ClientService;
import com.cocobongo.cerveceria.common.exception.BusinessException;
import com.cocobongo.cerveceria.common.exception.ResourceNotFoundException;
import com.cocobongo.cerveceria.common.utils.CurrencyConverter;
import com.cocobongo.cerveceria.exchangerate.entities.ExchangeRateEntity;
import com.cocobongo.cerveceria.exchangerate.services.ExchangeRateService;
import com.cocobongo.cerveceria.inventory.dto.InventoryResponseDTO;
import com.cocobongo.cerveceria.inventory.entities.ProductEntity;
import com.cocobongo.cerveceria.inventory.services.InventoryService;
import com.cocobongo.cerveceria.sales.dto.*;
import com.cocobongo.cerveceria.sales.entities.*;
import com.cocobongo.cerveceria.sales.repositories.SaleRepository;
import com.cocobongo.cerveceria.users.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class SalesService {
 
    private final SaleRepository     saleRepository;
    private final ClientService      clientService;
    private final BranchesService    branchService;
    private final InventoryService   inventoryService;
    private final ExchangeRateService exchangeRateService;
 
    // ── RF-VEN-01..05 + RF-INV-03: Registrar venta con multi-moneda ──────────
    //
    // FASE 1 — Validaciones (sin escrituras)
    //   1a. Obtener tasa BCV vigente
    //   1b. Validar pagos: CREDIT requiere cliente; al menos un pago
    //   1c. Resolver cliente si viene en el request
    //   1d. Procesar items: validar producto activo, stock suficiente,
    //       construir detalles con snapshot de precio en ambas monedas
    //   1e. Construir pagos: calcular amount_usd para pagos en VES
    //
    // FASE 2 — Persistencia
    //   2a. Guardar venta + detalles + pagos (cascade ALL)
    //   2b. Descontar stock y registrar movimiento SALE por cada item
    //   2c. Actualizar balance del cliente si hay pago(s) CREDIT
 
    @Transactional
    public SaleResponse registerSale(RegisterSaleRequest request,
                                     UserEntity currentUser) {
 
        // ── FASE 1: Validaciones ───────────────────────────────────────────────
 
        // 1a. Tasa BCV vigente — falla si no hay ninguna registrada
        ExchangeRateEntity currentRate = exchangeRateService.getCurrentEntity();
        BigDecimal rate = currentRate.getRate();
 
        // 1b. Validar que CREDIT requiere cliente
        boolean hasCredit = request.getPayments().stream()
                .anyMatch(p -> p.getMethod() == PaymentType.CREDIT);
 
        if (hasCredit && request.getClientId() == null) {
            throw new BusinessException(
                    "Los pagos a crédito requieren un cliente asociado");
        }
 
        // 1c. Resolver cliente
        ClientEntity client = null;
        if (request.getClientId() != null) {
            clientService.validateClientActive(request.getClientId());
            client = clientService.getClientReference(request.getClientId());
        }
 
        // 1d. Procesar items
        List<SaleDetailEntity> details   = new ArrayList<>();
        BigDecimal             totalUsd  = BigDecimal.ZERO;
        Integer                branchId  = null;
 
        for (SaleItemRequest item : request.getItems()) {
            if (item.getBranchId() == null) {
                throw new BusinessException("La sucursal del producto es obligatoria");
            }
            if (branchId == null) {
                branchId = item.getBranchId();
                branchService.findBranch(branchId);
            } else if (!branchId.equals(item.getBranchId())) {
                throw new BusinessException(
                        "Todos los productos deben pertenecer a la misma sucursal");
            }
 
            ProductEntity product = inventoryService.findActiveProductById(
                    item.getProductId());
 
            List<InventoryResponseDTO> inventoryList = inventoryService
                    .findByProductAndBranch(item.getProductId(), item.getBranchId());
 
            if (inventoryList.isEmpty()) {
                throw new ResourceNotFoundException(
                        "El producto '" + product.getName()
                                + "' no tiene inventario en esta sucursal");
            }
 
            InventoryResponseDTO inventory = inventoryList.get(0);
            if (inventory.getStock() < item.getQuantity()) {
                throw new BusinessException(
                        "Stock insuficiente para '" + product.getName()
                                + "'. Disponible: " + inventory.getStock()
                                + ", solicitado: " + item.getQuantity());
            }
 
            // Snapshot del precio en USD — fuente de verdad para reportes históricos
            BigDecimal unitPriceUsd = product.getPrice();
            BigDecimal subtotalUsd  = unitPriceUsd.multiply(
                    BigDecimal.valueOf(item.getQuantity()));
            totalUsd = totalUsd.add(subtotalUsd);
 
            details.add(SaleDetailEntity.builder()
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(unitPriceUsd)
                    .subtotal(subtotalUsd)
                    .build());
        }
 
        if (branchId == null) {
            throw new BusinessException("La sucursal de la venta es obligatoria");
        }
 
        // 1e. Construir pagos con amount_usd calculado
        List<SalePaymentEntity> payments = new ArrayList<>();
        BigDecimal creditAmountUsd = BigDecimal.ZERO;
 
        for (PaymentItemRequest paymentReq : request.getPayments()) {
            BigDecimal amountUsd;
 
            if (paymentReq.getCurrency() == Currency.VES) {
                // Convertir bolívares a USD usando la tasa actual
                amountUsd = CurrencyConverter.vesToUsd(paymentReq.getAmount(), rate);
            } else {
                amountUsd = paymentReq.getAmount();
            }
 
            if (paymentReq.getMethod() == PaymentType.CREDIT) {
                creditAmountUsd = creditAmountUsd.add(amountUsd);
            }
 
            payments.add(SalePaymentEntity.builder()
                    .method(paymentReq.getMethod())
                    .currency(paymentReq.getCurrency())
                    .amount(paymentReq.getAmount())
                    .amountUsd(amountUsd)
                    .build());
        }
 
        // Status: PENDING si hay al menos un pago CREDIT, COMPLETED si no
        SaleStatus initialStatus = hasCredit ? SaleStatus.PENDING : SaleStatus.COMPLETED;
 
        // ── FASE 2: Persistencia ───────────────────────────────────────────────
 
        // 2a. Construir y guardar la venta
        BranchEntity branch = new BranchEntity();
        branch.setIdBranch(branchId);
 
        SaleEntity sale = SaleEntity.builder()
                .branch(branch)
                .user(currentUser)
                .client(client)
                .rate(currentRate)
                .total(totalUsd)
                .status(initialStatus)
                .build();
 
        details.forEach(sale::addDetail);
        payments.forEach(sale::addPayment);
 
        SaleEntity saved = saleRepository.save(sale);
 
        // 2b. Descontar stock y registrar movimiento SALE
        for (SaleItemRequest item : request.getItems()) {
            int updated = inventoryService.decrementStock(
                    item.getProductId(),
                    item.getBranchId(),
                    item.getQuantity());
 
            if (updated == 0) {
                throw new BusinessException(
                        "No se pudo descontar stock del producto id: "
                                + item.getProductId()
                                + ". Es posible que otro usuario registrara "
                                + "una venta al mismo tiempo. Intente nuevamente.");
            }
 
            inventoryService.recordSaleMovement(
                    item.getProductId(),
                    item.getBranchId(),
                    currentUser.getIdUser(),
                    item.getQuantity(),
                    saved.getIdSale());
        }
 
        // 2c. Actualizar balance del cliente por el monto a crédito
        if (hasCredit && client != null && creditAmountUsd.compareTo(BigDecimal.ZERO) > 0) {
            clientService.updateClientBalance(request.getClientId(), creditAmountUsd);
        }
 
        return toResponse(saved, rate);
    }
 
    // ── GET /api/v1/sales/{id} ────────────────────────────────────────────────
 
    @Transactional(readOnly = true)
    public SaleResponse findById(Integer id) {
        SaleEntity sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta no encontrada con id: " + id));
 
        // Usar la tasa de la venta para conversiones históricas correctas
        BigDecimal rate = sale.getRate() != null
                ? sale.getRate().getRate()
                : exchangeRateService.getCurrentRate();
 
        return toResponse(sale, rate);
    }
 
    // ── Mapper ────────────────────────────────────────────────────────────────
 
    private SaleResponse toResponse(SaleEntity sale, BigDecimal rate) {
        SaleResponse response = new SaleResponse();
        response.setIdSale(sale.getIdSale());
        response.setSaleDate(sale.getSaleDate());
        response.setStatus(sale.getStatus());
        response.setTotalUsd(sale.getTotal());
        response.setTotalVes(CurrencyConverter.usdToVes(sale.getTotal(), rate));
        response.setExchangeRate(rate);
 
        if (sale.getBranch() != null) {
            response.setBranchName(sale.getBranch().getName());
        }
        if (sale.getUser() != null) {
            response.setRegisteredBy(sale.getUser().getName());
        }
        if (sale.getClient() != null) {
            response.setClientName(sale.getClient().getName());
        }
 
        // Detalles con valores en ambas monedas
        List<SaleDetailResponse> detailResponses = sale.getDetails().stream()
                .map(d -> {
                    SaleDetailResponse dr = new SaleDetailResponse();
                    dr.setIdSaleDetail(d.getIdSaleDetail());
                    dr.setProductId(d.getProduct().getIdProduct());
                    dr.setProductName(d.getProduct().getName());
                    dr.setQuantity(d.getQuantity());
                    dr.setUnitPriceUsd(d.getUnitPrice());
                    dr.setUnitPriceVes(CurrencyConverter.usdToVes(d.getUnitPrice(), rate));
                    dr.setSubtotalUsd(d.getSubtotal());
                    dr.setSubtotalVes(CurrencyConverter.usdToVes(d.getSubtotal(), rate));
                    return dr;
                })
                .toList();
        response.setDetails(detailResponses);
 
        // Pagos
        List<PaymentResponse> paymentResponses = sale.getPayments().stream()
                .map(p -> {
                    PaymentResponse pr = new PaymentResponse();
                    pr.setIdPayment(p.getIdPayment());
                    pr.setMethod(p.getMethod());
                    pr.setCurrency(p.getCurrency());
                    pr.setAmount(p.getAmount());
                    pr.setAmountUsd(p.getAmountUsd());
                    return pr;
                })
                .toList();
        response.setPayments(paymentResponses);
 
        return response;
    }
}