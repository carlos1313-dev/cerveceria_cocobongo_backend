package com.cocobongo.cerveceria.inventory.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cocobongo.cerveceria.common.exception.BusinessException;
import com.cocobongo.cerveceria.common.exception.ResourceNotFoundException;
import com.cocobongo.cerveceria.inventory.dto.InventoryMovementRequestDTO;
import com.cocobongo.cerveceria.inventory.dto.InventoryMovementResponseDTO;
import com.cocobongo.cerveceria.inventory.dto.InventoryResponseDTO;
import com.cocobongo.cerveceria.inventory.dto.ProductCreatedResponseDTO;
import com.cocobongo.cerveceria.inventory.dto.ProductRequestDTO;
import com.cocobongo.cerveceria.inventory.dto.ProductResponseDTO;
import com.cocobongo.cerveceria.inventory.dto.ProviderRequestDTO;
import com.cocobongo.cerveceria.inventory.dto.ProviderResponseDTO;
import com.cocobongo.cerveceria.inventory.entities.IdInventory;
import com.cocobongo.cerveceria.inventory.entities.InventoryEntity;
import com.cocobongo.cerveceria.inventory.entities.InventoryMovementEntity;
import com.cocobongo.cerveceria.inventory.entities.ProviderEntity;
import com.cocobongo.cerveceria.inventory.repositories.InventoryMovementRepository;
import com.cocobongo.cerveceria.inventory.repositories.InventoryRepository;
import com.cocobongo.cerveceria.branches.dto.BranchResponseDTO;
import com.cocobongo.cerveceria.branches.services.BranchesService;
import com.cocobongo.cerveceria.inventory.repositories.ProductRepository;
import com.cocobongo.cerveceria.inventory.repositories.ProviderRepository;

import jakarta.persistence.EntityNotFoundException;

import com.cocobongo.cerveceria.inventory.entities.ProductEntity;

@Service
public class InventoryService {

    private static final Set<String> VALID_PRODUCT_TYPES = Set.of("RESALE", "SUPPLY", "MADE");
    private static final Set<String> VALID_ENTRY_REASONS = Set.of("PURCHASE", "ADJUSTMENT");

    private final ProviderRepository providerRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryRepository inventoryRepository;
    private final BranchesService branchesService;

