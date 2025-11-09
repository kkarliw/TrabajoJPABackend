
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
import java.util.Map;

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
        NotificacionDAO notificacionDAO = new NotificacionDAO();

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
            RegistroRequest r = gson.fromJson(req.body(), RegistroRequest.class);

            try {
                r = gson.fromJson(req.body(), RegistroRequest.class);
            } catch (Exception e) {
                res.status(400);
                return "{\"error\":\"JSON inválido: " + e.getMessage() + "\"}";
            }

            if (r.getEmail() == null || r.getPassword() == null || r.getNombre() == null) {
                res.status(400);
                return "{\"error\":\"Faltan campos obligatorios (nombre, email, password)\"}";
            }

            String emailNormalized = r.getEmail().toLowerCase().trim();

            if (usuarioDAO.buscarPorEmail(emailNormalized) != null) {
                res.status(409);
                return "{\"error\":\"El correo ya está registrado\"}";
            }

            String hash = BCrypt.hashpw(r.getPassword(), BCrypt.gensalt());

            String rol = "PACIENTE";
            if (r.getRol() != null && !r.getRol().isEmpty()) {
                String rolUpper = r.getRol().toUpperCase();
                if (rolUpper.matches("ADMIN|PACIENTE|MEDICO|RECEPCIONISTA|CUIDADOR")) {
                    rol = rolUpper;
                }
            }

            if (!rol.matches("ADMIN|PACIENTE|MEDICO|CUIDADOR|RECEPCIONISTA")) {
                rol = "PACIENTE";
            }

            Usuario u = new Usuario(r.getEmail(), r.getNombre(), hash, rol);
            usuarioDAO.crear(u);

            try {
                usuarioDAO.crear(u);
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
                return "{\"error\":\"Faltan campos\"}";
            }

            Usuario u = usuarioDAO.buscarPorEmail(lr.getEmail().toLowerCase().trim());
            if (u == null || !BCrypt.checkpw(lr.getPassword(), u.getPasswordHash())) {
                res.status(401);
                return "{\"error\":\"Credenciales incorrectas\"}";
            }

            String token = JwtUtils.generateToken(u.getId(), u.getRol());
            res.status(200);

            return gson.toJson(Map.of(
                    "id", u.getId(),
                    "nombre", u.getNombre(),
                    "rol", u.getRol()
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

        // ✅ BÚSQUEDA DE PACIENTES - NUEVO
        get("/api/pacientes/buscar", (req, res) -> {
            res.type("application/json");

            String nombre = req.queryParams("nombre");
            String documento = req.queryParams("documento");

            if (nombre != null && !nombre.trim().isEmpty()) {
                try {
                    List<Paciente> resultados = pacienteDAO.buscarPorNombre(nombre);
                    return gson.toJson(resultados);
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(java.util.Map.of("error", "Error en búsqueda: " + e.getMessage()));
                }
            }

            if (documento != null && !documento.trim().isEmpty()) {
                try {
                    Paciente paciente = pacienteDAO.buscarPorDocumento(documento);
                    if (paciente == null) {
                        res.status(404);
                        return gson.toJson(java.util.Map.of("error", "Paciente no encontrado"));
                    }
                    return gson.toJson(java.util.List.of(paciente));
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(java.util.Map.of("error", "Error: " + e.getMessage()));
                }
            }

            res.status(400);
            return gson.toJson(java.util.Map.of("error", "Debe proporcionar 'nombre' o 'documento'"));
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

        // ✅ CITAS DEL DÍA - NUEVO
        get("/api/citas/hoy", (req, res) -> {
            res.type("application/json");
            try {
                LocalDate hoy = LocalDate.now();
                List<Cita> citasHoy = citaDAO.buscarPorFecha(hoy);
                return gson.toJson(citasHoy);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", "Error: " + e.getMessage()));
            }
        });

        // ✅ CITAS PRÓXIMAS - NUEVO
        get("/api/citas/proximas", (req, res) -> {
            res.type("application/json");
            try {
                String diasParam = req.queryParams("dias");
                int dias = (diasParam != null) ? Integer.parseInt(diasParam) : 7;

                LocalDate inicio = LocalDate.now();
                LocalDate fin = inicio.plusDays(dias);

                List<Cita> proximas = citaDAO.buscarEnRango(inicio, fin);
                return gson.toJson(proximas);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", "Error: " + e.getMessage()));
            }
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

            Paciente p = pacienteDAO.buscarPorId(input.pacienteId);
            ProfesionalSalud prof = (ProfesionalSalud) profDAO.buscarPorId(input.profesionalId);

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

        get("/api/citas/paciente/:pacienteId", (req, res) -> {
            res.type("application/json");
            Long pacienteId = Long.parseLong(req.params(":pacienteId"));
            List<Cita> citas = citaDAO.buscarPorPaciente(pacienteId);
            return gson.toJson(citas);
        });

        // ✅ MARCAR LLEGADA DE PACIENTE - NUEVO
        put("/api/citas/:id/marcar-llegada", (req, res) -> {
            res.type("application/json");
            try {
                Long id = Long.parseLong(req.params(":id"));
                Cita cita = citaDAO.buscarPorId(id);

                if (cita == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Cita no encontrada"));
                }

                cita.setEstado(EstadoCita.CONFIRMADA);
                citaDAO.actualizar(cita);

                // Crear notificación al médico
                if (cita.getProfesional() != null) {
                    try {
                        Usuario medico = usuarioDAO.buscarPorId((long) cita.getProfesional().getId());

                        if (medico != null) {
                            String mensaje = String.format(
                                    "El paciente %s ha llegado a la cita",
                                    cita.getPaciente().getNombre()
                            );

                            Notificacion notif = new Notificacion(
                                    "Paciente llegó",
                                    mensaje,
                                    null,
                                    medico,
                                    TipoNotificacion.PACIENTE_LLEGO,
                                    cita
                            );
                            notificacionDAO.crear(notif);
                        }
                    } catch (Exception ex) {
                        System.out.println("No se pudo crear notificación: " + ex.getMessage());
                    }
                }

                return gson.toJson(cita);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", "Error: " + e.getMessage()));
            }
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

        // ✅ NOTIFICACIONES NO LEÍDAS - NUEVO
        get("/api/notificaciones/:usuarioId/no-leidas", (req, res) -> {
            res.type("application/json");
            try {
                Long usuarioId = Long.parseLong(req.params(":usuarioId"));
                List<Notificacion> noLeidas = notificacionDAO.buscarNoLeidas(usuarioId);
                return gson.toJson(noLeidas);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", "Error: " + e.getMessage()));
            }
        });

        // ✅ OBTENER PERFIL DE USUARIO - NUEVO
        get("/api/usuarios/:id", (req, res) -> {
            res.type("application/json");
            try {
                Long id = Long.parseLong(req.params(":id"));
                Usuario usuario = usuarioDAO.buscarPorId(id);

                if (usuario == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Usuario no encontrado"));
                }

                return gson.toJson(java.util.Map.of(
                        "id", usuario.getId(),
                        "nombre", usuario.getNombre(),
                        "apellido", usuario.getApellido(),
                        "email", usuario.getEmail(),
                        "rol", usuario.getRol()
                ));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", "Error: " + e.getMessage()));
            }
        });

        // ✅ ACTUALIZAR PERFIL DE USUARIO - NUEVO
        put("/api/usuarios/:id", (req, res) -> {
            res.type("application/json");
            try {
                Long id = Long.parseLong(req.params(":id"));
                Usuario usuarioExistente = usuarioDAO.buscarPorId(id);

                if (usuarioExistente == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Usuario no encontrado"));
                }

                java.util.Map<String, Object> datosActualizacion = gson.fromJson(req.body(), java.util.Map.class);

                if (datosActualizacion.containsKey("nombre")) {
                    usuarioExistente.setNombre((String) datosActualizacion.get("nombre"));
                }
                if (datosActualizacion.containsKey("apellido")) {
                    usuarioExistente.setApellido((String) datosActualizacion.get("apellido"));
                }

                usuarioDAO.actualizar(usuarioExistente);

                res.status(200);
                return gson.toJson(java.util.Map.of(
                        "id", usuarioExistente.getId(),
                        "nombre", usuarioExistente.getNombre(),
                        "apellido", usuarioExistente.getApellido(),
                        "email", usuarioExistente.getEmail(),
                        "rol", usuarioExistente.getRol()
                ));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", "Error al actualizar: " + e.getMessage()));
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