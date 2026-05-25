package com.cocobongo.cerveceria.outgoings.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cocobongo.cerveceria.outgoings.entities.OutgoingEntity;

public interface OutgoingsRepository extends JpaRepository<OutgoingEntity, Integer> {

  @Query("""
          SELECT COALESCE(SUM(o.total), 0)
          FROM OutgoingEntity o
          WHERE o.idBranch = :branchId
            AND o.date BETWEEN :from AND :to
      """)
  BigDecimal sumarGastos(
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      @Param("branchId") Integer branchId);
}
