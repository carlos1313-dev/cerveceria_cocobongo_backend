package com.cocobongo.cerveceria.branches.services;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.cocobongo.cerveceria.branches.dto.BranchRequestDTO;
import com.cocobongo.cerveceria.branches.dto.BranchResponseDTO;
import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.branches.repositories.BranchesRepository;
import com.cocobongo.cerveceria.common.exception.BusinessException;
import com.cocobongo.cerveceria.common.exception.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BranchesService {
          @Autowired
          private BranchesRepository repo;

          @Transactional
          public BranchResponseDTO createBranch(BranchRequestDTO new_branch) throws BusinessException {
                    log.info("Create a branch");

                    if (new_branch.getName() == null) {
                              throw new BusinessException("El nombre es obligatorio");
                    }

                    if (new_branch.isActive()) {
                              throw new BusinessException("El estado (inactivo o activo) es obligatorio");
                    }

                    BranchEntity b = BranchEntity.builder()
                                        .name(new_branch.getName())
                                        .city(new_branch.getCity())
                                        .address(new_branch.getAddress())
                                        .isActive(new_branch.isActive())
                                        .build();

                    repo.save(b);
                    return toResponseDTO(b);

          }

          @Transactional
          public BranchResponseDTO findBranch(Integer id) {
                    log.info("");
                    return repo.findById(id).map(this::toResponseDTO).orElseThrow(
                                        () -> new EntityNotFoundException("Entity with id " + id + " not found"));
          }

          @Transactional
          public Page<BranchResponseDTO> findAll(Pageable pageable) {
                    return repo.findAll(pageable)
                                        .map(this::toResponseDTO);
          }

          @Transactional
          public BranchResponseDTO updateBranch(BranchRequestDTO uBranch, Integer id) {
                    BranchEntity u = repo.findById(id)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                            "No se encontro una sucursal con el id: " + id));

                    if (uBranch.getName() == null) {
                              throw new BusinessException("El nombre es obligatorio");
                    }


                    Objects.requireNonNull(uBranch.isActive(), "El estado de la sucursal es obligatorio");

                    u.setName(uBranch.getName());
                    u.setAddress(uBranch.getAddress());
                    u.setCity(uBranch.getCity());
                    u.setIsActive(uBranch.isActive());

                    repo.save(u);
                    return toResponseDTO(u);

          }


          @Transactional
          public void deleteBranch(Integer id) {
                    repo.deleteById(id);
          }

          private BranchResponseDTO toResponseDTO(BranchEntity b) {
                    return new BranchResponseDTO(b);
          }

}
