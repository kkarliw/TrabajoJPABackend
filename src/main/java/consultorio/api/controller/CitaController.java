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

            // Listar todas las citas
            get("", (req, res) -> {
                res.type("application/json");
                List<Cita> list = citaDAO.buscarTodos();
                return gson.toJson(list);
            });

            // Buscar cita por ID
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

            // Buscar citas por paciente
            get("/paciente/:pacienteId", (req, res) -> {
                res.type("application/json");
                Long pacienteId = Long.parseLong(req.params(":pacienteId"));
                List<Cita> citas = citaDAO.buscarPorPaciente(pacienteId);
                return gson.toJson(citas);
            });

            // Buscar citas por médico/profesional
            get("/medico/:medicoId", (req, res) -> {
                res.type("application/json");
                Long medicoId = Long.parseLong(req.params(":medicoId"));
                List<Cita> citas = citaDAO.buscarPorMedico(medicoId);
                return gson.toJson(citas);
            });

            // Crear cita
            post("", (req, res) -> {
                res.type("application/json");

                CitaInput input;
                try {
                    input = gson.fromJson(req.body(), CitaInput.class);
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "JSON inválido: " + e.getMessage()));
                }

                // Validaciones
                if (input.fecha == null || input.fecha.trim().isEmpty()) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "El campo 'fecha' es obligatorio"));
                }

                if (input.pacienteId == null || input.profesionalId == null) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Debe especificar paciente y profesional"));
                }

                // Buscar entidades
                Paciente p = pacienteDAO.buscarPorId(input.pacienteId);
                ProfesionalSalud prof = profDAO.buscarPorId(input.profesionalId);

                if (p == null || prof == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Paciente o profesional no existe"));
                }

                // Parsear fecha
                LocalDate fecha;
                try {
                    fecha = LocalDate.parse(input.fecha);
                } catch (DateTimeParseException e) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Formato de fecha inválido. Use YYYY-MM-DD"));
                }

                // Crear cita
                Cita c = new Cita(p, prof, fecha, input.motivo);

                try {
                    citaDAO.crear(c);
                    res.status(201);
                    return gson.toJson(c);
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Error al crear la cita: " + e.getMessage()));
                }
            });

            // Actualizar cita
            put("/:id", (req, res) -> {
                res.type("application/json");
                Long id = Long.parseLong(req.params(":id"));

                Cita citaExistente = citaDAO.buscarPorId(id);
                if (citaExistente == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Cita no encontrada"));
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
                    return gson.toJson(Map.of("error", "Error al actualizar cita: " + e.getMessage()));
                }
            });

            // Eliminar cita
            delete("/:id", (req, res) -> {
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