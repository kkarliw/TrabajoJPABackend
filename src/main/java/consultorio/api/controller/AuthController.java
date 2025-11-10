package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.api.dto.LoginRequest;
import consultorio.api.dto.RegistroRequest;
import consultorio.modelo.Usuario;
import consultorio.persistencia.UsuarioDAO;
import consultorio.seguridad.JwtUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;
import java.util.Map;

import static spark.Spark.*;

public class AuthController {
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public static void registerRoutes(Gson gson) {
        path("/api/auth", () -> {

            // Registro
            post("/register", (req, res) -> {
                res.type("application/json");

                RegistroRequest r;
                try {
                    r = gson.fromJson(req.body(), RegistroRequest.class);
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "JSON inválido: " + e.getMessage()));
                }

                if (r.getEmail() == null || r.getPassword() == null || r.getNombre() == null) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Faltan campos obligatorios (nombre, email, password)"));
                }

                String emailNormalized = r.getEmail().toLowerCase().trim();

                if (usuarioDAO.buscarPorEmail(emailNormalized) != null) {
                    res.status(409);
                    return gson.toJson(Map.of("error", "El correo ya está registrado"));
                }

                String hash = BCrypt.hashpw(r.getPassword(), BCrypt.gensalt());

                String rol = (r.getRol() != null && !r.getRol().isEmpty())
                        ? r.getRol().toUpperCase()
                        : "PACIENTE";

                if (!rol.matches("ADMIN|PACIENTE|MEDICO|CUIDADOR|RECEPCIONISTA")) {
                    rol = "PACIENTE";
                }

                Usuario u = new Usuario(emailNormalized, r.getNombre(), hash, rol);
                u.setApellido(r.getApellido());

                try {
                    usuarioDAO.crear(u);
                    res.status(201);
                    Map<? super String, ? super Serializable> token = Map.of();
                    return gson.toJson(Map.of(
                            "token", token,
                            "user", Map.of(  // ✅ Debe estar anidado en "user"
                                    "id", u.getId(),
                                    "email", u.getEmail(),
                                    "nombre", u.getNombre(),
                                    "apellido", u.getApellido() != null ? u.getApellido() : "",
                                    "rol", u.getRol()
                            )
                    ));
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Error al crear usuario: " + e.getMessage()));
                }
            });

            // Login
            post("/login", (req, res) -> {
                res.type("application/json");

                LoginRequest lr;
                try {
                    lr = gson.fromJson(req.body(), LoginRequest.class);
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "JSON inválido"));
                }

                if (lr.getEmail() == null || lr.getPassword() == null) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Faltan campos (email o password)"));
                }

                Usuario u = usuarioDAO.buscarPorEmail(lr.getEmail().toLowerCase().trim());
                if (u == null || !BCrypt.checkpw(lr.getPassword(), u.getPasswordHash())) {
                    res.status(401);
                    return gson.toJson(Map.of("error", "Credenciales incorrectas"));
                }

                String token = JwtUtils.generateToken(u.getId(), u.getRol());
                res.status(200);
                return gson.toJson(Map.of(
                        "token", token,
                        "user", Map.of(
                                "id", u.getId(),
                                "email", u.getEmail(),
                                "nombre", u.getNombre(),
                                "apellido", u.getApellido() != null ? u.getApellido() : "",
                                "rol", u.getRol()
                        )
                ));
            });
        });
    }
}
