package com.cocobongo.cerveceria.branches.services;

import java.util.List;

import org.hibernate.query.IllegalQueryOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.cocobongo.cerveceria.branches.dto.BranchResponseDTO;
import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.branches.repositories.BranchesRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BranchesService {
          @Autowired
          private BranchesRepository repo;

          @Transactional
          public BranchEntity createBranch(BranchEntity branch) throws IllegalQueryOperationException{
                    log.info("Create a branch");

                    if(branch.getName() == null){
                              throw new IllegalQueryOperationException("Name is mandatory");
                    }

                    if(branch.is_active()){
                              throw new IllegalQueryOperationException("Active is mandatory");
                    }
                    return repo.save(branch);
          }

          @Transactional
          public BranchEntity findBranch(Integer id){
                    log.info("");
                    return repo.findById(id).orElseThrow(()-> new EntityNotFoundException("Entity with id "+ id + " not found"));
          }

          @Transactional
          public Page<BranchResponseDTO> findAll(){
                    Page<BranchResponseDTO> b;
                    return repo.findAll(Page<BranchResponseDTO> b).map(this::toResponseDTO);
          }

          @Transactional
          public BranchEntity updateBranch(BranchEntity branch, Integer id){
                    BranchEntity existing = repo.findById(id).orElseThrow(()-> new EntityNotFoundException("Entity with id "+ id + " not found"));
                    
                    existing.setAddress(branch.getAddress());
                    existing.setCity(branch.getCity());
                    existing.setName(branch.getName());
                    existing.set_active(branch.is_active());

                    return repo.save(existing);

          }

          @Transactional
          public void deleteBranch(Integer id){
                    repo.deleteById(id);
          }

          private BranchResponseDTO toResponseDTO(BranchEntity b){
                    return new BranchResponseDTO(b);
          }
}
