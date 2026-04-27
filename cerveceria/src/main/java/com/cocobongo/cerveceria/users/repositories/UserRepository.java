package com.cocobongo.cerveceria.users.repositories;
 
import com.cocobongo.cerveceria.users.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
 
    Optional<UserEntity> findByEmail(String email);
 
    boolean existsByEmail(String email);
 
    // Cuenta cuántos ADMINs activos hay — usado para proteger el único ADMIN
    long countByRoleAndIsActiveTrue(com.cocobongo.cerveceria.users.entities.Role role);
}
 