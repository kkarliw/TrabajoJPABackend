package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.modelo.HistoriaClinica;
import consultorio.modelo.Paciente;
import consultorio.modelo.profesionales.ProfesionalSalud;
import consultorio.persistencia.HistoriaClinicaDAO;
import consultorio.persistencia.PacienteDAO;
import consultorio.persistencia.ProfesionalSaludDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class HistoriaClinicaController {

    private static HistoriaClinicaDAO historiaDAO = new HistoriaClinicaDAO();
    private static PacienteDAO pacienteDAO = new PacienteDAO();
    private static ProfesionalSaludDAO profDAO = new ProfesionalSaludDAO();

    public static class HistoriaInput {
        public Long pacienteId;
        public Long profesionalId;
        public String fecha;
        public String diagnostico;
        public String tratamiento;
        public String observaciones;
        public String motivoConsulta;
        public String formulaMedica;
        public Boolean requiereIncapacidad;
    }

    public static void registerRoutes(Gson gson) {
        path("/api/historias", () -> {

            // Listar todas
            get("", (req, res) -> {
                res.type("application/json");
                List<HistoriaClinica> list = historiaDAO.buscarTodos();
                return gson.toJson(list);
            });

            // Buscar por ID
            get("/:id", (req, res) -> {
                res.type("application/json");
                Long id = Long.parseLong(req.params(":id"));
                HistoriaClinica h = historiaDAO.buscarPorId(id);

                if (h == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Historia clínica no encontrada"));
                }
                return gson.toJson(h);
            });

            // Buscar por paciente
            get("/paciente/:pacienteId", (req, res) -> {
                res.type("application/json");
                Long pacienteId = Long.parseLong(req.params(":pacienteId"));
                HistoriaClinica h = historiaDAO.buscarPorPaciente(pacienteId);

                if (h == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "No se encontró historia para este paciente"));
                }
                return gson.toJson(h);
            });

            // Crear
            post("", (req, res) -> {
                res.type("application/json");

                HistoriaInput input;
                try {
                    input = gson.fromJson(req.body(), HistoriaInput.class);
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "JSON inválido"));
                }

                if (input.pacienteId == null || input.profesionalId == null) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Paciente y profesional son obligatorios"));
                }

                Paciente p = pacienteDAO.buscarPorId(Math.toIntExact(input.pacienteId));
                ProfesionalSalud prof = profDAO.buscarPorId(Math.toIntExact(input.profesionalId));

                if (p == null || prof == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Paciente o profesional no encontrado"));
                }

                LocalDate fecha = input.fecha != null ? LocalDate.parse(input.fecha) : LocalDate.now();

                HistoriaClinica h = new HistoriaClinica(p, prof, fecha);
                h.setDiagnostico(input.diagnostico);
                h.setTratamiento(input.tratamiento);
                h.setObservaciones(input.observaciones);
                h.setMotivoConsulta(input.motivoConsulta);
                h.setFormulaMedica(input.formulaMedica);
                h.setRequiereIncapacidad();

                try {
                    historiaDAO.crear(h);
                    res.status(201);
                    return gson.toJson(h);
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Error al crear: " + e.getMessage()));
                }
            });

            // Actualizar
            put("/:id", (req, res) -> {
                res.type("application/json");
                Long id = Long.parseLong(req.params(":id"));

                HistoriaClinica h = historiaDAO.buscarPorId(id);
                if (h == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Historia no encontrada"));
                }

                try {
                    HistoriaInput input = gson.fromJson(req.body(), HistoriaInput.class);

                    if (input.diagnostico != null) h.setDiagnostico(input.diagnostico);
                    if (input.tratamiento != null) h.setTratamiento(input.tratamiento);
                    if (input.observaciones != null) h.setObservaciones(input.observaciones);
                    if (input.motivoConsulta != null) h.setMotivoConsulta(input.motivoConsulta);
                    if (input.formulaMedica != null) h.setFormulaMedica(input.formulaMedica);
                    if (input.requiereIncapacidad != null) h.setRequiereIncapacidad();

                    historiaDAO.actualizar(h);
                    res.status(200);
                    return gson.toJson(h);
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Error al actualizar"));
                }
            });

            // Eliminar
            delete("/:id", (req, res) -> {
                res.type("application/json");
                Long id = Long.parseLong(req.params(":id"));

                HistoriaClinica h = historiaDAO.buscarPorId(id);
                if (h == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Historia no encontrada"));
                }

                try {
                    historiaDAO.eliminar(id);
                    res.status(204);
                    return "";
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Error al eliminar"));
                }
            });
        });
    }
}
