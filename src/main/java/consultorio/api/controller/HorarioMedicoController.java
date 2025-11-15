package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.modelo.HorarioMedico;
import consultorio.persistencia.HorarioMedicoDAO;
import java.util.Map;
import static spark.Spark.*;

public class HorarioMedicoController {
    private static final HorarioMedicoDAO horarioDAO = new HorarioMedicoDAO();

    public static void registerRoutes(Gson gson) {
        path("/api/horarios-medico", () -> {

            // ✅ GET /api/horarios-medico/:profesionalId
            get("/:profesionalId", (req, res) -> {
                res.type("application/json");
                Long profesionalId = Long.parseLong(req.params(":profesionalId"));

                HorarioMedico horario = horarioDAO.buscarPorProfesionalId(profesionalId);

                if (horario == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Horario no configurado para este médico"));
                }

                System.out.println("✅ Horario encontrado para médico: " + profesionalId);
                return gson.toJson(horario);
            });

            // ✅ PUT /api/horarios-medico/:profesionalId (actualizar horario)
            put("/:profesionalId", (req, res) -> {
                res.type("application/json");
                String rol = req.attribute("rol");

                // Solo ADMIN puede modificar horarios
                if (rol == null || !rol.equals("ADMIN")) {
                    res.status(403);
                    return gson.toJson(Map.of("error", "Solo ADMIN puede modificar horarios"));
                }

                Long profesionalId = Long.parseLong(req.params(":profesionalId"));

                try {
                    HorarioMedico horario = gson.fromJson(req.body(), HorarioMedico.class);
                    HorarioMedico existente = horarioDAO.buscarPorProfesionalId(profesionalId);

                    if (existente == null) {
                        res.status(404);
                        return gson.toJson(Map.of("error", "Horario no encontrado"));
                    }

                    // Actualizar campos
                    existente.setHoraInicio(horario.getHoraInicio());
                    existente.setHoraFin(horario.getHoraFin());
                    existente.setAlmuerzoInicio(horario.getAlmuerzoInicio());
                    existente.setAlmuerzoFin(horario.getAlmuerzoFin());
                    existente.setDuracionCita(horario.getDuracionCita());
                    existente.setDiasLaborales(horario.getDiasLaborales());
                    existente.setActivo(horario.getActivo());

                    horarioDAO.actualizar(existente);

                    System.out.println("✅ Horario actualizado para médico: " + profesionalId);
                    res.status(200);
                    return gson.toJson(existente);

                } catch (Exception e) {
                    System.out.println("❌ Error al actualizar horario: " + e.getMessage());
                    res.status(500);
                    return gson.toJson(Map.of("error", "Error al actualizar horario: " + e.getMessage()));
                }
            });
        });
    }
}