package com.cocobongo.cerveceria.users.repositories;
 
import com.cocobongo.cerveceria.users.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.cocobongo.cerveceria.users.entities.Role;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
 
    Optional<UserEntity> findByEmail(String email);
 
    boolean existsByEmail(String email);
 
    // Cuenta cuántos ADMINs activos hay — usado para proteger el único ADMIN
    long countByRoleAndIsActiveTrue(Role role);

    /*
     * JPQL con filtros opcionales — patrón IS NULL como cortocircuito.
     * Si role llega null, la condición (:role IS NULL) es true y se ignora.
     * Mismo patrón que AuditRepository — consistente en todo el proyecto.
     */
    @Query("""
        SELECT u FROM UserEntity u
        WHERE (:role     IS NULL OR u.role     = :role)
          AND (:branchId IS NULL OR u.idBranch = :branchId)
    """)
    Page<UserEntity> findByFilters(
            @Param("role")     Role    role,
            @Param("branchId") Integer branchId,
            Pageable pageable
    );
}
 