package com.cocobongo.cerveceria.clients.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cocobongo.cerveceria.clients.entities.InstallmentEntity;

@Repository
public interface InstallmentRepository extends JpaRepository<InstallmentEntity,Integer> {

    // Historial de abonos de un cliente, más reciente primero
    List<InstallmentEntity> findByClientIdClientOrderByPaymentDateDesc(Integer idClient);

    // Suma total de abonos de un cliente
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM InstallmentEntity i " +
           "WHERE i.client.idClient = :idClient")
    BigDecimal sumByClient(@Param("idClient") Integer idClient);
}