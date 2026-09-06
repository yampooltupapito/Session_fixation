package com.botica.demo.controller;

import com.botica.demo.repository.ProductoRepository;
import com.botica.demo.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardController(ProductoRepository productoRepository, UsuarioRepository usuarioRepository) {
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        model.addAttribute("nombreCompleto", session.getAttribute("nombreCompleto"));
        model.addAttribute("rol", session.getAttribute("rol"));
        model.addAttribute("totalProductos", productoRepository.count());
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        // El ID de sesion se muestra unicamente con fines academicos,
        // para poder comparar su valor antes y despues del login en la demo.
        model.addAttribute("sessionId", session.getId());
        return "dashboard";
    }
}
