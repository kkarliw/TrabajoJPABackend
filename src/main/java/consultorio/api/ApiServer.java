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
        ConsultorioDAO consultorioDAO = new ConsultorioDAO();

        NotificacionController.registerRoutes(gson);
        EstadisticasController.registerRoutes(gson);
        ProfesionalSaludController.registerRoutes(gson);

        // ✅ FILTRO JWT
        before("/api/*", (req, res) -> {
            String path = req.pathInfo();
            if (path.equals("/api/auth/register") || path.equals("/api/auth/login")) return;

            String auth = req.headers("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                halt(401, "{\"error\":\"Token faltante o inválido\"}");
            }
            Claims claims = (Claims) JwtUtils.validateToken(auth.substring(7));
            if (claims != null) {
                req.attribute("rol", claims.get("rol", String.class));
            }
        });

        // ============ AUTENTICACIÓN ============

        // 📝 REGISTRO CON VALIDACIÓN DE ROL - ✅ INCLUYE RECEPCIONISTA
        post("/api/auth/register", (req, res) -> {
            res.type("application/json");

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

            if (r.getRol() == null || r.getRol().isEmpty()) {
                res.status(400);
                return "{\"error\":\"El rol es obligatorio\"}";
            }

            String emailNormalized = r.getEmail().toLowerCase().trim();

            // Verificar si ya existe
            if (usuarioDAO.buscarPorEmail(emailNormalized) != null) {
                res.status(409);
                return "{\"error\":\"El correo ya está registrado\"}";
            }

            // ✅ VALIDAR ROL - AHORA INCLUYE RECEPCIONISTA
            String rol = r.getRol().toUpperCase().trim();
            if (!rol.matches("ADMIN|PACIENTE|MEDICO|RECEPCIONISTA|CUIDADOR")) {
                res.status(400);
                return "{\"error\":\"Rol inválido. Use: ADMIN, PACIENTE, MEDICO, RECEPCIONISTA, CUIDADOR\"}";
            }

            // Hashear contraseña
            String hash = BCrypt.hashpw(r.getPassword(), BCrypt.gensalt());

            // ✅ CREAR USUARIO CON EL ROL CORRECTO
            Usuario u = new Usuario(emailNormalized, r.getNombre(), hash, rol);

            try {
                usuarioDAO.crear(u);

                // ✅ NUEVO: Si PACIENTE, crear también en tabla pacientes
                if (rol.equals("PACIENTE")) {
                    Paciente p = new Paciente(r.getNombre(), null, emailNormalized, null, null, null, null);
                    pacienteDAO.crear(p);
                }

                // ✅ NUEVO: Si MEDICO, crear también en tabla profesionales
                if (rol.equals("MEDICO")) {
                    ProfesionalSalud prof = new ProfesionalSalud(r.getNombre(), null, "General", emailNormalized, null, null);
                    profDAO.crear(prof);
                }

                res.status(201);

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

        // 🔐 LOGIN - RETORNA ROL CORRECTAMENTE
        post("/api/auth/login", (req, res) -> {
            res.type("application/json");

            LoginRequest lr;
            try {
                lr = gson.fromJson(req.body(), LoginRequest.class);
            } catch (Exception e) {
                res.status(400);
                return "{\"error\":\"JSON inválido\"}";
            }

            if (lr.getEmail() == null || lr.getPassword() == null) {
                res.status(400);
                return "{\"error\":\"Faltan campos (email o password)\"}";
            }

            Usuario u = usuarioDAO.buscarPorEmail(lr.getEmail().toLowerCase().trim());
            if (u == null || !BCrypt.checkpw(lr.getPassword(), u.getPasswordHash())) {
                res.status(401);
                return "{\"error\":\"Credenciales incorrectas\"}";
            }

            // ✅ VERIFICAR ROL
            if (u.getRol() == null || u.getRol().isEmpty()) {
                res.status(500);
                return "{\"error\":\"Error: Usuario sin rol en BD\"}";
            }

            String token = JwtUtils.generateToken(u.getId(), u.getRol());
            res.status(200);

            return gson.toJson(java.util.Map.of(
                    "token", token,
                    "id", u.getId(),
                    "nombre", u.getNombre(),
                    "email", u.getEmail(),
                    "rol", u.getRol()
            ));
        });

        // ============ CITAS - CORREGIDO CON Integer ============
        get("/api/citas", (req, res) -> {
            res.type("application/json");
            List<Cita> list = citaDAO.buscarTodos();
            return gson.toJson(list);
        });

        // ✅ CORREGIDO: Integer en lugar de Long
        get("/api/citas/:id", (req, res) -> {
            res.type("application/json");
            Integer id = Integer.parseInt(req.params(":id"));
            Cita c = citaDAO.buscarPorId(Long.valueOf(id));
            if (c == null) {
                res.status(404);
                return "{\"error\":\"Cita no encontrada\"}";
            }
            return gson.toJson(c);
        });

        // ✅ CORREGIDO: Buscar por paciente
        get("/api/citas/paciente/:pacienteId", (req, res) -> {
            res.type("application/json");
            Integer pacienteId = Integer.parseInt(req.params(":pacienteId"));
            List<Cita> citas = citaDAO.buscarPorPaciente(Long.valueOf(pacienteId));
            return gson.toJson(citas);
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

            if (input.fecha == null || input.fecha.trim().isEmpty()) {
                res.status(400);
                return "{\"error\": \"El campo 'fecha' es obligatorio.\"}";
            }

            if (input.pacienteId == null || input.profesionalId == null) {
                res.status(400);
                return "{\"error\": \"Debe especificar paciente y profesional.\"}";
            }

            // ✅ CORREGIDO: Convertir Long a Integer
            Paciente p = pacienteDAO.buscarPorId(input.pacienteId.intValue());
            ProfesionalSalud prof = profDAO.buscarPorId(input.profesionalId.intValue());

            if (p == null || prof == null) {
                res.status(400);
                return "{\"error\": \"Paciente o profesional no existe.\"}";
            }

            LocalDate fecha;
            try {
                fecha = LocalDate.parse(input.fecha);
            } catch (DateTimeParseException e) {
                res.status(400);
                return "{\"error\": \"Formato de fecha inválido. Use YYYY-MM-DD.\"}";
            }

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

        // ✅ CORREGIDO: Integer
        put("/api/citas/:id", (req, res) -> {
            res.type("application/json");
            Integer id = Integer.parseInt(req.params(":id"));

            Cita citaExistente = citaDAO.buscarPorId(Long.valueOf(id));
            if (citaExistente == null) {
                res.status(404);
                return "{\"error\":\"Cita no encontrada\"}";
            }

            try {
                CitaInput input = gson.fromJson(req.body(), CitaInput.class);

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

        // ✅ CORREGIDO: Integer
        delete("/api/citas/:id", (req, res) -> {
            Integer id = Integer.parseInt(req.params(":id"));

            Cita c = citaDAO.buscarPorId(Long.valueOf(id));
            if (c == null) {
                res.status(404);
                return "{\"error\":\"Cita no encontrada\"}";
            }

            try {
                citaDAO.eliminar(id.longValue());
                res.status(204);
                return "";
            } catch (Exception e) {
                res.status(500);
                return "{\"error\": \"Error al eliminar cita: " + e.getMessage() + "\"}";
            }
        });

        // ============ PACIENTES - CORREGIDO CON Integer ============
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

        // ✅ CORREGIDO: Integer
        get("/api/pacientes/:id", (req, res) -> {
            Integer id = Integer.parseInt(req.params(":id"));
            Paciente p = pacienteDAO.buscarPorId(id);
            if (p == null) {
                res.status(404);
                return "{}";
            }
            res.type("application/json");
            return gson.toJson(p);
        });

        // ✅ CORREGIDO: Integer
        put("/api/pacientes/:id", (req, res) -> {
            Integer id = Integer.parseInt(req.params(":id"));
            Paciente p = gson.fromJson(req.body(), Paciente.class);
            p.setId(Long.valueOf(id));
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

        // ============ PROFESIONALES - CORREGIDO CON Integer ============
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

        // ✅ CORREGIDO: Integer
        get("/api/profesionales/:id", (req, res) -> {
            Integer id = Integer.parseInt(req.params(":id"));
            ProfesionalSalud prof = profDAO.buscarPorId(id);
            if (prof == null) {
                res.status(404);
                return "{}";
            }
            res.type("application/json");
            return gson.toJson(prof);
        });

        // ✅ CORREGIDO: Integer
        put("/api/profesionales/:id", (req, res) -> {
            Integer id = Integer.parseInt(req.params(":id"));
            ProfesionalSalud prof = gson.fromJson(req.body(), ProfesionalSalud.class);
            prof.setId(id);
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

        // ============ CUIDADORES ============
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

        // ============ CONSULTORIOS ============
        get("/api/consultorios", (req, res) -> {
            res.type("application/json");
            List<Consultorio> list = consultorioDAO.buscarTodos();
            return gson.toJson(list);
        });

        get("/api/consultorios/:id", (req, res) -> {
            Integer id = Integer.parseInt(req.params(":id"));
            Consultorio c = consultorioDAO.buscarPorId(id);
            if (c == null) {
                res.status(404);
                return "{\"error\":\"Consultorio no encontrado\"}";
            }
            res.type("application/json");
            return gson.toJson(c);
        });

        post("/api/consultorios", (req, res) -> {
            res.type("application/json");
            String rol = (String) req.attribute("rol");

            // Solo ADMIN y MEDICO pueden crear
            if (rol == null || (!rol.equals("ADMIN") && !rol.equals("MEDICO"))) {
                res.status(403);
                return "{\"error\":\"Acceso denegado: requiere rol ADMIN o MEDICO\"}";
            }

            try {
                Consultorio c = gson.fromJson(req.body(), Consultorio.class);

                if (c.getNumeroSala() == null || c.getNumeroSala().isEmpty()) {
                    res.status(400);
                    return "{\"error\":\"El número de sala es obligatorio\"}";
                }

                consultorioDAO.crear(c);
                res.status(201);
                return gson.toJson(c);
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"Error al crear consultorio: " + e.getMessage() + "\"}";
            }
        });

        put("/api/consultorios/:id", (req, res) -> {
            res.type("application/json");
            String rol = (String) req.attribute("rol");

            // Solo ADMIN y MEDICO pueden actualizar
            if (rol == null || (!rol.equals("ADMIN") && !rol.equals("MEDICO"))) {
                res.status(403);
                return "{\"error\":\"Acceso denegado\"}";
            }

            Integer id = Integer.parseInt(req.params(":id"));
            Consultorio c = consultorioDAO.buscarPorId(id);

            if (c == null) {
                res.status(404);
                return "{\"error\":\"Consultorio no encontrado\"}";
            }

            try {
                Consultorio actualizado = gson.fromJson(req.body(), Consultorio.class);

                if (actualizado.getNumeroSala() != null && !actualizado.getNumeroSala().isEmpty()) {
                    c.setNumeroSala(actualizado.getNumeroSala());
                }
                if (actualizado.getUbicacion() != null) {
                    c.setUbicacion(actualizado.getUbicacion());
                }

                consultorioDAO.actualizar(c);
                res.status(200);
                return gson.toJson(c);
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"Error al actualizar: " + e.getMessage() + "\"}";
            }
        });

        delete("/api/consultorios/:id", (req, res) -> {
            String rol = (String) req.attribute("rol");

            // Solo ADMIN puede eliminar
            if (rol == null || !rol.equals("ADMIN")) {
                res.status(403);
                return "{\"error\":\"Solo ADMIN puede eliminar consultorios\"}";
            }

            Integer id = Integer.parseInt(req.params(":id"));
            Consultorio c = consultorioDAO.buscarPorId(id);

            if (c == null) {
                res.status(404);
                return "{\"error\":\"Consultorio no encontrado\"}";
            }

            try {
                consultorioDAO.eliminar(id.longValue());
                res.status(204);
                return "";
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"Error al eliminar: " + e.getMessage() + "\"}";
            }
        });

        // ============ HISTORIAS CLÍNICAS ============
        post("/api/historias", (req, res) -> {
            java.util.Map<String, Object> input = gson.fromJson(req.body(), java.util.Map.class);
            try {
                Integer pacienteId = ((Number) input.get("pacienteId")).intValue();
                Integer profesionalId = ((Number) input.get("profesionalId")).intValue();
                String fechaStr = (String) input.get("fecha");
                String motivo = (String) input.getOrDefault("motivoConsulta", "");
                String diagnostico = (String) input.getOrDefault("diagnostico", "");
                String tratamiento = (String) input.getOrDefault("tratamiento", "");
                String observaciones = (String) input.getOrDefault("observaciones", "");
                String formula = (String) input.getOrDefault("formulaMedica", "");
                boolean requiereIncapacidad = Boolean.parseBoolean(String.valueOf(input.getOrDefault("requiereIncapacidad", "false")));

                Paciente paciente = pacienteDAO.buscarPorId(pacienteId);
                ProfesionalSalud prof = profDAO.buscarPorId(profesionalId);

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

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        });

        after((request, response) -> {
            if (!response.raw().containsHeader("Content-Type") &&
                    !request.requestMethod().equals("OPTIONS")) {
                response.type("application/json");
            }
        });
    }
}