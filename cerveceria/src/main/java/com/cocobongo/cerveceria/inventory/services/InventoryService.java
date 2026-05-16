package com.cocobongo.cerveceria.inventory.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cocobongo.cerveceria.common.exception.BusinessException;
import com.cocobongo.cerveceria.common.exception.ResourceNotFoundException;
import com.cocobongo.cerveceria.inventory.dto.InventoryMovementRequestDTO;
import com.cocobongo.cerveceria.inventory.dto.InventoryMovementResponseDTO;
import com.cocobongo.cerveceria.inventory.dto.InventoryResponseDTO;
import com.cocobongo.cerveceria.inventory.dto.ProviderRequestDTO;
import com.cocobongo.cerveceria.inventory.dto.ProviderResponseDTO;
import com.cocobongo.cerveceria.inventory.entities.IdInventory;
import com.cocobongo.cerveceria.inventory.entities.InventoryEntity;
import com.cocobongo.cerveceria.inventory.entities.InventoryMovementEntity;
import com.cocobongo.cerveceria.inventory.entities.ProviderEntity;
import com.cocobongo.cerveceria.inventory.repositories.InventoryMovementRepository;
import com.cocobongo.cerveceria.inventory.repositories.InventoryRepository;
import com.cocobongo.cerveceria.inventory.repositories.ProductRepository;
import com.cocobongo.cerveceria.inventory.repositories.ProviderRepository;

@Service
public class InventoryService {

    private static final Set<String> VALID_PRODUCT_TYPES = Set.of("RESALE", "SUPPLY", "MADE");
    private static final Set<String> VALID_ENTRY_REASONS = Set.of("PURCHASE", "ADJUSTMENT");

    private final ProviderRepository          providerRepository;
    private final ProductRepository           productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryRepository         inventoryRepository;

    public InventoryService(ProviderRepository providerRepository,
                            ProductRepository productRepository,
                            InventoryRepository inventoryRepository,
                            InventoryMovementRepository movementRepository) {
        this.providerRepository          = providerRepository;
        this.productRepository           = productRepository;
        this.inventoryRepository         = inventoryRepository;
        this.inventoryMovementRepository = movementRepository;
    }

    // Obtiene todos los proveedores activos, con opción de búsqueda por nombre
    @Transactional(readOnly = true)
    public List<ProviderResponseDTO> findAllProviders(String search) {
        List<ProviderEntity> list = (search == null || search.isBlank())
                ? providerRepository.findByIsActiveTrue()
                : providerRepository.searchByName(search);
        return list.stream().map(ProviderResponseDTO::new).collect(Collectors.toList());
    }

    // Obtiene un proveedor activo por su ID
    @Transactional(readOnly = true)
    public ProviderResponseDTO findProviderById(Integer id) {
        ProviderEntity p = providerRepository.findByIdProviderAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Provider not found: " + id));
        return new ProviderResponseDTO(p);
    }

    // Crea un nuevo proveedor
    @Transactional
    public ProviderResponseDTO createProvider(ProviderRequestDTO request) {
        ProviderEntity p = new ProviderEntity();
        p.setName(request.getName());
        p.setTelephone(request.getTelephone());
        p.setAddress(request.getAddress());
        p.setEmail(request.getEmail());
        p.setIsActive(true);
        return new ProviderResponseDTO(providerRepository.save(p));
    }

