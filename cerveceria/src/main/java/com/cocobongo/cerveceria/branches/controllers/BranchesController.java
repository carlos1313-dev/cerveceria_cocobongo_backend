package com.cocobongo.cerveceria.branches.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cocobongo.cerveceria.branches.dto.BranchResponseDTO;
import com.cocobongo.cerveceria.branches.entities.BranchEntity;
import com.cocobongo.cerveceria.branches.services.BranchesService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@PreAuthorize("hasrole('ADMIN')")
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchesController {
          @Autowired
          private BranchesService branch;

          @GetMapping
          public ResponseEntity<ApiResponse<Page<BranchResponseDTO>>> getBranches() {
                    Page<BranchResponseDTO>  b;
                    return b;
          }

          @GetMapping("/{id}")
          public ResponseEntity<ApiResponse<BranchResponseDTO>> getBranch(@RequestParam Integer id) {
              branch.findBranch(id);
          }
          
          

}
