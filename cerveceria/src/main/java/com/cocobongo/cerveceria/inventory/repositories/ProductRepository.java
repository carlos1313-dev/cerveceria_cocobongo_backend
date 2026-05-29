package com.cocobongo.cerveceria.inventory.repositories;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cocobongo.cerveceria.inventory.entities.ProductEntity;
import com.cocobongo.cerveceria.inventory.entities.InventoryEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
        /**
         * Retorna el precio de venta de un producto activo.
         * Si el producto no existe o está inactivo, retorna Optional.empty()
         * y el servicio lanzará ResourceNotFoundException.
         */
        @Query("SELECT p.price FROM ProductEntity p WHERE p.idProduct = :id AND p.isActive = true")
        Optional<BigDecimal> findActivePriceById(@Param("id") Integer id);

        /**
         * Verifica existencia y activo en una sola query liviana.
         * Usado antes de intentar descontar stock para dar un mensaje
         * de error más descriptivo que el de stock insuficiente.
         */
        @Query("SELECT p FROM ProductEntity p WHERE p.idProduct = :id AND p.isActive = true")
        Optional<ProductEntity> findActiveById(@Param("id") Integer id);

        @Query("""
                            SELECT p
                            FROM ProductEntity p
                            INNER JOIN InventoryEntity i ON i.product.idProduct = p.idProduct
                            WHERE p.idProduct = :id
                              AND i.branch.idBranch = :idBranch
                              AND p.isActive = true
                        """)
        Page<ProductEntity> findProductByidAndBranch(
                        @Param("id") Integer id,
                        @Param("idBranch") Integer idBranch,
                        Pageable pageable);

        @Query("""
                            SELECT p
                            FROM ProductEntity p
                            INNER JOIN InventoryEntity i ON i.product.idProduct = p.idProduct
                            WHERE p.name = :name
                              AND i.branch.idBranch = :idBranch
                              AND p.isActive = true
                        """)
        Page<ProductEntity> findProductByNameAndBranch(
                        @Param("name") String name,
                        @Param("idBranch") Integer idBranch,
                        Pageable pageable);
}
