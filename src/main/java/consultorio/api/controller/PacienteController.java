package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.modelo.*;
import consultorio.persistencia.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class PacienteController {

    private static final PacienteDAO pacienteDAO = new PacienteDAO();
    private static final CitaDAO citaDAO = new CitaDAO();
    private static final HistoriaClinicaDAO historiaDAO = new HistoriaClinicaDAO();
    private static final SignosVitalesDAO signosDAO = new SignosVitalesDAO();
    private static final VacunaDAO vacunaDAO = new VacunaDAO();

    public static void registerRoutes(Gson gson) {

        path("/api/pacientes", () -> {

            // ============ LISTAR TODOS LOS PACIENTES ============
            get("", (req, res) -> {
                res.type("application/json");
                List<Paciente> pacientes = pacienteDAO.listarTodos();

                // ✅ IMPORTANTE: Limpiar colecciones lazy para evitar LazyInitializationException
                pacientes.forEach(p -> {
                    p.setCitas(new ArrayList<>()); // Vaciar citas
                    p.setSignosVitales(new ArrayList<>()); // Vaciar signos vitales
                    p.setVacunas(new ArrayList<>()); // Vaciar vacunas
                    p.setHistoriasClinicas(new ArrayList<>()); // Vaciar historias
                });

                return gson.toJson(pacientes);
            });
            // ============ OBTENER PACIENTE POR ID ============
            get("/:id", (req, res) -> {
                res.type("application/json");
                Long id = Long.parseLong(req.params(":id"));
                Paciente paciente = pacienteDAO.buscarPorId(id);

                if (paciente == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Paciente no encontrado"));
                }

                // ✅ Limpiar colecciones
                paciente.setCitas(new ArrayList<>());
                paciente.setSignosVitales(new ArrayList<>());
                paciente.setVacunas(new ArrayList<>());
                paciente.setHistoriasClinicas(new ArrayList<>());

                return gson.toJson(paciente);
            });

            post("", (req, res) -> {
                res.type("application/json");
                String rol = req.attribute("rol");
                if (rol == null || (!rol.equals("ADMIN") && !rol.equals("RECEPCIONISTA"))) {
                    res.status(403);
                    return gson.toJson(Map.of("error", "Acceso denegado"));
                }

                try {
                    System.out.println("🎯 Creando paciente...");
                    System.out.println("BODY: " + req.body());

                    Paciente paciente = gson.fromJson(req.body(), Paciente.class);

                    // Validar que todos los campos requeridos estén presentes
                    if (paciente.getNombre() == null || paciente.getNombre().isEmpty()) {
                        res.status(400);
                        return gson.toJson(Map.of("error", "El nombre es requerido"));
                    }
                    if (paciente.getApellido() == null || paciente.getApellido().isEmpty()) {
                        res.status(400);
                        return gson.toJson(Map.of("error", "El apellido es requerido"));
                    }
                    if (paciente.getFechaNacimiento() == null) {
                        res.status(400);
                        return gson.toJson(Map.of("error", "La fecha de nacimiento es requerida"));
                    }

                    System.out.println("✅ Paciente válido: " + paciente.getNombre() + " " + paciente.getApellido());

                    pacienteDAO.crear(paciente);
                    res.status(201);
                    return gson.toJson(paciente);

                } catch (Exception e) {
                    System.out.println("❌ ERROR AL CREAR PACIENTE:");
                    System.out.println("MENSAJE: " + e.getMessage());
                    e.printStackTrace();

                    res.status(400);
                    return gson.toJson(Map.of("error", "Error: " + e.getMessage()));
                }
            });

            // ============ ACTUALIZAR PACIENTE ============
            put("/:id", (req, res) -> {
                res.type("application/json");
                String rol = req.attribute("rol");
                if (rol == null || (!rol.equals("ADMIN") && !rol.equals("RECEPCIONISTA"))) {
                    res.status(403);
                    return gson.toJson(Map.of("error", "Acceso denegado"));
                }

                Long id;
                try {
                    id = Long.parseLong(req.params(":id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "ID inválido"));
                }

                Paciente pacienteExistente = pacienteDAO.buscarPorId(id);
                if (pacienteExistente == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Paciente no encontrado"));
                }

                try {
                    Paciente actualizado = gson.fromJson(req.body(), Paciente.class);
                    // Actualizar solo campos básicos
                    pacienteExistente.setNombre(actualizado.getNombre());
                    pacienteExistente.setApellido(actualizado.getApellido());
                    pacienteExistente.setEmail(actualizado.getEmail());
                    pacienteExistente.setTelefono(actualizado.getTelefono());
                    pacienteExistente.setDireccion(actualizado.getDireccion());
                    pacienteExistente.setNumeroDocumento(actualizado.getNumeroDocumento());
                    pacienteExistente.setFechaNacimiento(actualizado.getFechaNacimiento());
                    pacienteExistente.setGenero(actualizado.getGenero());

                    pacienteDAO.actualizar(pacienteExistente);
                    res.status(200);
                    return gson.toJson(pacienteExistente);
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Error al actualizar paciente: " + e.getMessage()));
                }
            });

            // ============ ELIMINAR PACIENTE ============
            delete("/:id", (req, res) -> {
                res.type("application/json");
                String rol = req.attribute("rol");
                if (rol == null || !rol.equals("ADMIN")) {
                    res.status(403);
                    return gson.toJson(Map.of("error", "Solo ADMIN puede eliminar pacientes"));
                }

                Long id;
                try {
                    id = Long.parseLong(req.params(":id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    return gson.toJson(Map.of("error", "ID inválido"));
                }

                Paciente paciente = pacienteDAO.buscarPorId(id);
                if (paciente == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Paciente no encontrado"));
                }

                try {
                    pacienteDAO.eliminar(id);
                    res.status(204);
                    return "";
                } catch (Exception e) {
                    res.status(500);
                    return gson.toJson(Map.of("error", "Error al eliminar paciente: " + e.getMessage()));
                }
            });
        });
    }
}
