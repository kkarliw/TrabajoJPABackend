package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.api.dto.ReporteDiarioDTO;
import consultorio.modelo.Cuidador;
import consultorio.modelo.Paciente;
import consultorio.modelo.ReporteDiario;
import consultorio.persistencia.CuidadorDAO;
import consultorio.persistencia.PacienteDAO;
import consultorio.persistencia.ReporteDiarioDAO;

import java.time.LocalDate;
import java.util.List;

import static spark.Spark.*;

public class CuidadorController {
    private static CuidadorDAO cuidadorDAO = new CuidadorDAO();
    private static ReporteDiarioDAO reporteDAO = new ReporteDiarioDAO();
    private static PacienteDAO pacienteDAO = new PacienteDAO();

    public static void registerRoutes(Gson gson) {

        path("/api/cuidadores", () -> {

            // Listar todos los cuidadores
            get("", (req, res) -> {
                res.type("application/json");
                return gson.toJson(cuidadorDAO.listarTodos());
            });

            // Buscar cuidador por ID
            get("/:id", (req, res) -> {
                res.type("application/json");
                Integer id = Integer.parseInt(req.params(":id")); // ✅ Integer
                Cuidador c = cuidadorDAO.buscarPorId(id.longValue());

                if (c == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Cuidador no encontrado"));
                }
                return gson.toJson(c);
            });

            // Crear cuidador
            post("", (req, res) -> {
                res.type("application/json");

                Cuidador c;
                try {
                    c = gson.fromJson(req.body(), Cuidador.class);
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(java.util.Map.of("error", "JSON inválido: " + e.getMessage()));
                }

                try {
                    cuidadorDAO.crear(c);
                    res.status(201);
                    return gson.toJson(c);
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(java.util.Map.of("error", "Error al crear cuidador: " + e.getMessage()));
                }
            });

            // Eliminar cuidador
            delete("/:id", (req, res) -> {
                Long id = Long.parseLong(req.params(":id"));

                try {
                    cuidadorDAO.eliminar(id);
                    res.status(204);
                    return "";
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(java.util.Map.of("error", "Error al eliminar cuidador: " + e.getMessage()));
                }
            });

            // ============ REPORTES DIARIOS ============

            // Obtener reportes de un cuidador
            get("/:id/reportes", (req, res) -> {
                res.type("application/json");
                Long cuidadorId = Long.parseLong(req.params(":id"));
                List<ReporteDiario> reportes = reporteDAO.buscarPorCuidador(cuidadorId);
                return gson.toJson(reportes);
            });
        });

        // ============ ENDPOINTS DE REPORTES DIARIOS ============
        path("/api/reportes-diarios", () -> {

            // Crear reporte diario
            post("", (req, res) -> {
                res.type("application/json");

                ReporteDiarioDTO dto;
                try {
                    dto = gson.fromJson(req.body(), ReporteDiarioDTO.class);
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(java.util.Map.of("error", "JSON inválido: " + e.getMessage()));
                }

                // Validaciones
                if (dto.getPacienteId() == null || dto.getCuidadorId() == null) {
                    res.status(400);
                    return gson.toJson(java.util.Map.of("error", "Faltan campos obligatorios (pacienteId, cuidadorId)"));
                }

                // Buscar entidades
                Paciente paciente = pacienteDAO.buscarPorId((long) Math.toIntExact(dto.getPacienteId()));
                if (paciente == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Paciente no encontrado"));
                }

                Cuidador cuidador = cuidadorDAO.buscarPorId(dto.getCuidadorId());
                if (cuidador == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Cuidador no encontrado"));
                }

                // Parsear fecha
                LocalDate fecha = (dto.getFecha() != null && !dto.getFecha().isEmpty())
                        ? LocalDate.parse(dto.getFecha())
                        : LocalDate.now();

                // Verificar si ya existe un reporte para este paciente en esta fecha
                ReporteDiario reporteExistente = reporteDAO.buscarPorPacienteYFecha(dto.getPacienteId(), fecha);
                if (reporteExistente != null) {
                    res.status(409);
                    return gson.toJson(java.util.Map.of(
                            "error", "Ya existe un reporte para este paciente en esta fecha",
                            "reporteId", reporteExistente.getId()
                    ));
                }

                // Crear reporte
                ReporteDiario rd = new ReporteDiario(
                        paciente,
                        cuidador,
                        fecha,
                        dto.getEstadoGeneral(),
                        dto.getAlimentacion(),
                        dto.getHidratacion(),
                        dto.getSueno(),
                        dto.getMovilidad(),
                        dto.getEstadoEmocional(),
                        dto.getMedicamentosAdministrados(),
                        dto.getIncidentes(),
                        dto.getObservaciones()
                );

                try {
                    reporteDAO.crear(rd);
                    res.status(201);
                    return gson.toJson(rd);
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(java.util.Map.of("error", "Error al crear reporte: " + e.getMessage()));
                }
            });

            // Listar reportes por paciente
            get("/paciente/:pacienteId", (req, res) -> {
                res.type("application/json");
                Long pacienteId = Long.parseLong(req.params(":pacienteId"));
                List<ReporteDiario> reportes = reporteDAO.buscarPorPaciente(pacienteId);
                return gson.toJson(reportes);
            });

            // Obtener reporte por ID
            get("/:id", (req, res) -> {
                res.type("application/json");
                Long id = Long.parseLong(req.params(":id"));
                ReporteDiario reporte = reporteDAO.buscarPorId(id);

                if (reporte == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Reporte no encontrado"));
                }
                return gson.toJson(reporte);
            });

            // Actualizar reporte
            put("/:id", (req, res) -> {
                res.type("application/json");
                Long id = Long.parseLong(req.params(":id"));

                ReporteDiario reporte = reporteDAO.buscarPorId(id);
                if (reporte == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Reporte no encontrado"));
                }

                ReporteDiarioDTO dto;
                try {
                    dto = gson.fromJson(req.body(), ReporteDiarioDTO.class);
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(java.util.Map.of("error", "JSON inválido: " + e.getMessage()));
                }

                // Actualizar campos
                if (dto.getEstadoGeneral() != null) reporte.setEstadoGeneral(dto.getEstadoGeneral());
                if (dto.getAlimentacion() != null) reporte.setAlimentacion(dto.getAlimentacion());
                if (dto.getHidratacion() != null) reporte.setHidratacion(dto.getHidratacion());
                if (dto.getSueno() != null) reporte.setSueno(dto.getSueno());
                if (dto.getMovilidad() != null) reporte.setMovilidad(dto.getMovilidad());
                if (dto.getEstadoEmocional() != null) reporte.setEstadoEmocional(dto.getEstadoEmocional());
                if (dto.getMedicamentosAdministrados() != null) reporte.setMedicamentosAdministrados(dto.getMedicamentosAdministrados());
                if (dto.getIncidentes() != null) reporte.setIncidentes(dto.getIncidentes());
                if (dto.getObservaciones() != null) reporte.setObservaciones(dto.getObservaciones());

                try {
                    reporteDAO.actualizar(reporte);
                    res.status(200);
                    return gson.toJson(reporte);
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(java.util.Map.of("error", "Error al actualizar reporte: " + e.getMessage()));
                }
            });
        });
    }
}