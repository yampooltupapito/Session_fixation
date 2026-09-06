package com.botica.demo.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro simple (sin Spring Security) que protege las rutas internas
 * verificando el atributo "usuario" en la sesion HTTP.
 */
@Component
public class SessionAuthFilter implements Filter {

    private static final String[] RUTAS_PUBLICAS = {
            "/login", "/css", "/js", "/h2-console"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();

        for (String publica : RUTAS_PUBLICAS) {
            if (path.startsWith(publica)) {
                chain.doFilter(req, res);
                return;
            }
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("/login");
            return;
        }

        chain.doFilter(req, res);
    }
}
