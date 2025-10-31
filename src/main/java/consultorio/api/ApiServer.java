package consultorio.api;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import consultorio.modelo.Paciente;
import consultorio.modelo.Cita;
import consultorio.modelo.profesionales.ProfesionalSalud;
import consultorio.persistencia.PacienteDAO;
import consultorio.persistencia.ProfesionalSaludDAO;
import consultorio.persistencia.CitaDAO;

import java.time.LocalDate;
import java.util.List;

public class ApiServer {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        port(4567);
        enableCORS();

        PacienteDAO pacienteDAO = new PacienteDAO();
        ProfesionalSaludDAO profDAO = new ProfesionalSaludDAO();
        CitaDAO citaDAO = new CitaDAO();

        // PACIENTES
        post("/api/pacientes", (req, res) -> {
            Paciente p = gson.fromJson(req.body(), Paciente.class);
            pacienteDAO.crear(p);
            res.status(201);
            res.type("application/json");
            return gson.toJson(p);
        });

        get("/api/pacientes", (req, res) -> {
            List<Paciente> list = pacienteDAO.buscarTodos();
            res.type("application/json");
            return gson.toJson(list);
        });

        get("/api/pacientes/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            Paciente p = pacienteDAO.buscarPorId(id);
            if (p == null) {
                res.status(404);
                return "{}";
            }
            res.type("application/json");
            return gson.toJson(p);
        });

        post("/api/profesionales", (req, res) -> {
            ProfesionalSalud prof = gson.fromJson(req.body(), ProfesionalSalud.class);
            profDAO.crear(prof);
            res.status(201);
            res.type("application/json");
            return gson.toJson(prof);
        });

        get("/api/profesionales", (req, res) -> {
            List<ProfesionalSalud> list = profDAO.buscarTodos();
            res.type("application/json");
            return gson.toJson(list);
        });

        post("/api/citas", (req, res) -> {
            CitaInput input = gson.fromJson(req.body(), CitaInput.class);
            Paciente p = pacienteDAO.buscarPorId(input.pacienteId);
            ProfesionalSalud prof = (ProfesionalSalud) profDAO.buscarPorId(input.profesionalId);
            if (p == null || prof == null) {
                res.status(400);
                return gson.toJson("Paciente o profesional no existe");
            }
            LocalDate fecha = LocalDate.parse(input.fecha);
            Cita c = new Cita(p, prof, fecha, input.motivo);
            citaDAO.crear(c);
            res.status(201);
            res.type("application/json");
            return gson.toJson(c);
        });

        get("/api/citas", (req, res) -> {
            List<Cita> list = citaDAO.buscarTodos();
            res.type("application/json");
            return gson.toJson(list);
        });

        get("/api/citas/:id", (req, res) -> {
            Long id = Long.parseLong(req.params(":id"));
            Cita c = citaDAO.buscarPorId(id);
            if (c == null) {
                res.status(404);
                return "{}";
            }
            res.type("application/json");
            return gson.toJson(c);
        });

    }

    private static void enableCORS() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
            response.type("application/json");
        });
    }
}
