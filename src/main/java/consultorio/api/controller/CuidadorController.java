package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.modelo.Cuidador;
import consultorio.persistencia.CuidadorDAO;

import static spark.Spark.*;

public class CuidadorController {
    private static CuidadorDAO cuidadorDAO = new CuidadorDAO();

    public static void registerRoutes(Gson gson) {
        path("/api/cuidadores", () -> {
            get("", (req, res) -> gson.toJson(cuidadorDAO.listarTodos()));

            get("/:id", (req, res) -> {
                Integer id = Integer.valueOf(req.params(":id"));
                Cuidador c = cuidadorDAO.buscarPorId(Long.valueOf(id));
                if (c == null) { res.status(404); return gson.toJson("{\"error\":\"No existe\"}"); }
                return gson.toJson(c);
            });

            post("", (req, res) -> {
                Cuidador c = gson.fromJson(req.body(), Cuidador.class);
                cuidadorDAO.crear(c);
                res.status(201);
                return gson.toJson(c);
            });

            delete("/:id", (req, res) -> {
                Integer id = Integer.valueOf(req.params(":id"));
                cuidadorDAO.eliminar(Long.valueOf(id));
                res.status(204);
                return "";
            });
        });
    }
}
