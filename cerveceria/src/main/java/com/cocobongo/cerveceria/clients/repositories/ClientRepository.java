package com.cocobongo.cerveceria.clients.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cocobongo.cerveceria.clients.entities.ClientEntity;

public interface ClientRepository extends JpaRepository<ClientEntity, Integer> {
}
 