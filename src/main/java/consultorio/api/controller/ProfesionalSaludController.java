package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.modelo.profesionales.ProfesionalSalud;
import consultorio.persistencia.ProfesionalSaludDAO;

import java.util.Map;

import static spark.Spark.*;

public class ProfesionalSaludController {
    private static ProfesionalSaludDAO profDAO = new ProfesionalSaludDAO();

    public static void registerRoutes(Gson gson) {
        path("/api/profesionales", () -> {
            get("", (req, res) -> {
                res.type("application/json");
                return gson.toJson(profDAO.buscarTodos());
            });
            get("/mi-perfil", (req, res) -> {
                res.type("application/json");
                Long userId = req.attribute("userId");

                if (userId == null) {
                    res.status(401);
                    return gson.toJson(Map.of("error", "No autenticado"));
                }

                ProfesionalSalud prof = profDAO.buscarPorId(Math.toIntExact(userId));
                if (prof == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Perfil profesional no encontrado"));
                }

                return gson.toJson(prof);
            });

            get("/:id", (req, res) -> {
                Long id = Long.parseLong(req.params(":id"));
                ProfesionalSalud p = profDAO.buscarPorId(Math.toIntExact(id));
                if (p == null) {
                    res.status(404);
                    return "{}";
                }
                res.type("application/json");
                return gson.toJson(p);
            });

            post("", (req, res) -> {
                ProfesionalSalud p = gson.fromJson(req.body(), ProfesionalSalud.class);
                profDAO.crear(p);
                res.status(201);
                res.type("application/json");
                return gson.toJson(p);
            });

            put("/:id", (req, res) -> {
                Long id = Long.parseLong(req.params(":id"));
                ProfesionalSalud p = profDAO.buscarPorId(Math.toIntExact(id));
                if (p == null) {
                    res.status(404);
                    return gson.toJson("{\"error\":\"Profesional no encontrado\"}");
                }
                ProfesionalSalud actualizado = gson.fromJson(req.body(), ProfesionalSalud.class);
                p.setNombre(actualizado.getNombre());
                p.setApellido(actualizado.getApellido());
                p.setEspecialidad(actualizado.getEspecialidad());
                p.setEmail(actualizado.getEmail());
                p.setTelefono(actualizado.getTelefono());
                p.setNumeroLicencia(actualizado.getNumeroLicencia());
                profDAO.actualizar(p);
                res.type("application/json");
                return gson.toJson(p);
            });

            delete("/:id", (req, res) -> {
                Long id = Long.parseLong(req.params(":id"));
                profDAO.eliminar(id);
                res.status(204);
                return "";
            });
        });
    }
}
