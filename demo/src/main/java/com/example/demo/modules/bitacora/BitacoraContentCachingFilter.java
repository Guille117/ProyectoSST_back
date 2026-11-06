package com.example.demo.modules.bitacora;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Locale;

/**
 * Envuelve la petición en un {@link ContentCachingRequestWrapper} para que el
 * interceptor pueda leer el cuerpo (payload) después de que el controlador lo procese.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BitacoraContentCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (esCacheable(request)) {
            filterChain.doFilter(new ContentCachingRequestWrapper(request, 20000), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean esCacheable(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).contains("json");
    }
}
