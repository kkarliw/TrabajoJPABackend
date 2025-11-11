package consultorio.api.controller;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import static spark.Spark.*;
import java.util.Map;

public class ConsultorioController {

    private static final Gson gson = new Gson();

    public static void configurarRutas() {

        // Crear consultorio - SOLO ADMIN y MEDICO
        post("/api/consultorios", (req, res) -> {
            res.type("application/json");
            String rol = req.attribute("rol");

            if (rol == null || (!rol.equals("ADMIN") && !rol.equals("MEDICO"))) {
                res.status(403);
                return gson.toJson(Map.of("error", "Acceso denegado: requiere rol ADMIN o MEDICO"));
            }

            // Lógica para crear consultorio
            res.status(201);
            return gson.toJson(Map.of("message", "Consultorio creado exitosamente"));
        });

        // Actualizar consultorio - SOLO ADMIN y MEDICO
        put("/api/consultorios/:id", (req, res) -> {
            res.type("application/json");
            String rol = req.attribute("rol");

            if (rol == null || (!rol.equals("ADMIN") && !rol.equals("MEDICO"))) {
                res.status(403);
                return gson.toJson(Map.of("error", "Acceso denegado"));
            }

            // Lógica para actualizar consultorio
            String id = req.params(":id");
            res.status(200);
            return gson.toJson(Map.of("message", "Consultorio actualizado correctamente"));
        });

        // Eliminar consultorio - SOLO ADMIN
        delete("/api/consultorios/:id", (req, res) -> {
            res.type("application/json");
            String rol = req.attribute("rol");

            if (rol == null || !rol.equals("ADMIN")) {
                res.status(403);
                return gson.toJson(Map.of("error", "Solo ADMIN puede eliminar consultorios"));
            }

            // Lógica para eliminar consultorio
            String id = req.params(":id");
            res.status(200);
            return gson.toJson(Map.of("message", "Consultorio eliminado exitosamente"));
        });
    }
}
