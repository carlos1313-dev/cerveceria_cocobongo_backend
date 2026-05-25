package com.cocobongo.cerveceria.outgoings.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cocobongo.cerveceria.common.dto.ApiResponse;
import com.cocobongo.cerveceria.outgoings.dto.BalanceReport;
import com.cocobongo.cerveceria.outgoings.dto.OutgoingRequestDTO;
import com.cocobongo.cerveceria.outgoings.dto.OutgoingResponseDTO;
import com.cocobongo.cerveceria.outgoings.services.OutgoingsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/outgoings")
@RequiredArgsConstructor
public class OutgoingsController {

    @Autowired
    private OutgoingsService outgoings;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<OutgoingResponseDTO>> create(
            @Valid @RequestBody OutgoingRequestDTO request) {
        OutgoingResponseDTO newOutgoing = outgoings.create(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Gasto creado", newOutgoing));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OutgoingResponseDTO>>> getAll(
            @PageableDefault(size = 20, sort = "idOutgoing") Pageable pageable) {
        Page<OutgoingResponseDTO> page = outgoings.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));

    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OutgoingResponseDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(outgoings.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OutgoingResponseDTO>> update(@PathVariable Integer id,
            @Valid @RequestBody OutgoingRequestDTO uOutgoing) {
        OutgoingResponseDTO upOutgoing = outgoings.update(uOutgoing, id);
        return ResponseEntity.ok(ApiResponse.ok("Gasto actualizado", upOutgoing));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BalanceReport>> genBalance(@RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin, @RequestParam Integer idBranch) {
                return ResponseEntity.ok(ApiResponse.ok("Balance de la sucursal", outgoings.generarBalance(inicio, fin, idBranch)));
    }

}
