package com.cocobongo.cerveceria.auth.repositories;
 
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cocobongo.cerveceria.auth.entities.SessionEntity;
 
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

    // Revoca todas las sesiones activas EXCEPTO la actual (para cambio de contraseña)
    // Permite al usuario continuar en el dispositivo actual, pero desconecta otros
    @Modifying
    @Query("""
        UPDATE SessionEntity s
        SET s.isActive = false, s.revokedAt = CURRENT_TIMESTAMP
        WHERE s.user.idUser = :userId AND s.isActive = true AND s.token != :currentToken
    """)
    void revokeAllByUserIdExceptToken(@Param("userId") Integer userId,
                                      @Param("currentToken") String currentToken);
}