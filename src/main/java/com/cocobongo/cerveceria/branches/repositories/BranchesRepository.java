package com.cocobongo.cerveceria.branches.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cocobongo.cerveceria.branches.entities.BranchEntity;

public interface BranchesRepository extends JpaRepository<BranchEntity, Integer>{

}
