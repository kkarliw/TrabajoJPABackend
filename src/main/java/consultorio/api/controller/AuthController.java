package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.api.dto.LoginRequest;
import consultorio.api.dto.RegistroRequest;
import consultorio.modelo.Paciente;
import consultorio.modelo.Usuario;
import consultorio.modelo.profesionales.ProfesionalSalud;
import consultorio.persistencia.PacienteDAO;
import consultorio.persistencia.ProfesionalSaludDAO;
import consultorio.persistencia.UsuarioDAO;
import consultorio.seguridad.JwtUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.util.Map;

import static spark.Spark.*;

public class AuthController {
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final PacienteDAO pacienteDAO = new PacienteDAO();
    private static final ProfesionalSaludDAO profesionalDAO = new ProfesionalSaludDAO();

    public static void registerRoutes(Gson gson) {
        path("/api/auth", () -> {

            // ============ REGISTRO ============
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

                // ✅ VALIDACIÓN DE ROL
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

                    // ✅ CREAR REGISTRO ESPECÍFICO SEGÚN ROL
                    if (rol.equals("PACIENTE")) {
                        crearPaciente(u, r);
                    } else if (rol.equals("MEDICO")) {
                        crearProfesional(u, r, "MEDICO");
                    }

                    // ✅ GENERAR TOKEN INMEDIATAMENTE
                    String token = JwtUtils.generateToken(u.getId(), u.getRol());

                    res.status(201);
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
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Error al crear usuario: " + e.getMessage()));
                }
            });

            // ============ LOGIN ============
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

    // ✅ CREAR PACIENTE AUTOMÁTICAMENTE
    private static void crearPaciente(Usuario u, RegistroRequest r) {
        try {
            Paciente p = new Paciente();
            p.setNombre(u.getNombre());
            p.setApellido(u.getApellido() != null ? u.getApellido() : "");
            p.setEmail(u.getEmail());
            p.setTelefono(r.getTelefono() != null ? r.getTelefono() : "");
            p.setDireccion(r.getDireccion() != null ? r.getDireccion() : "");

            // Campos obligatorios con valores por defecto
            p.setNumeroDocumento(r.getNumeroDocumento() != null ? r.getNumeroDocumento() : "POR_DEFINIR");
            p.setFechaNacimiento(r.getFechaNacimiento() != null ? r.getFechaNacimiento() : LocalDate.now());
            p.setGenero(r.getGenero() != null ? Paciente.Genero.valueOf(r.getGenero()) : Paciente.Genero.OTRO);

            pacienteDAO.crear(p);
            System.out.println("✅ Paciente creado automáticamente: " + p.getEmail());
        } catch (Exception e) {
            System.out.println("❌ Error al crear paciente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ CREAR PROFESIONAL AUTOMÁTICAMENTE
    private static void crearProfesional(Usuario u, RegistroRequest r, String tipo) {
        try {
            ProfesionalSalud prof = new ProfesionalSalud();
            prof.setNombre(u.getNombre());
            prof.setApellido(u.getApellido() != null ? u.getApellido() : "");
            prof.setEmail(u.getEmail());
            prof.setTelefono(r.getTelefono() != null ? r.getTelefono() : "");

            // Campos obligatorios con valores por defecto
            prof.setNumeroLicencia(r.getNumeroLicencia() != null ? r.getNumeroLicencia() : "POR_DEFINIR_" + u.getId());
            prof.setEspecialidad(r.getEspecialidad() != null ? r.getEspecialidad() : "Médico General");
            prof.setTipoProfesional(tipo);

            profesionalDAO.crear(prof);
            System.out.println("✅ Profesional creado automáticamente: " + prof.getEmail());
        } catch (Exception e) {
            System.out.println("❌ Error al crear profesional: " + e.getMessage());
            e.printStackTrace();
        }
    }
}