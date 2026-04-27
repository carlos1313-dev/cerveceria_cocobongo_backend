package com.cocobongo.cerveceria.auth.controllers;
 
import com.cocobongo.cerveceria.auth.dto.*;
import com.cocobongo.cerveceria.auth.entities.AuditEntity;
import com.cocobongo.cerveceria.users.entities.UserEntity;
import com.cocobongo.cerveceria.auth.services.AuditService;
import com.cocobongo.cerveceria.auth.services.AuthService;
import com.cocobongo.cerveceria.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
 
import java.time.LocalDateTime;
 
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {
 
    private final AuthService  authService;
    private final AuditService auditService;
 
    // ── POST /api/v1/auth/register ─────────────────────────────────────────────
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
 
        authService.register(request, httpRequest.getRemoteAddr());
        return ResponseEntity.status(201)
                .body(ApiResponse.ok("Administrador registrado correctamente"));
    }
 
    // ── POST /api/v1/auth/login ────────────────────────────────────────────────
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
 
        LoginResponse response = authService.login(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
 
    // ── POST /api/v1/auth/logout ───────────────────────────────────────────────
    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {
 
        String token = authHeader.substring(7);
        authService.logout(token, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada correctamente"));
    }
 
    // ── POST /api/v1/auth/forgot-password ─────────────────────────────────────
    @PostMapping("/auth/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
 
        authService.forgotPassword(request, httpRequest.getRemoteAddr());
        // Siempre responder con el mismo mensaje para no revelar si el correo existe
        return ResponseEntity.ok(
                ApiResponse.ok("Si el correo existe, recibirás instrucciones"));
    }
 
    // ── POST /api/v1/auth/reset-password ──────────────────────────────────────
    @PostMapping("/auth/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {
 
        authService.resetPassword(request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Contraseña restablecida correctamente"));
    }
 
    // ── PUT /api/v1/auth/change-password ──────────────────────────────────────
    @PutMapping("/auth/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {
 
        String token = authHeader.substring(7);
        authService.changePassword(request, currentUser, token,
                httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada correctamente"));
    }
 
    // ── GET /api/v1/auth/me ────────────────────────────────────────────────────
    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(
            @AuthenticationPrincipal UserEntity currentUser) {
 
        MeResponse me = new MeResponse();
        me.setId(currentUser.getIdUser());
        me.setName(currentUser.getName());
        me.setEmail(currentUser.getEmail());
        me.setRole(currentUser.getRole());
        me.setBranchId(currentUser.getIdBranch());
        me.setActive(currentUser.isActive());
        return ResponseEntity.ok(ApiResponse.ok(me));
    }
 
    // ── GET /api/v1/audit ──────────────────────────────────────────────────────
    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditEntity>>> getAudit(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String  action,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
 
        Page<AuditEntity> page = auditService.findByFilters(userId, action, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }
}