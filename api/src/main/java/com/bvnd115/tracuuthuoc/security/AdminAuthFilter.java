package com.bvnd115.tracuuthuoc.security;

import com.bvnd115.tracuuthuoc.service.AdminSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AdminAuthFilter extends OncePerRequestFilter {

    private final AdminSessionService sessions;

    public AdminAuthFilter(AdminSessionService sessions) {
        this.sessions = sessions;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/admin");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isLogin(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!sessions.valid(request.getHeader("Authorization"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Phiên đăng nhập không hợp lệ\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isLogin(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && "/api/admin/login".equals(request.getRequestURI());
    }
}
