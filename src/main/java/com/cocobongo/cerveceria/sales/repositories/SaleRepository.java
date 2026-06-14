package com.cocobongo.cerveceria.sales.repositories;

import com.cocobongo.cerveceria.sales.entities.SaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface SaleRepository extends JpaRepository<SaleEntity, Integer> {
}