    public InventoryService(ProviderRepository providerRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            InventoryMovementRepository movementRepository,
            BranchesService branchesService) {
        this.providerRepository = providerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = movementRepository;
        this.branchesService = branchesService;
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

    // Busca un producto activo por su ID (usado en ventas)
    @Transactional(readOnly = true)
    public ProductEntity findActiveProductById(Integer idProduct) {
        ProductEntity p = productRepository.findActiveById(idProduct)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado o inactivo con id: " + idProduct));
        return p;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toResponseDTO);

    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findProductById(Integer id) {
        return productRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro un producto con el id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findProductByNameAndBranch(String name, Integer idBranch, Pageable pageable) {
        Page<ProductResponseDTO> p = productRepository.findProductByNameAndBranch(name, idBranch, pageable)
                .map(this::toResponseDTO);
        return p;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findProductByIdAndBranch(Integer id, Integer idBranch, Pageable pageable) {
        Page<ProductResponseDTO> p = productRepository.findProductByidAndBranch(id, idBranch, pageable)
                .map(this::toResponseDTO);
        return p;
    }

    @Transactional
    public ProductCreatedResponseDTO createProduct(ProductRequestDTO newp) {
        if (newp.getName() == null) {
            throw new BusinessException("El nombre es obligatorio");
        }

        if (newp.getCost() == null) {
            throw new BusinessException("El costo es obligatorio");

        }

        if (newp.getPrice() == null) {
            throw new BusinessException("El precio es obligatorio");
        }

        String type = newp.getType() != null
                ? newp.getType().toString()
                : "RESALE";

        if (newp.getProviderId() == null && !VALID_PRODUCT_TYPES.contains(type)) {
            throw new BusinessException("El proveedor es obligatorio");
        }

        ProviderEntity provider = null;
        if (newp.getProviderId() != null) {
            provider = providerRepository.findById(newp.getProviderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + newp.getProviderId()));
        }

        if (newp.getIsActive() == null) {
            throw new BusinessException("El estado del producto es obligatorio");
        }

        ProductEntity e = ProductEntity.builder()
                .provider(provider)
                .name(newp.getName())
                .description(newp.getDescription())
                .type(newp.getType())
                .cost(newp.getCost())
                .price(newp.getPrice())
                .isActive(newp.getIsActive())
                .build();

        // Requiere datos de sucursal para crear el inventario asociado.
        if (newp.getBranch() == null) {
            throw new BusinessException("La sucursal es obligatoria al crear un producto y su inventario.");
        }

        // Primero se guarda el producto para obtener su id generado.
        productRepository.save(e);

        // Luego se crea la sucursal nueva y se utiliza su id para inventario.
        BranchResponseDTO branch = branchesService.createBranch(newp.getBranch());

        InventoryEntity inventory = new InventoryEntity();
        inventory.setIdProduct(e.getIdProduct());
        inventory.setIdBranch(branch.getId());
        Integer stock = newp.getInitialStock();
        if (stock == null) {
            stock = 0;
        }
        inventory.setStock(stock);
        Integer minStock = newp.getMinStock();
        if (minStock == null) {
            minStock = 0;
        }
        inventory.setMinStock(minStock);
        inventoryRepository.save(inventory);

        return new ProductCreatedResponseDTO(
            toResponseDTO(e),
            branch,
            toInventoryResponseWithProduct(inventory, e));
    }

    private InventoryResponseDTO toInventoryResponseWithProduct(InventoryEntity i, ProductEntity product) {
        InventoryResponseDTO r = new InventoryResponseDTO();
        r.setIdProduct(i.getIdProduct());
        r.setIdBranch(i.getIdBranch());
        r.setStock(i.getStock());
        r.setMinStock(i.getMinStock());

        if (product != null) {
            r.setProductName(product.getName());
            r.setProductType(product.getType());
            r.setCost(product.getCost());
            r.setPrice(product.getPrice());
        }
        return r;
    }

    @Transactional
    public ProductResponseDTO updateProduct(ProductRequestDTO request, Integer id) {
        ProductEntity up = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro un producto con el id: " + id));
        if (request.getName() == null) {
            throw new BusinessException("El nombre es obligatorio");
        }

        if (request.getCost() == null) {
            throw new BusinessException("El costo es obligatorio");

        }

        if (request.getPrice() == null) {
            throw new BusinessException("El precio es obligatorio");
        }

        String type = request.getType() != null
                ? request.getType().toString()
                : "RESALE";

        if (request.getProviderId() == null && !VALID_PRODUCT_TYPES.contains(type)) {
            throw new BusinessException("El proveedor es obligatorio");
        }

        Objects.requireNonNull(request.getIsActive(), "El estado del producto es obligatorio");

        ProviderEntity provider = null;
        if (request.getProviderId() != null) {
            provider = providerRepository.findById(request.getProviderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + request.getProviderId()));
        }
        up.setProvider(provider);
        up.setName(request.getName());
        up.setDescription(request.getDescription());
        up.setType(request.getType());
        up.setCost(request.getCost());
        up.setPrice(request.getPrice());
        up.setIsActive(request.getIsActive());

        productRepository.save(up);
        return toResponseDTO(up);
    }

    @Transactional
    public void deleteProduct(Integer id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro un producto con el id: " + id));
        product.setIsActive(false);
        productRepository.save(product);
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

    // Obtiene el inventario de un producto en una sucursal específica
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

    // Decrementa el stock de un producto en una sucursal de forma atómica (usado
    // para registrar ventas)
    @Transactional
    public int decrementStock(Integer idProduct, Integer idBranch, Integer quantity) {
        int rowsAffected = inventoryRepository.decrementStock(idProduct, idBranch, quantity);
        if (rowsAffected == 0) {
            throw new BusinessException(
                    "Stock insuficiente para el producto " + idProduct + " en la sucursal " + idBranch);
        }
        return rowsAffected;
    }

    // Registra un movimiento de venta con referencia a la venta (encapsula la
    // lógica de inventario)
    @Transactional
    public InventoryMovementResponseDTO recordSaleMovement(Integer idProduct,
            Integer idBranch,
            Integer idUser,
            Integer quantity,
            Integer saleId) {
        InventoryMovementEntity movement = InventoryMovementEntity.builder()
                .idProduct(idProduct)
                .idBranch(idBranch)
                .idUser(idUser)
                .type("OUT")
                .reason("SALE")
                .quantity(quantity)
                .idReference(saleId)
                .build();
        return toMovementResponse(inventoryMovementRepository.save(movement));
    }

    // Registra una entrada de inventario (IN) y actualiza el stock
    @Transactional
    public InventoryMovementResponseDTO registerEntry(InventoryMovementRequestDTO request,
            Integer idUserLogged) {
        String reason = request.getReason() != null
                ? request.getReason().toUpperCase()
                : "PURCHASE";

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
            r.setCost(i.getProduct().getCost());
            r.setPrice(i.getProduct().getPrice());
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

    private ProductResponseDTO toResponseDTO(ProductEntity entity) {
        if (entity == null) {
            return null;
        }

        return ProductResponseDTO.builder()
                .idProduct(entity.getIdProduct())

                .providerId(
                        entity.getProvider() != null
                                ? entity.getProvider().getIdProvider()
                                : null)

                .providerName(
                        entity.getProvider() != null
                                ? entity.getProvider().getName()
                                : null)

                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .cost(entity.getCost())
                .price(entity.getPrice())
                .isActive(entity.getIsActive())
                .build();
    }
}