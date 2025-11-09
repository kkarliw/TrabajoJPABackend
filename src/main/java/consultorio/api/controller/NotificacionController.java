package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.modelo.Notificacion;
import consultorio.persistencia.NotificacionDAO;
import static spark.Spark.*;

public class NotificacionController {
    private static NotificacionDAO notificacionDAO = new NotificacionDAO();

    public static void registerRoutes(Gson gson) {
        path("/api/notificaciones", () -> {
            get("/:usuarioId", (req, res) -> {
                Long usuarioId = Long.parseLong(req.params(":usuarioId"));
                res.type("application/json");
                return gson.toJson(notificacionDAO.buscarPorDestinatario(usuarioId));
            });

            put("/:id/leer", (req, res) -> {
                Long id = Long.parseLong(req.params(":id"));
                Notificacion n = notificacionDAO.buscarPorId(id);
                if (n == null) {
                    res.status(404);
                    return gson.toJson("{\"error\":\"Notificación no encontrada\"}");
                }
                n.setLeida(true);
                notificacionDAO.actualizar(n);
                res.type("application/json");
                return gson.toJson(n);
            });

            post("", (req, res) -> {
                Notificacion n = gson.fromJson(req.body(), Notificacion.class);
                notificacionDAO.crear(n);
                res.status(201);
                res.type("application/json");
                return gson.toJson(n);
            });
        });
    }
}