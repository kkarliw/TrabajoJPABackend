package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.services.EstadisticasService;
import static spark.Spark.*;

public class EstadisticasController {
    private static EstadisticasService estadisticasService = new EstadisticasService();

    public static void registerRoutes(Gson gson) {
        path("/api/estadisticas", () -> {
            get("", (req, res) -> {
                res.type("application/json");
                return gson.toJson(estadisticasService.obtenerEstadisticas());
            });

            get("/medico/:medicoId", (req, res) -> {
                Long medicoId = Long.parseLong(req.params(":medicoId"));
                res.type("application/json");
                return gson.toJson(estadisticasService.obtenerEstadisticasPorMedico(medicoId));
            });
        });
    }
}