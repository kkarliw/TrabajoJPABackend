package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.modelo.*;
import consultorio.persistencia.*;

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
                return gson.toJson(pacientes);
            });

            // ============ OBTENER PACIENTE POR ID ============
            get("/:id", (req, res) -> {
                res.type("application/json");
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

                // Incluir relaciones si se pasa ?detalles=true
                String detalles = req.queryParams("detalles");
                if (detalles != null && detalles.equalsIgnoreCase("true")) {
                    List<Cita> citas = citaDAO.buscarPorPaciente(id);
                    List<HistoriaClinica> historias = historiaDAO.buscarPorPaciente(id);
                    List<SignosVitales> signos = signosDAO.buscarPorPaciente(id);
                    List<Vacuna> vacunas = vacunaDAO.buscarPorPaciente(id);

                    return gson.toJson(Map.of(
                            "paciente", paciente,
                            "citas", citas,
                            "historias", historias,
                            "signosVitales", signos,
                            "vacunas", vacunas
                    ));
                }

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
                    // 👇 AÑADE ESTAS 3 LÍNEAS PARA DEBUG
                    System.out.println("🎯 JSON recibido para crear paciente:");
                    System.out.println("BODY: " + req.body());
                    System.out.println("HEADERS: " + req.headers());

                    Paciente paciente = gson.fromJson(req.body(), Paciente.class);

                    // 👇 AÑADE ESTA LÍNEA TAMBIÉN
                    System.out.println("✅ Paciente parseado correctamente: " + paciente.getNombre());

                    pacienteDAO.crear(paciente);
                    res.status(201);
                    return gson.toJson(paciente);
                } catch (Exception e) {
                    // 👇 AÑADE ESTAS LÍNEAS PARA VER EL ERROR COMPLETO
                    System.out.println("❌ ERROR AL CREAR PACIENTE:");
                    System.out.println("MENSAJE: " + e.getMessage());
                    System.out.println("CAUSA: " + e.getCause());
                    e.printStackTrace(); // 👈 ESTO MUESTRA EL ERROR COMPLETO

                    res.status(400);
                    return gson.toJson(Map.of("error", "Error al crear paciente: " + e.getMessage()));
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
