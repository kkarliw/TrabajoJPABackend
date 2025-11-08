package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.api.dto.CitaInput;
import consultorio.modelo.Cita;
import consultorio.persistencia.CitaDAO;

import static spark.Spark.*;

public class CitaController {
    private static CitaDAO citaDAO = new CitaDAO();

    public static void registerRoutes(Gson gson) {
        path("/api/citas", () -> {
            get("", (req, res) -> gson.toJson(citaDAO.listarTodos()));

            get("/:id", (req, res) -> {
                Integer id = Integer.valueOf(req.params(":id"));
                Cita c = citaDAO.buscarPorId(id.longValue());
                if (c == null) { res.status(404); return gson.toJson("{\"error\":\"Cita no encontrada\"}"); }
                return gson.toJson(c);
            });

            post("", (req, res) -> {
                // el front manda CitaInput JSON (pacienteId, profesionalId, fechaHora, motivo)
                CitaInput input = gson.fromJson(req.body(), CitaInput.class);
                // Aquí debes construir la entidad Cita según tu modelo actual
                Cita c = new Cita();
                // suponer setters: setPacienteId, setProfesionalId o similar según tu modelo
                // ejemplo genérico (ajusta según tu Cita.java)
                // c.setPacienteId(input.getPacienteId());
                // c.setProfesionalId(input.getProfesionalId());
                // c.setFechaHora(input.getFechaHora());
                // c.setMotivo(input.getMotivo());
                citaDAO.crear(c);
                res.status(201);
                return gson.toJson(c);
            });

            put("/:id", (req, res) -> {
                Integer id = Integer.valueOf(req.params(":id"));
                Cita c = citaDAO.buscarPorId(id.longValue());
                if (c == null) { res.status(404); return gson.toJson("{\"error\":\"No existe\"}"); }
                CitaInput input = gson.fromJson(req.body(), CitaInput.class);
                // actualizar campos (según tu Cita.java)
                // c.setFechaHora(input.getFechaHora()); etc.
                citaDAO.actualizar(c);
                return gson.toJson(c);
            });

            delete("/:id", (req, res) -> {
                Integer id = Integer.valueOf(req.params(":id"));
                citaDAO.eliminar(id.longValue());
                res.status(204);
                return "";
            });

            // consultas por paciente (ejemplo)
            get("/paciente/:pacienteId", (req, res) -> {
                Integer pid = Integer.valueOf(req.params(":pacienteId"));
                return gson.toJson(citaDAO.buscarPorPaciente(pid.longValue()));
            });
        });
    }
}
