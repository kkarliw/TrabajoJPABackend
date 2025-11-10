package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.api.dto.SignosVitalesDTO;
import consultorio.api.dto.VacunaDTO;
import consultorio.modelo.*;
import consultorio.persistencia.*;

import java.time.LocalDate;
import java.util.List;

import static spark.Spark.*;

public class PanelSaludController {
    private static SignosVitalesDAO signosDAO = new SignosVitalesDAO();
    private static VacunaDAO vacunaDAO = new VacunaDAO();
    private static PacienteDAO pacienteDAO = new PacienteDAO();
    private static UsuarioDAO usuarioDAO = new UsuarioDAO();

    public static void registerRoutes(Gson gson) {

        // ============ SIGNOS VITALES ============
        path("/api/pacientes/:pacienteId/signos-vitales", () -> {

            // Listar todos los signos vitales de un paciente
            get("", (req, res) -> {
                res.type("application/json");
                Integer pacienteId = Integer.parseInt(req.params(":pacienteId")); // ✅ Integer
                List<SignosVitales> signos = signosDAO.buscarPorPaciente(pacienteId.longValue());
                return gson.toJson(signos);
            });

            // Obtener último registro de signos vitales
            get("/ultimo", (req, res) -> {
                res.type("application/json");
                Integer pacienteId = Integer.parseInt(req.params(":pacienteId"));
                SignosVitales ultimo = signosDAO.obtenerUltimo(pacienteId.longValue());

                if (ultimo == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "No hay registros de signos vitales"));
                }
                return gson.toJson(ultimo);
            });

            // Registrar nuevos signos vitales
            post("", (req, res) -> {
                res.type("application/json");
                Integer pacienteId = Integer.parseInt(req.params(":pacienteId"));

                SignosVitalesDTO dto;
                try {
                    dto = gson.fromJson(req.body(), SignosVitalesDTO.class);
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(java.util.Map.of("error", "JSON inválido: " + e.getMessage()));
                }

                // Buscar entidades
                Paciente paciente = pacienteDAO.buscarPorId((long) pacienteId.longValue());
                if (paciente == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Paciente no encontrado"));
                }

                Usuario registrador = usuarioDAO.buscarPorId(dto.getRegistradoPorId());
                if (registrador == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Usuario registrador no encontrado"));
                }

                // Crear signos vitales
                SignosVitales sv = new SignosVitales(
                        paciente,
                        registrador,
                        dto.getTemperatura(),
                        dto.getPresionSistolica(),
                        dto.getPresionDiastolica(),
                        dto.getFrecuenciaCardiaca(),
                        dto.getFrecuenciaRespiratoria(),
                        dto.getSaturacionOxigeno(),
                        dto.getPeso(),
                        dto.getAltura(),
                        dto.getObservaciones()
                );

                try {
                    signosDAO.crear(sv);
                    res.status(201);
                    return gson.toJson(sv);
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(java.util.Map.of("error", "Error al registrar signos vitales: " + e.getMessage()));
                }
            });
        });

        // ============ VACUNAS ============
        path("/api/pacientes/:pacienteId/vacunas", () -> {

            // Listar todas las vacunas de un paciente
            get("", (req, res) -> {
                res.type("application/json");
                Integer pacienteId = Integer.parseInt(req.params(":pacienteId"));
                List<Vacuna> vacunas = vacunaDAO.buscarPorPaciente(pacienteId.longValue());
                return gson.toJson(vacunas);
            });

            // Listar vacunas pendientes
            get("/pendientes", (req, res) -> {
                res.type("application/json");
                Integer pacienteId = Integer.parseInt(req.params(":pacienteId"));
                List<Vacuna> pendientes = vacunaDAO.buscarPendientes(pacienteId.longValue());
                return gson.toJson(pendientes);
            });

            // Registrar nueva vacuna
            post("", (req, res) -> {
                res.type("application/json");
                Integer pacienteId = Integer.parseInt(req.params(":pacienteId"));

                VacunaDTO dto;
                try {
                    dto = gson.fromJson(req.body(), VacunaDTO.class);
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(java.util.Map.of("error", "JSON inválido: " + e.getMessage()));
                }

                // Buscar entidades
                Paciente paciente = pacienteDAO.buscarPorId((long) pacienteId.longValue());
                if (paciente == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Paciente no encontrado"));
                }

                Usuario aplicador = usuarioDAO.buscarPorId(dto.getAplicadaPorId());
                if (aplicador == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Usuario aplicador no encontrado"));
                }

                // Parsear fechas
                LocalDate fechaAplicacion = LocalDate.parse(dto.getFechaAplicacion());
                LocalDate proximaDosis = (dto.getProximaDosis() != null && !dto.getProximaDosis().isEmpty())
                        ? LocalDate.parse(dto.getProximaDosis())
                        : null;

                // Crear vacuna
                Vacuna v = new Vacuna(
                        paciente,
                        aplicador,
                        dto.getNombreVacuna(),
                        fechaAplicacion,
                        proximaDosis,
                        dto.getLote(),
                        dto.getFabricante(),
                        dto.getObservaciones()
                );

                try {
                    vacunaDAO.crear(v);
                    res.status(201);
                    return gson.toJson(v);
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(java.util.Map.of("error", "Error al registrar vacuna: " + e.getMessage()));
                }
            });

            // Actualizar vacuna
            put("/:vacunaId", (req, res) -> {
                res.type("application/json");
                Long vacunaId = Long.parseLong(req.params(":vacunaId"));

                Vacuna vacuna = vacunaDAO.buscarPorId(vacunaId);
                if (vacuna == null) {
                    res.status(404);
                    return gson.toJson(java.util.Map.of("error", "Vacuna no encontrada"));
                }

                VacunaDTO dto;
                try {
                    dto = gson.fromJson(req.body(), VacunaDTO.class);
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(java.util.Map.of("error", "JSON inválido: " + e.getMessage()));
                }

                // Actualizar campos
                if (dto.getNombreVacuna() != null) vacuna.setNombreVacuna(dto.getNombreVacuna());
                if (dto.getLote() != null) vacuna.setLote(dto.getLote());
                if (dto.getFabricante() != null) vacuna.setFabricante(dto.getFabricante());
                if (dto.getObservaciones() != null) vacuna.setObservaciones(dto.getObservaciones());

                try {
                    vacunaDAO.actualizar(vacuna);
                    res.status(200);
                    return gson.toJson(vacuna);
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(java.util.Map.of("error", "Error al actualizar vacuna: " + e.getMessage()));
                }
            });
        });

        // ============ PANEL DE SALUD (Dashboard) ============
        get("/api/pacientes/:pacienteId/panel-salud", (req, res) -> {
            res.type("application/json");
            Integer pacienteId = Integer.parseInt(req.params(":pacienteId"));

            Paciente paciente = pacienteDAO.buscarPorId((long) pacienteId.longValue());
            if (paciente == null) {
                res.status(404);
                return gson.toJson(java.util.Map.of("error", "Paciente no encontrado"));
            }

            // Obtener últimos signos vitales
            SignosVitales ultimosSignos = signosDAO.obtenerUltimo(pacienteId.longValue());

            // Obtener vacunas pendientes
            List<Vacuna> vacunasPendientes = vacunaDAO.buscarPendientes(pacienteId.longValue());

            // Construir respuesta
            return gson.toJson(java.util.Map.of(
                    "paciente", paciente,
                    "ultimosSignosVitales", ultimosSignos != null ? ultimosSignos : java.util.Map.of(),
                    "vacunasPendientes", vacunasPendientes
            ));
        });
    }
}