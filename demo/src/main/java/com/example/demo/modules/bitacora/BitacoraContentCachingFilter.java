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
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Locale;

/**
 * Envuelve la petición y la respuesta para que el interceptor pueda leer el
 * cuerpo enviado y el cuerpo devuelto por el controlador.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BitacoraContentCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!esAuditable(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletRequest requestParaCadena = esCacheable(request)
                ? new ContentCachingRequestWrapper(request, 20000)
                : request;
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(requestParaCadena, responseWrapper);
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }

    private boolean esAuditable(HttpServletRequest request) {
        String metodo = request.getMethod();
        boolean escritura = "POST".equals(metodo) || "PUT".equals(metodo)
                || "PATCH".equals(metodo) || "DELETE".equals(metodo);
        String uri = request.getRequestURI();
        return escritura && uri != null && uri.startsWith("/api/v1/");
    }

    private boolean esCacheable(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).contains("json");
    }
}
