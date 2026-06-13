package com.cocobongo.cerveceria.clients.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cocobongo.cerveceria.clients.entities.ClientEntity;
import com.cocobongo.cerveceria.sales.entities.PaymentType;
import com.cocobongo.cerveceria.sales.entities.SaleStatus;
import com.cocobongo.cerveceria.sales.entities.SaleEntity;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Integer> {

    // Todos los clientes activos
    List<ClientEntity> findByIsActiveTrue();

    // Clientes activos con saldo pendiente > 0
    List<ClientEntity> findByIsActiveTrueAndBalanceGreaterThan(BigDecimal balance);

    // Cliente activo por id — evita retornar clientes desactivados
    Optional<ClientEntity> findByIdClientAndIsActiveTrue(Integer idClient);

    // Suma de ventas a crédito no anuladas para calcular deuda total
    @Query("SELECT COALESCE(SUM(s.total), 0) FROM SaleEntity s " +
           "JOIN s.payments p " +
           "WHERE s.client.idClient = :idClient " +
           "AND p.method = :paymentType " +
           "AND s.status <> :cancelled")
    BigDecimal sumCreditSalesByClient(
            @Param("idClient")    Integer       idClient,
            @Param("paymentType") PaymentType paymentType,
            @Param("cancelled")   SaleStatus  cancelled);

    // Ventas a crédito de un cliente para el estado de cuenta
    @Query("SELECT DISTINCT s FROM SaleEntity s " +
           "JOIN s.payments p " +
           "WHERE s.client.idClient = :idClient " +
           "AND p.method = :paymentType " +
           "ORDER BY s.saleDate DESC")
    List<SaleEntity> findCreditSalesByClient(
            @Param("idClient")    Integer       idClient,
            @Param("paymentType") PaymentType paymentType);
}
