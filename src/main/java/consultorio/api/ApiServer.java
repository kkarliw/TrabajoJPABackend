package consultorio.api;

import consultorio.api.controller.*;
import consultorio.api.dto.LoginRequest;
import consultorio.api.dto.RegistroRequest;
import io.jsonwebtoken.Claims;
import org.mindrot.jbcrypt.BCrypt;
import consultorio.modelo.*;
import consultorio.modelo.profesionales.ProfesionalSalud;
import consultorio.persistencia.*;
import consultorio.seguridad.JwtUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import consultorio.LocalDateAdapter;
import static spark.Spark.*;

import java.time.LocalDate;

public class ApiServer {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    public static void main(String[] args) {
        port(4567);
        enableCORS();

        // ============ REGISTRAR CONTROLLERS ============
        System.out.println("🚀 Iniciando servidor en puerto 4567...");

        // Registrar todos los controllers
        AuthController.registerRoutes(gson);
        CitaController.registerRoutes(gson);
        PacienteController.registerRoutes(gson);
        ProfesionalSaludController.registerRoutes(gson);
        CuidadorController.registerRoutes(gson);
        HistoriaClinicaController.registerRoutes(gson);
        NotificacionController.registerRoutes(gson);
        EstadisticasController.registerRoutes(gson);
        PanelSaludController.registerRoutes(gson);

        System.out.println("✅ Controllers registrados correctamente");

        // ✅ FILTRO JWT (Protege todas las rutas excepto auth)
        before("/api/*", (req, res) -> {
            String path = req.pathInfo();

            // Permitir rutas públicas
            if (path.equals("/api/auth/register") ||
                    path.equals("/api/auth/login")) {
                return;
            }

            // Validar token
            String auth = req.headers("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                halt(401, gson.toJson(java.util.Map.of("error", "Token faltante o inválido")));
            }

            try {
                Claims claims = (Claims) JwtUtils.validateToken(auth.substring(7));
                if (claims == null) {
                    halt(401, gson.toJson(java.util.Map.of("error", "Token inválido")));
                }
                req.attribute("rol", claims.get("rol", String.class));
                req.attribute("userId", Long.valueOf(claims.getSubject()));
            } catch (Exception e) {
                halt(401, gson.toJson(java.util.Map.of("error", "Token expirado o inválido")));
            }
        });

        // ============ RUTA DE SALUD ============
        get("/api/health", (req, res) -> {
            res.type("application/json");
            return gson.toJson(java.util.Map.of(
                    "status", "OK",
                    "timestamp", java.time.LocalDateTime.now().toString(),
                    "message", "Backend funcionando correctamente"
            ));
        });

        // ============ CONSULTORIOS (Mantener aquí por ahora) ============
        ConsultorioDAO consultorioDAO = new ConsultorioDAO();

        get("/api/consultorios", (req, res) -> {
            res.type("application/json");
            return gson.toJson(consultorioDAO.buscarTodos());
        });

        get("/api/consultorios/:id", (req, res) -> {
            res.type("application/json");
            Long id = Long.parseLong(req.params(":id"));
            Consultorio c = consultorioDAO.buscarPorId(Math.toIntExact(id));
            if (c == null) {
                res.status(404);
                return gson.toJson(java.util.Map.of("error", "Consultorio no encontrado"));
            }
            return gson.toJson(c);
        });

        post("/api/consultorios", (req, res) -> {
            res.type("application/json");
            String rol = req.attribute("rol");

            if (rol == null || (!rol.equals("ADMIN") && !rol.equals("MEDICO"))) {
                res.status(403);
                return gson.toJson(java.util.Map.of("error", "Acceso denegado: requiere rol ADMIN o MEDICO"));
            }

            try {
                Consultorio c = gson.fromJson(req.body(), Consultorio.class);
                if (c.getNumeroSala() == null || c.getNumeroSala().isEmpty()) {
                    res.status(400);
                    return gson.toJson(java.util.Map.of("error", "El número de sala es obligatorio"));
                }
                consultorioDAO.crear(c);
                res.status(201);
                return gson.toJson(c);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", "Error al crear consultorio: " + e.getMessage()));
            }
        });

        put("/api/consultorios/:id", (req, res) -> {
            res.type("application/json");
            String rol = req.attribute("rol");

            if (rol == null || (!rol.equals("ADMIN") && !rol.equals("MEDICO"))) {
                res.status(403);
                return gson.toJson(java.util.Map.of("error", "Acceso denegado"));
            }

            Integer id = Integer.parseInt(req.params(":id"));
            Consultorio c = consultorioDAO.buscarPorId(id);

            if (c == null) {
                res.status(404);
                return gson.toJson(java.util.Map.of("error", "Consultorio no encontrado"));
            }

            try {
                Consultorio actualizado = gson.fromJson(req.body(), Consultorio.class);
                if (actualizado.getNumeroSala() != null && !actualizado.getNumeroSala().isEmpty()) {
                    c.setNumeroSala(actualizado.getNumeroSala());
                }
                if (actualizado.getUbicacion() != null) {
                    c.setUbicacion(actualizado.getUbicacion());
                }
                consultorioDAO.actualizar(c);
                res.status(200);
                return gson.toJson(c);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", "Error al actualizar: " + e.getMessage()));
            }
        });

        delete("/api/consultorios/:id", (req, res) -> {
            res.type("application/json");
            String rol = req.attribute("rol");

            if (rol == null || !rol.equals("ADMIN")) {
                res.status(403);
                return gson.toJson(java.util.Map.of("error", "Solo ADMIN puede eliminar consultorios"));
            }

            Integer id = Integer.parseInt(req.params(":id"));
            Consultorio c = consultorioDAO.buscarPorId(id);

            if (c == null) {
                res.status(404);
                return gson.toJson(java.util.Map.of("error", "Consultorio no encontrado"));
            }

            try {
                consultorioDAO.eliminar(id.longValue());
                res.status(204);
                return "";
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(java.util.Map.of("error", "Error al eliminar: " + e.getMessage()));
            }
        });

        System.out.println("✅ Servidor iniciado correctamente en http://localhost:4567");
    }

    private static void enableCORS() {
        options("/*", (request, response) -> {
            String headers = request.headers("Access-Control-Request-Headers");
            if (headers != null) {
                response.header("Access-Control-Allow-Headers", headers);
            }
            String methods = request.headers("Access-Control-Request-Method");
            if (methods != null) {
                response.header("Access-Control-Allow-Methods", methods);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        });

        after((request, response) -> {
            if (!response.raw().containsHeader("Content-Type") &&
                    !request.requestMethod().equals("OPTIONS")) {
                response.type("application/json");
            }
        });
    }
}