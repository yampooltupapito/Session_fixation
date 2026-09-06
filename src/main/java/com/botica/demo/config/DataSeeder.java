package com.botica.demo.config;

import com.botica.demo.model.Producto;
import com.botica.demo.model.Usuario;
import com.botica.demo.repository.ProductoRepository;
import com.botica.demo.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Crea el usuario administrador por defecto y datos de ejemplo
 * al levantar la aplicacion, solo si la base de datos esta vacia.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataSeeder(UsuarioRepository usuarioRepository, ProductoRepository productoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario(
                    "admin",
                    passwordEncoder.encode("Admin123*"),
                    "ADMIN",
                    "Administrador del Sistema"
            );
            usuarioRepository.save(admin);
            System.out.println("=========================================================");
            System.out.println(" Usuario admin creado -> usuario: admin / clave: Admin123*");
            System.out.println("=========================================================");
        }

        if (productoRepository.count() == 0) {
            productoRepository.save(new Producto("Paracetamol 500mg", "Analgesicos", new BigDecimal("5.50"), 120));
            productoRepository.save(new Producto("Amoxicilina 500mg", "Antibioticos", new BigDecimal("12.00"), 60));
            productoRepository.save(new Producto("Ibuprofeno 400mg", "Antiinflamatorios", new BigDecimal("6.80"), 90));
            productoRepository.save(new Producto("Alcohol en gel 250ml", "Higiene", new BigDecimal("4.20"), 200));
        }
    }
}
