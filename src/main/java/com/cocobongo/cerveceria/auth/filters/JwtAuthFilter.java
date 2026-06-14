package com.cocobongo.cerveceria.auth.filters;
 
import com.cocobongo.cerveceria.users.entities.UserEntity;
import com.cocobongo.cerveceria.auth.repositories.SessionRepository;
import com.cocobongo.cerveceria.users.repositories.UserRepository;
import com.cocobongo.cerveceria.auth.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
 
import java.io.IOException;
 
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
 
    private final JwtUtils          jwtUtils;
    private final UserRepository    userRepository;
    private final SessionRepository sessionRepository;
 
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain
    ) throws ServletException, IOException {
 
        final String authHeader = request.getHeader("Authorization");
 
        // Si no hay token o no empieza con "Bearer ", dejar pasar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
 
        final String token = authHeader.substring(7).trim();
 
        try {
            final String email = jwtUtils.extractEmail(token);
 
            // Solo procesar si aún no hay autenticación en el contexto
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
 
                UserEntity user = userRepository.findByEmail(email).orElse(null);
 
                if (user == null) {
                    filterChain.doFilter(request, response);
                    return;
                }
 
                // Verificar que la sesión no haya sido revocada (logout explícito)
                boolean sessionActive = sessionRepository.findByToken(token)
                        .map(s -> s.isActive())
                        .orElse(false);
 
                if (jwtUtils.isTokenValid(token, user) && sessionActive) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ignored) {
            // Token malformado o expirado — Spring Security rechazará la request
        }
 
        filterChain.doFilter(request, response);
    }
}