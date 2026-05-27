package com.cocobongo.cerveceria.branches.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cocobongo.cerveceria.branches.dto.BranchRequestDTO;
import com.cocobongo.cerveceria.branches.dto.BranchResponseDTO;
import com.cocobongo.cerveceria.branches.services.BranchesService;
import com.cocobongo.cerveceria.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchesController {
    @Autowired
    private BranchesService branch;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BranchResponseDTO>>> getAll(
            @PageableDefault(size = 20, sort = "idBranch") Pageable pageable) {
        Page<BranchResponseDTO> page = branch.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(branch.findBranch(id)));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<BranchResponseDTO>> create(
        @Valid @RequestBody BranchRequestDTO request) {
        
        BranchResponseDTO newBranch = branch.createBranch(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Sucursal creada", newBranch));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponseDTO>> update(@PathVariable Integer id, 
        @Valid @RequestBody BranchRequestDTO uBranch) {
            BranchResponseDTO updatedBranch = branch.updateBranch(uBranch, id);
            return ResponseEntity.ok(ApiResponse.ok("Sucursal actualizada", updatedBranch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        branch.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.ok("Sucursal eliminada", null));
    }
    
}
