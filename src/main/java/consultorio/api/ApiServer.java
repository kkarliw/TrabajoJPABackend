package consultorio.api;

import consultorio.api.controller.EstadisticasController;
import consultorio.api.controller.NotificacionController;
import consultorio.api.controller.ProfesionalSaludController;
import consultorio.api.dto.LoginRequest;
import consultorio.api.dto.RegistroRequest;
import io.jsonwebtoken.Claims;
import org.mindrot.jbcrypt.BCrypt;
import consultorio.modelo.*;
import consultorio.modelo.profesionales.ProfesionalSalud;
import consultorio.persistencia.*;
import consultorio.seguridad.JwtUtils;
import consultorio.util.PdfGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import consultorio.LocalDateAdapter;
import static spark.Spark.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ApiServer {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    public static class CitaInput {
        public Long pacienteId;
        public Long profesionalId;
        public String fecha;
        public String motivo;
    }

    public static void main(String[] args) {
        port(4567);
        enableCORS();

        // DAOs
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        PacienteDAO pacienteDAO = new PacienteDAO();
        ProfesionalSaludDAO profDAO = new ProfesionalSaludDAO();
        CitaDAO citaDAO = new CitaDAO();
        HistoriaClinicaDAO historiaDAO = new HistoriaClinicaDAO();
        IncapacidadDAO incapacidadDAO = new IncapacidadDAO();
        CuidadorDAO cuidadorDAO = new CuidadorDAO();

        NotificacionController.registerRoutes(gson);
        EstadisticasController.registerRoutes(gson);
        ProfesionalSaludController.registerRoutes(gson);

        // ---------------- FILTRO JWT ----------------
        before("/api/*", (req, res) -> {
            String path = req.pathInfo();
            if (path.equals("/api/auth/register") || path.equals("/api/auth/login")) return;

            String auth = req.headers("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                halt(401, "{\"error\":\"Token faltante o inválido\"}");
            }
            Claims claims = (Claims) JwtUtils.validateToken(auth.substring(7));
            req.attribute("rol", claims.get("rol", String.class));
        });

        // ---------------- REGISTER / LOGIN ----------------
        post("/api/auth/register", (req, res) -> {
            res.type("application/json"); // ✅ Forzar JSON aquí

            RegistroRequest r;
            try {
                r = gson.fromJson(req.body(), RegistroRequest.class);
            } catch (Exception e) {
                res.status(400);
                return "{\"error\":\"JSON inválido: " + e.getMessage() + "\"}";
            }

            // Validaciones
            if (r.getEmail() == null || r.getPassword() == null || r.getNombre() == null) {
                res.status(400);
                return "{\"error\":\"Faltan campos obligatorios (nombre, email, password)\"}";
            }

            String emailNormalized = r.getEmail().toLowerCase().trim();

            // Verificar si ya existe
            if (usuarioDAO.buscarPorEmail(emailNormalized) != null) {
                res.status(409);
                return "{\"error\":\"El correo ya está registrado\"}";
            }

            // Hashear contraseña
            String hash = BCrypt.hashpw(r.getPassword(), BCrypt.gensalt());

            // Asignar rol (default: PACIENTE)
            String rol = (r.getRol() != null && !r.getRol().isEmpty())
                    ? r.getRol().toUpperCase()
                    : "PACIENTE";

            if (!rol.matches("ADMIN|PACIENTE|MEDICO|CUIDADOR")) {
                rol = "PACIENTE";
            }

            // Crear usuario
            Usuario u = new Usuario(emailNormalized, r.getNombre(), hash, rol);

            try {
                usuarioDAO.crear(u);
                res.status(201);

                // ✅ No devolver el hash en la respuesta
                return gson.toJson(java.util.Map.of(
                        "id", u.getId(),
                        "email", u.getEmail(),
                        "nombre", u.getNombre(),
                        "rol", u.getRol()
                ));
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"Error al crear usuario: " + e.getMessage() + "\"}";
            }
        });

        post("/api/auth/login", (req, res) -> {
            res.type("application/json"); // ✅ Forzar JSON

            LoginRequest lr;
            try {
                lr = gson.fromJson(req.body(), LoginRequest.class);
            } catch (Exception e) {
                res.status(400);
                return "{\"error\":\"JSON inválido\"}";
            }

            if (lr.getEmail() == null || lr.getPassword() == null) {
                res.status(400);
                return "{\"error\":\"Faltan campos\"}";
            }

            Usuario u = usuarioDAO.buscarPorEmail(lr.getEmail().toLowerCase().trim());
            if (u == null || !BCrypt.checkpw(lr.getPassword(), u.getPasswordHash())) {
                res.status(401);
                return "{\"error\":\"Credenciales incorrectas\"}";
            }

            String token = JwtUtils.generateToken(u.getId(), u.getRol());
            res.status(200);

            // ✅ CORRECCIÓN: Devolver objeto user completo
            return gson.toJson(java.util.Map.of(
                    "token", token,
                    "rol", u.getRol(),
                    "nombre", u.getNombre(),
                    "id", u.getId(),
                    "email", u.getEmail()
            ));
        });

        // ---------------- CUIDADORES ----------------
        get("/api/cuidadores", (req, res) -> {
            res.type("application/json");
            return gson.toJson(cuidadorDAO.listarTodos());
        });

        get("/api/cuidadores/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            Cuidador c = cuidadorDAO.buscarPorId(id);
            if (c == null) {
                res.status(404);
                return "{}";
            }
            res.type("application/json");
            return gson.toJson(c);
        });

        post("/api/cuidadores", (req, res) -> {
            Cuidador c = gson.fromJson(req.body(), Cuidador.class);
            cuidadorDAO.crear(c);
            res.status(201);
            res.type("application/json");
            return gson.toJson(c);
        });

        delete("/api/cuidadores/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            cuidadorDAO.eliminar(id);
            res.status(204);
            return "";
        });

        // ---------------- PACIENTES ----------------
        post("/api/pacientes", (req, res) -> {
            Paciente p = gson.fromJson(req.body(), Paciente.class);
            pacienteDAO.crear(p);
            res.status(201);
            res.type("application/json");
            return gson.toJson(p);
        });

        get("/api/pacientes", (req, res) -> {
            List<Paciente> list = pacienteDAO.buscarTodos();
            res.type("application/json");
            return gson.toJson(list);
        });

        get("/api/pacientes/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            Paciente p = pacienteDAO.buscarPorId(id);
            if (p == null) {
                res.status(404);
                return "{}";
            }
            res.type("application/json");
            return gson.toJson(p);
        });

        put("/api/pacientes/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            Paciente p = gson.fromJson(req.body(), Paciente.class);
            p.setId(Math.toIntExact(id));
            pacienteDAO.actualizar(p);
            res.status(200);
            res.type("application/json");
            return gson.toJson(p);
        });

        delete("/api/pacientes/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            pacienteDAO.eliminar(id);
            res.status(204);
            return "";
        });

        // ---------------- PROFESIONALES ----------------
        post("/api/profesionales", (req, res) -> {
            ProfesionalSalud prof = gson.fromJson(req.body(), ProfesionalSalud.class);
            profDAO.crear(prof);
            res.status(201);
            res.type("application/json");
            return gson.toJson(prof);
        });

        get("/api/profesionales", (req, res) -> {
            List<ProfesionalSalud> list = profDAO.buscarTodos();
            res.type("application/json");
            return gson.toJson(list);
        });

        get("/api/profesionales/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            ProfesionalSalud prof = (ProfesionalSalud) profDAO.buscarPorId(id);
            if (prof == null) {
                res.status(404);
                return "{}";
            }
            res.type("application/json");
            return gson.toJson(prof);
        });

        put("/api/profesionales/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            ProfesionalSalud prof = gson.fromJson(req.body(), ProfesionalSalud.class);
            prof.setId(Math.toIntExact(id));
            profDAO.actualizar(prof);
            res.status(200);
            res.type("application/json");
            return gson.toJson(prof);
        });

        delete("/api/profesionales/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            profDAO.eliminar(id);
            res.status(204);
            return "";
        });

        post("/api/citas", (req, res) -> {
            res.type("application/json");

            CitaInput input;
            try {
                input = gson.fromJson(req.body(), CitaInput.class);
            } catch (Exception e) {
                res.status(400);
                return "{\"error\": \"JSON inválido: " + e.getMessage() + "\"}";
            }

            // Validaciones
            if (input.fecha == null || input.fecha.trim().isEmpty()) {
                res.status(400);
                return "{\"error\": \"El campo 'fecha' es obligatorio.\"}";
            }

            if (input.pacienteId == null || input.profesionalId == null) {
                res.status(400);
                return "{\"error\": \"Debe especificar paciente y profesional.\"}";
            }

            // Buscar entidades
            Paciente p = pacienteDAO.buscarPorId(input.pacienteId);
            ProfesionalSalud prof = (ProfesionalSalud) profDAO.buscarPorId(input.profesionalId);

            if (p == null || prof == null) {
                res.status(400);
                return "{\"error\": \"Paciente o profesional no existe.\"}";
            }

            // Parsear fecha
            LocalDate fecha;
            try {
                fecha = LocalDate.parse(input.fecha);
            } catch (DateTimeParseException e) {
                res.status(400);
                return "{\"error\": \"Formato de fecha inválido. Use YYYY-MM-DD.\"}";
            }

            // ✅ Crear cita usando el constructor correcto
            Cita c = new Cita(p, prof, fecha, input.motivo);

            try {
                citaDAO.crear(c);
                res.status(201);
                return gson.toJson(c);
            } catch (Exception e) {
                res.status(500);
                return "{\"error\": \"Error al crear la cita: " + e.getMessage() + "\"}";
            }
        });

        get("/api/citas", (req, res) -> {
            res.type("application/json");
            List<Cita> list = citaDAO.buscarTodos();
            return gson.toJson(list);
        });

        get("/api/citas/:id", (req, res) -> {
            res.type("application/json");
            Long id = Long.parseLong(req.params(":id"));
            Cita c = citaDAO.buscarPorId(id);
            if (c == null) {
                res.status(404);
                return "{\"error\":\"Cita no encontrada\"}";
            }
            return gson.toJson(c);
        });

