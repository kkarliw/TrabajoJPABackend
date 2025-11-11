package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.modelo.Cita;
import consultorio.modelo.Paciente;
import consultorio.modelo.profesionales.ProfesionalSalud;
import consultorio.persistencia.CitaDAO;
import consultorio.persistencia.PacienteDAO;
import consultorio.persistencia.ProfesionalSaludDAO;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class CitaController {
    private static CitaDAO citaDAO = new CitaDAO();
    private static PacienteDAO pacienteDAO = new PacienteDAO();
    private static ProfesionalSaludDAO profDAO = new ProfesionalSaludDAO();

    public static class CitaInput {
        public Long pacienteId;
        public Long profesionalId;
        public String fecha;
        public String motivo;
    }

    public static void registerRoutes(Gson gson) {
        path("/api/citas", () -> {

            get("", (req, res) -> {
                res.type("application/json");
                List<Cita> list = citaDAO.buscarTodos();
                return gson.toJson(list);
            });

            get("/:id", (req, res) -> {
                res.type("application/json");
                Long id = Long.parseLong(req.params(":id"));
                Cita c = citaDAO.buscarPorId(id);

                if (c == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Cita no encontrada"));
                }
                return gson.toJson(c);
            });

            get("/paciente/:pacienteId", (req, res) -> {
                res.type("application/json");
                Long pacienteId = Long.parseLong(req.params(":pacienteId"));
                List<Cita> citas = citaDAO.buscarPorPaciente(pacienteId);
                return gson.toJson(citas);
            });

            get("/medico/:medicoId", (req, res) -> {
                res.type("application/json");
                Long medicoId = Long.parseLong(req.params(":medicoId"));
                List<Cita> citas = citaDAO.buscarPorMedico(medicoId);
                return gson.toJson(citas);
            });

            // ============ CREAR CITA CON DEBUG ============
            post("", (req, res) -> {
                res.type("application/json");

                // 🎯 DEBUG
                System.out.println("\n🎯 === CREANDO CITA ===");
                System.out.println("BODY: " + req.body());
                System.out.println("ROL: " + req.attribute("rol"));

                String rol = req.attribute("rol");

                if (rol == null || (!rol.equals("ADMIN") && !rol.equals("MEDICO") && !rol.equals("RECEPCIONISTA"))) {
                    res.status(403);
                    return gson.toJson(Map.of("error", "Acceso denegado: requiere rol ADMIN, MEDICO o RECEPCIONISTA"));
                }

                CitaInput input;
                try {
                    input = gson.fromJson(req.body(), CitaInput.class);
                    System.out.println("✅ Input parseado:");
                    System.out.println("   - pacienteId: " + input.pacienteId);
                    System.out.println("   - profesionalId: " + input.profesionalId);
                    System.out.println("   - fecha: " + input.fecha);
                    System.out.println("   - motivo: " + input.motivo);
                } catch (Exception e) {
                    System.out.println("❌ Error al parsear JSON: " + e.getMessage());
                    res.status(400);
                    return gson.toJson(Map.of("error", "JSON inválido: " + e.getMessage()));
                }

                // Validaciones
                if (input.fecha == null || input.fecha.trim().isEmpty()) {
                    System.out.println("❌ Fecha vacía");
                    res.status(400);
                    return gson.toJson(Map.of("error", "El campo 'fecha' es obligatorio"));
                }

                if (input.pacienteId == null || input.profesionalId == null) {
                    System.out.println("❌ pacienteId o profesionalId nulo");
                    res.status(400);
                    return gson.toJson(Map.of("error", "Debe especificar paciente y profesional"));
                }

                // Buscar entidades
                Paciente p = pacienteDAO.buscarPorId(input.pacienteId);
                ProfesionalSalud prof = profDAO.buscarPorId(Math.toIntExact(input.profesionalId));

                if (p == null) {
                    System.out.println("❌ Paciente no encontrado: " + input.pacienteId);
                    res.status(404);
                    return gson.toJson(Map.of("error", "Paciente no existe"));
                }

                if (prof == null) {
                    System.out.println("❌ Profesional no encontrado: " + input.profesionalId);
                    res.status(404);
                    return gson.toJson(Map.of("error", "Profesional no existe"));
                }

                // Parsear fecha
                LocalDate fecha;
                try {
                    fecha = LocalDate.parse(input.fecha);
                    System.out.println("✅ Fecha parseada: " + fecha);
                } catch (DateTimeParseException e) {
                    System.out.println("❌ Error al parsear fecha '" + input.fecha + "': " + e.getMessage());
                    res.status(400);
                    return gson.toJson(Map.of("error", "Formato de fecha inválido. Use YYYY-MM-DD. Recibido: " + input.fecha));
                }

                // Crear cita
                Cita c = new Cita(p, prof, fecha, input.motivo);

                try {
                    citaDAO.crear(c);
                    System.out.println("✅ Cita creada exitosamente: " + c);
                    res.status(201);
                    return gson.toJson(c);
                } catch (Exception e) {
                    System.out.println("❌ Error al guardar cita: " + e.getMessage());
                    e.printStackTrace();
                    res.status(500);
                    return gson.toJson(Map.of("error", "Error al crear la cita: " + e.getMessage()));
                }
            });

            put("/:id", (req, res) -> {
                res.type("application/json");
                String rol = req.attribute("rol");

                if (rol == null || (!rol.equals("ADMIN") && !rol.equals("MEDICO") && !rol.equals("RECEPCIONISTA"))) {
                    res.status(403);
                    return gson.toJson(Map.of("error", "Acceso denegado"));
                }

                Long id = Long.parseLong(req.params(":id"));

                Cita citaExistente = citaDAO.buscarPorId(id);
                if (citaExistente == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Cita no encontrada"));
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
                    return gson.toJson(Map.of("error", "Error al actualizar cita: " + e.getMessage()));
                }
            });

            delete("/:id", (req, res) -> {
                res.type("application/json");
                String rol = req.attribute("rol");

                if (rol == null || (!rol.equals("ADMIN") && !rol.equals("RECEPCIONISTA"))) {
                    res.status(403);
                    return gson.toJson(Map.of("error", "Solo ADMIN y RECEPCIONISTA pueden eliminar citas"));
                }

                Long id = Long.parseLong(req.params(":id"));

                Cita c = citaDAO.buscarPorId(id);
                if (c == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Cita no encontrada"));
                }

                try {
                    citaDAO.eliminar(id);
                    res.status(204);
                    return "";
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Error al eliminar cita: " + e.getMessage()));
                }
            });
        });
    }
}