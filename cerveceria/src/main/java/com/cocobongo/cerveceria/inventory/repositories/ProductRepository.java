package com.cocobongo.cerveceria.inventory.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cocobongo.cerveceria.inventory.entities.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Integer>{

          

}