// ✅ NUEVO: Endpoint para buscar citas por paciente
        get("/api/citas/paciente/:pacienteId", (req, res) -> {
            res.type("application/json");
            Long pacienteId = Long.parseLong(req.params(":pacienteId"));
            List<Cita> citas = citaDAO.buscarPorPaciente(pacienteId);
            return gson.toJson(citas);
        });

        put("/api/citas/:id", (req, res) -> {
            res.type("application/json");
            Long id = Long.parseLong(req.params(":id"));

            Cita citaExistente = citaDAO.buscarPorId(id);
            if (citaExistente == null) {
                res.status(404);
                return "{\"error\":\"Cita no encontrada\"}";
            }

            try {
                CitaInput input = gson.fromJson(req.body(), CitaInput.class);

                // Actualizar campos si vienen en el request
                if (input.fecha != null && !input.fecha.isEmpty()) {
                    citaExistente.setFecha(LocalDate.parse(input.fecha));
                }
                if (input.motivo != null) {
                    citaExistente.setMotivo(input.motivo);
                }

                citaDAO.actualizar(citaExistente);
                res.status(200);
                return gson.toJson(citaExistente);
            } catch (Exception e) {
                res.status(500);
                return "{\"error\": \"Error al actualizar cita: " + e.getMessage() + "\"}";
            }
        });

        delete("/api/citas/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));

            Cita c = citaDAO.buscarPorId(id);
            if (c == null) {
                res.status(404);
                return "{\"error\":\"Cita no encontrada\"}";
            }

            try {
                citaDAO.eliminar(id);
                res.status(204);
                return "";
            } catch (Exception e) {
                res.status(500);
                return "{\"error\": \"Error al eliminar cita: " + e.getMessage() + "\"}";
            }
        });

        // ---------------- HISTORIAS CLÍNICAS ----------------
        post("/api/historias", (req, res) -> {
            java.util.Map<String, Object> input = gson.fromJson(req.body(), java.util.Map.class);
            try {
                Long pacienteId = ((Number) input.get("pacienteId")).longValue();
                Long profesionalId = ((Number) input.get("profesionalId")).longValue();
                String fechaStr = (String) input.get("fecha");
                String motivo = (String) input.getOrDefault("motivoConsulta", "");
                String diagnostico = (String) input.getOrDefault("diagnostico", "");
                String tratamiento = (String) input.getOrDefault("tratamiento", "");
                String observaciones = (String) input.getOrDefault("observaciones", "");
                String formula = (String) input.getOrDefault("formulaMedica", "");
                boolean requiereIncapacidad = Boolean.parseBoolean(String.valueOf(input.getOrDefault("requiereIncapacidad", "false")));

                Paciente paciente = pacienteDAO.buscarPorId(pacienteId);
                ProfesionalSalud prof = (ProfesionalSalud) profDAO.buscarPorId(profesionalId);

                LocalDate fecha = LocalDate.parse(fechaStr);
                HistoriaClinica h = new HistoriaClinica(paciente, prof, fecha, motivo, diagnostico, tratamiento, observaciones, formula, requiereIncapacidad);
                historiaDAO.crear(h);

                if (requiereIncapacidad) {
                    Incapacidad inc = new Incapacidad(paciente, prof, fecha, 3, "Incapacidad generada desde historia clínica");
                    incapacidadDAO.crear(inc);
                }

                res.status(201);
                res.type("application/json");
                return gson.toJson(h);
            } catch (Exception ex) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", "Error al crear historia", "detalle", ex.getMessage()));
            }
        });

        get("/api/historias", (req, res) -> {
            res.type("application/json");
            return gson.toJson(historiaDAO.buscarTodos());
        });

        get("/api/historias/paciente/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            res.type("application/json");
            return gson.toJson(historiaDAO.buscarPorPaciente(id));
        });

        get("/api/historias/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            HistoriaClinica h = historiaDAO.buscarPorId(id);
            if (h == null) {
                res.status(404);
                return "{}";
            }
            res.type("application/json");
            return gson.toJson(h);
        });

        get("/api/historias/:id/pdf", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            HistoriaClinica h = historiaDAO.buscarPorId(id);
            if (h == null) {
                res.status(404);
                return "{\"error\":\"Historia no encontrada\"}";
            }
            try {
                byte[] pdf = PdfGenerator.generarHistoria(h);
                res.status(200);
                res.type("application/pdf");
                res.header("Content-Disposition", "attachment; filename=historia_" + id + ".pdf");
                res.raw().getOutputStream().write(pdf);
                res.raw().getOutputStream().flush();
                return res.raw();
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", "Error generando PDF", "detalle", e.getMessage()));
            }
        });
    }

    private static void enableCORS() {
        // 1️⃣ Manejar peticiones OPTIONS primero (preflight)
        options("/*", (request, response) -> {
            String headers = request.headers("Access-Control-Request-Headers");
            if (headers != null) {
                response.header("Access-Control-Allow-Headers", headers);
            }
            String methods = request.headers("Access-Control-Request-Method");
            if (methods != null) {
                response.header("Access-Control-Allow-Methods", methods);
            }
            return "OK";
        });

        // 2️⃣ Aplicar headers CORS ANTES de otros filtros
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");

            // ⚠️ NO forzar application/json aquí - déjalo para cada endpoint
            // response.type("application/json"); // ❌ ELIMINAR ESTA LÍNEA
        });

        // 3️⃣ Añadir Content-Type solo en endpoints específicos
        after((request, response) -> {
            if (!response.raw().containsHeader("Content-Type") &&
                    !request.requestMethod().equals("OPTIONS")) {
                response.type("application/json");
            }
        });
    }
}
