package com.cocobongo.cerveceria.inventory.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cocobongo.cerveceria.inventory.entities.IdInventory;
import com.cocobongo.cerveceria.inventory.entities.InventoryEntity;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, IdInventory> {

    // Inventario de una sucursal con búsqueda por nombre de producto
    @Query("SELECT i FROM InventoryEntity i JOIN FETCH i.product p " +
           "WHERE i.idBranch = :branchId " +
           "AND p.isActive = true " +
           "AND (:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<InventoryEntity> findByBranch(
            @Param("branchId") Integer branchId,
            @Param("search")   String  search);

    // Inventario de un producto en todas sus sucursales
    @Query("SELECT i FROM InventoryEntity i WHERE i.idProduct = :productId")
    List<InventoryEntity> findByProduct(@Param("productId") Integer productId);

    // Stock de un producto en una sucursal específica
    @Query("SELECT i FROM InventoryEntity i " +
           "WHERE i.idProduct = :productId AND i.idBranch = :branchId")
    Optional<InventoryEntity> findByProductAndBranch(
            @Param("productId") Integer productId,
            @Param("branchId")  Integer branchId);

    // Productos con stock <= minStock (alertas)
    @Query("SELECT i FROM InventoryEntity i JOIN FETCH i.product p " +
           "WHERE i.stock <= i.minStock " +
           "AND p.isActive = true " +
           "AND (:branchId IS NULL OR i.idBranch = :branchId)")
    List<InventoryEntity> findLowStock(@Param("branchId") Integer branchId);

    /**
     * Descuenta stock de forma atómica a nivel de BD.
     * Se usa UPDATE directo en lugar de load+save para evitar
     * race conditions en entornos con múltiples instancias o usuarios
     * registrando ventas simultáneamente.
     *
     * La condición stock >= :quantity evita stock negativo a nivel de BD
     * como segunda línea de defensa (la primera es la validación en el servicio).
     * Si retorna 0 filas afectadas, el servicio lanza excepción.
     */
    @Modifying
    @Query("""
        UPDATE InventoryEntity i
        SET i.stock = i.stock - :quantity
        WHERE i.idProduct = :productId
          AND i.idBranch  = :branchId
          AND i.stock     >= :quantity
    """)
    int decrementStock(
            @Param("productId") Integer productId,
            @Param("branchId")  Integer branchId,
            @Param("quantity")  Integer quantity
    );
}