    // Actualiza un proveedor existente
    @Transactional
    public ProviderResponseDTO updateProvider(Integer id, ProviderRequestDTO request) {
        ProviderEntity p = providerRepository.findByIdProviderAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + id));
        p.setName(request.getName());
        p.setTelephone(request.getTelephone());
        p.setAddress(request.getAddress());
        p.setEmail(request.getEmail());
        return new ProviderResponseDTO(providerRepository.save(p));
    }

    // Realiza un borrado lógico (soft delete) del proveedor
    @Transactional
    public void deleteProvider(Integer id) {
        ProviderEntity p = providerRepository.findByIdProviderAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + id));
        if (providerRepository.hasActiveProducts(id)) {
            throw new BusinessException(
                "Cannot deactivate provider " + id + ": has active products associated");
        }
        p.setIsActive(false);
        providerRepository.save(p);
    }

    // Obtiene el inventario de una sucursal con opción de búsqueda por producto
    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> findByBranch(Integer idBranch, String search) {
        return inventoryRepository.findByBranch(idBranch, search)
                .stream().map(this::toInventoryResponse).collect(Collectors.toList());
    }

    // Obtiene el inventario de un producto en todas las sucursales
    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> findByProduct(Integer idProduct) {
        return inventoryRepository.findByProduct(idProduct)
                .stream().map(this::toInventoryResponse).collect(Collectors.toList());
    }

    //Obtiene el inventario de un producto en una sucursal específica
    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> findByProductAndBranch(Integer idProduct, Integer idBranch) {
        return inventoryRepository.findByProductAndBranch(idProduct, idBranch)
                .stream().map(this::toInventoryResponse).collect(Collectors.toList());
    }

    // Obtiene productos con bajo stock (stock <= minStock)
    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> findLowStock(Integer idBranch) {
        return inventoryRepository.findLowStock(idBranch)
                .stream().map(this::toInventoryResponse).collect(Collectors.toList());
    }

    //Decrementa el stock de un producto en una sucursal de forma atómica (usado para registrar ventas)
    @Transactional
    public int decrementStock(Integer idProduct, Integer idBranch, Integer quantity) {
        int rowsAffected = inventoryRepository.decrementStock(idProduct, idBranch, quantity);
        if (rowsAffected == 0) {
            throw new BusinessException(
                "Stock insuficiente para el producto " + idProduct + " en la sucursal " + idBranch);
        }
        return rowsAffected;
    }   

    // Registra una entrada de inventario (IN) y actualiza el stock
    @Transactional
    public InventoryMovementResponseDTO registerEntry(InventoryMovementRequestDTO request,
                                                      Integer idUserLogged) {
        String reason = request.getReason() != null
                ? request.getReason().toUpperCase() : "PURCHASE";

        if (!VALID_ENTRY_REASONS.contains(reason)) {
            throw new BusinessException(
                "Razón inválida para entrada manual: " + reason +
                ". Permitidas: PURCHASE, ADJUSTMENT");
        }

        // Busca o crea el registro de inventario
        IdInventory idInventory = new IdInventory(request.getIdProduct(), request.getIdBranch());
        InventoryEntity inventory = inventoryRepository.findById(idInventory)
                .orElseGet(() -> {
                    InventoryEntity newRecord = new InventoryEntity();
                    newRecord.setIdProduct(request.getIdProduct());
                    newRecord.setIdBranch(request.getIdBranch());
                    newRecord.setStock(0);
                    newRecord.setMinStock(0);
                    return newRecord;
                });

        // Actualiza el stock
        inventory.setStock(inventory.getStock() + request.getQuantity());
        inventoryRepository.save(inventory);

        // Registra el movimiento
        InventoryMovementEntity movement = InventoryMovementEntity.builder()
                .idProduct(request.getIdProduct())
                .idBranch(request.getIdBranch())
                .idUser(idUserLogged)
                .type("IN")
                .reason(reason)
                .quantity(request.getQuantity())
                .movementDate(LocalDateTime.now())
                .build();

        return toMovementResponse(inventoryMovementRepository.save(movement));
    }

    // Obtiene el historial de movimientos de inventario con filtros opcionales
    @Transactional(readOnly = true)
    public List<InventoryMovementResponseDTO> findMovements(Integer idProduct,
                                                            Integer idBranch,
                                                            String type,
                                                            String reason,
                                                            LocalDateTime from,
                                                            LocalDateTime to) {
        return inventoryMovementRepository.findByFilters(idProduct, idBranch, type, reason, from, to)
                .stream().map(this::toMovementResponse).collect(Collectors.toList());
    }

    // Convierte una entidad Inventory a DTO
    private InventoryResponseDTO toInventoryResponse(InventoryEntity i) {
        InventoryResponseDTO r = new InventoryResponseDTO();
        r.setIdProduct(i.getIdProduct());
        r.setIdBranch(i.getIdBranch());
        r.setStock(i.getStock());
        r.setMinStock(i.getMinStock());

        if (i.getProduct() != null) {
            r.setProductName(i.getProduct().getName());
            r.setProductType(i.getProduct().getType());
        }
        return r;
    }

    // Convierte una entidad InventoryMovement a DTO
    private InventoryMovementResponseDTO toMovementResponse(InventoryMovementEntity im) {
        InventoryMovementResponseDTO r = new InventoryMovementResponseDTO();
        r.setIdMovement(im.getIdMovement());
        r.setIdProduct(im.getIdProduct());
        r.setIdBranch(im.getIdBranch());
        r.setIdUser(im.getIdUser());
        r.setType(im.getType());
        r.setReason(im.getReason());
        r.setQuantity(im.getQuantity());
        r.setMovementDate(im.getMovementDate());
        r.setIdReference(im.getIdReference());

        if (im.getProduct() != null) {
            r.setProductName(im.getProduct().getName());
        }

        return r;
    }
}