package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.api.dto.LoginRequest;
import consultorio.api.dto.RegistroRequest;
import consultorio.modelo.Usuario;
import consultorio.persistencia.UsuarioDAO;
import consultorio.seguridad.JwtUtils;
import org.mindrot.jbcrypt.BCrypt;

import static spark.Spark.*;

public class UsuarioController {
    private static UsuarioDAO usuarioDAO = new UsuarioDAO();

    public static void registerRoutes(Gson gson) {

        // Define el prefijo de todas las rutas de autenticación
        path("/api/auth", () -> {

            // 🎯 ENDPOINT DE REGISTRO: /api/auth/register
            post("/register", (req, res) -> {
                RegistroRequest r = gson.fromJson(req.body(), RegistroRequest.class);

                // --- Validaciones y Lógica ---
                if (r.getEmail() == null || r.getPassword() == null) {
                    res.status(400);
                    return gson.toJson("{\"error\":\"Faltan campos (email o password)\"}");
                }
                if (usuarioDAO.buscarPorEmail(r.getEmail()) != null) {
                    res.status(409);
                    return gson.toJson("{\"error\":\"Correo ya registrado\"}");
                }

                // 1. Hashear Contraseña
                String hash = BCrypt.hashpw(r.getPassword(), BCrypt.gensalt());

                // 2. ¡CORRECCIÓN CLAVE! Usa r.getRol() para el rol
                Usuario u = new Usuario(r.getEmail(), r.getNombre(), hash, r.getRol());

                // 3. Guardar en la BD
                usuarioDAO.crear(u);

                res.status(201);
                return gson.toJson(u);
            });

            // 🎯 ENDPOINT DE LOGIN: /api/auth/login
            post("/login", (req, res) -> {
                LoginRequest lr = gson.fromJson(req.body(), LoginRequest.class);
                Usuario u = usuarioDAO.buscarPorEmail(lr.getEmail());

                // --- Validación de Credenciales ---
                if (u == null || !BCrypt.checkpw(lr.getPassword(), u.getPasswordHash())) {
                    res.status(401);
                    return gson.toJson("{\"error\":\"Credenciales inválidas\"}");
                }

                // 3. Generar Token JWT
                String token = JwtUtils.generateToken(u.getId(), u.getRol());

                res.status(200);
                return gson.toJson(java.util.Map.of("token", token, "rol", u.getRol()));
            });
        });
    }
}