package com.botica.demo.controller;

import com.botica.demo.model.Usuario;
import com.botica.demo.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Bandera SOLO con fines academicos: permite alternar en vivo entre
     * el comportamiento VULNERABLE (para la demo de Session Fixation) y
     * el comportamiento CORREGIDO, sin tocar el codigo.
     * Se configura en application.properties -> app.security.session-fixation-vulnerable
     */
    @Value("${app.security.session-fixation-vulnerable:true}")
    private boolean sessionFixationVulnerable;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                         @RequestParam String password,
                         HttpServletRequest request,
                         Model model) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        boolean credencialesValidas = usuarioOpt.isPresent()
                && passwordEncoder.matches(password, usuarioOpt.get().getPassword());

        if (!credencialesValidas) {
            model.addAttribute("error", "Usuario o contrasena incorrectos");
            return "login";
        }

        // request.getSession(true) REUTILIZA la sesion existente si ya habia una
        // (por ejemplo, una que un atacante hubiera "fijado" en el navegador
        // de la victima antes de que esta iniciara sesion).
        HttpSession session = request.getSession(true);

        if (sessionFixationVulnerable) {
            // ------------------------------------------------------------
            // VULNERABLE A SESSION FIXATION (a proposito, para la demo)
            // No se genera un nuevo ID de sesion tras autenticar.
            // Si el ID de sesion ya era conocido por un atacante antes del
            // login, seguira siendo un ID valido y autenticado despues.
            // ------------------------------------------------------------
        } else {
            // ------------------------------------------------------------
            // CORRECCION: se regenera el ID de sesion (manteniendo los
            // atributos ya guardados), invalidando cualquier ID que un
            // atacante pudiera haber fijado de antemano.
            // ------------------------------------------------------------
            request.changeSessionId();
        }

        session.setAttribute("usuario", usuarioOpt.get().getUsername());
        session.setAttribute("rol", usuarioOpt.get().getRol());
        session.setAttribute("nombreCompleto", usuarioOpt.get().getNombreCompleto());

        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}
