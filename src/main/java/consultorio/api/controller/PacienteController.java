package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.modelo.Paciente;
import consultorio.persistencia.PacienteDAO;

import static spark.Spark.*;

public class PacienteController {
    private static final PacienteDAO pacienteDAO = new PacienteDAO();

    public static void registerRoutes(Gson gson) {
        path("/api/pacientes", () -> {
            get("", (req, res) -> {
                res.type("application/json");
                return gson.toJson(pacienteDAO.listarTodos());
            });

            get("/:id", (req, res) -> {
                Integer id = Integer.valueOf(req.params(":id"));
                Paciente p = pacienteDAO.buscarPorId(id.longValue());
                if (p == null) {
                    res.status(404);
                    return gson.toJson("{\"error\":\"Paciente no encontrado\"}");
                }
                return gson.toJson(p);
            });

            post("", (req, res) -> {
                Paciente p = gson.fromJson(req.body(), Paciente.class);
                pacienteDAO.crear(p);
                res.status(201);
                return gson.toJson(p);
            });

            put("/:id", (req, res) -> {
                Integer id = Integer.valueOf(req.params(":id"));
                Paciente existente = pacienteDAO.buscarPorId(id.longValue());
                if (existente == null) { res.status(404); return gson.toJson("{\"error\":\"No existe\"}"); }
                Paciente actualizado = gson.fromJson(req.body(), Paciente.class);
                // actualiza campos
                existente.setNombre(actualizado.getNombre());
                existente.setNombre(actualizado.getEmail());
                existente.setTelefono(actualizado.getTelefono());
                existente.setDireccion(actualizado.getDireccion());
                pacienteDAO.actualizar(existente);
                return gson.toJson(existente);
            });

            delete("/:id", (req, res) -> {
                Integer id = Integer.valueOf(req.params(":id"));
                pacienteDAO.eliminar(id.longValue());
                res.status(204);
                return "";
            });
        });
    }
}
