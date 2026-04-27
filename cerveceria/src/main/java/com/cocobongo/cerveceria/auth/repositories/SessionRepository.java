package com.cocobongo.cerveceria.auth.repositories;
 
import com.cocobongo.cerveceria.auth.entities.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.Optional;
 
public interface SessionRepository extends JpaRepository<SessionEntity, Integer> {
 
    Optional<SessionEntity> findByToken(String token);
 
    // Revoca todas las sesiones activas de un usuario (ej: cambio de contraseña)
    @Modifying
    @Query("""
        UPDATE SessionEntity s
        SET s.isActive = false, s.revokedAt = CURRENT_TIMESTAMP
        WHERE s.user.idUser = :userId AND s.isActive = true
    """)
    void revokeAllByUserId(@Param("userId") Integer userId);
}