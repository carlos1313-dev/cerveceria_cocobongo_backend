package com.cocobongo.cerveceria.exchangerate.repositories;
 
import com.cocobongo.cerveceria.exchangerate.entities.ExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
 
import java.util.Optional;
 
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Integer> {
 
    /**
     * Tasa más reciente — la que está vigente en este momento.
     * Se usa en todo el sistema: ventas, cálculo de precios, reportes.
     */
    @Query("SELECT e FROM ExchangeRateEntity e ORDER BY e.registeredAt DESC LIMIT 1")
    Optional<ExchangeRateEntity> findLatest();
}