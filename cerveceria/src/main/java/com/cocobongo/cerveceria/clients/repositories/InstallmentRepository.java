package com.cocobongo.cerveceria.clients.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cocobongo.cerveceria.clients.entities.InstallmentEntity;

public interface InstallmentRepository extends JpaRepository<InstallmentEntity, Integer> {

}
