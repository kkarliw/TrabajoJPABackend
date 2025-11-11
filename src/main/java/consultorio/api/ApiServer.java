package consultorio.api;

import consultorio.LocalDateTimeAdapter;
import consultorio.api.controller.*;
import consultorio.modelo.Consultorio;
import consultorio.persistencia.ConsultorioDAO;
import consultorio.seguridad.JwtUtils;
import com.google.gson.Gson;
import consultorio.util.GsonConfig;
import io.jsonwebtoken.Claims;

import static spark.Spark.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApiServer {

    private static final Gson gson = GsonConfig.createGson();

    public static void main(String[] args) {
        port(4567);
        enableCORS();

        System.out.println("🚀 Iniciando servidor en puerto 4567...");

        // ============ REGISTRAR CONTROLLERS ============
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

        // ============ FILTRO JWT ============
        before("/api/*", (req, res) -> {
            String path = req.pathInfo();
            String method = req.requestMethod();

            // ✅ IGNORAR OPTIONS (preflight)
            if (method.equals("OPTIONS")) {
                return;
            }

            // ✅ RUTAS PÚBLICAS (sin token)
            if (path.equals("/api/auth/register") ||
                    path.equals("/api/auth/login") ||
                    path.equals("/api/health")) {
                return;
            }

            // ✅ VALIDAR TOKEN
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

        // ============ CONSULTORIOS ============
        ConsultorioDAO consultorioDAO = new ConsultorioDAO();

        // LISTAR CONSULTORIOS - Todos los roles autenticados
        get("/api/consultorios", (req, res) -> {
            res.type("application/json");
            return gson.toJson(consultorioDAO.buscarTodos());
        });

        // BUSCAR POR ID - Todos los roles autenticados
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

        // CREAR CONSULTORIO - SOLO ADMIN y MEDICO
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

        // ACTUALIZAR CONSULTORIO - SOLO ADMIN y MEDICO
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

        // ELIMINAR CONSULTORIO - SOLO ADMIN
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
        // Manejar preflight OPTIONS requests
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            response.header("Access-Control-Allow-Origin", "*");
            return "OK";
        });

        // Headers para todas las rutas
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
            response.header("Access-Control-Allow-Credentials", "true");

            // No procesar OPTIONS con el filtro JWT
            if (request.requestMethod().equals("OPTIONS")) {
                halt(200);
            }
        });
    }

}