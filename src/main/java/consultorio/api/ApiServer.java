package consultorio.api;

import consultorio.LocalDateAdapter;
import consultorio.api.controller.*;
import consultorio.seguridad.JwtUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jsonwebtoken.Claims;

import java.time.LocalDate;

import static spark.Spark.*;

public class ApiServer {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    public static void main(String[] args) {
        port(4567);

        System.out.println("🚀 Iniciando servidor en http://localhost:4567");

        // 1. Configurar CORS
        enableCORS();

        // 2. Filtro JWT (antes de las rutas)
        before("/api/*", (req, res) -> {
            String path = req.pathInfo();

            // Rutas públicas (sin autenticación)
            if (path.equals("/api/auth/register") || path.equals("/api/auth/login")) {
                return;
            }

            // Validar token JWT
            String auth = req.headers("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                halt(401, gson.toJson(java.util.Map.of("error", "Token faltante o inválido")));
            }

            try {
                Claims claims = (Claims) JwtUtils.validateToken(auth.substring(7));
                assert claims != null;
                req.attribute("userId", claims.getSubject());
                req.attribute("rol", claims.get("rol", String.class));
            } catch (Exception e) {
                halt(401, gson.toJson(java.util.Map.of("error", "Token inválido o expirado")));
            }
        });

        // 3. Registrar todos los controllers
        System.out.println("📋 Registrando controllers...");

        AuthController.registerRoutes(gson);
        System.out.println("   ✓ AuthController");

        PacienteController.registerRoutes(gson);
        System.out.println("   ✓ PacienteController");

        ProfesionalSaludController.registerRoutes(gson);
        System.out.println("   ✓ ProfesionalSaludController");

        CitaController.registerRoutes(gson);
        System.out.println("   ✓ CitaController");

        CuidadorController.registerRoutes(gson);
        System.out.println("   ✓ CuidadorController");

        HistoriaClinicaController.registerRoutes(gson);
        System.out.println("   ✓ HistoriaClinicaController");

        NotificacionController.registerRoutes(gson);
        System.out.println("   ✓ NotificacionController");

        EstadisticasController.registerRoutes(gson);
        System.out.println("   ✓ EstadisticasController");

        PanelSaludController.registerRoutes(gson);
        System.out.println("   ✓ PanelSaludController (NUEVO)");

        // 4. Ruta de prueba
        get("/api/health", (req, res) -> {
            res.type("application/json");
            return gson.toJson(java.util.Map.of(
                    "status", "OK",
                    "message", "API funcionando correctamente",
                    "version", "2.0"
            ));
        });

        System.out.println("✅ Servidor listo y escuchando en el puerto 4567");
    }

    /**
     * Configuración de CORS para permitir peticiones desde el frontend
     */
    private static void enableCORS() {
        // 1. Manejar peticiones OPTIONS (preflight)
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

        // 2. Aplicar headers CORS a todas las respuestas
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        });

        // 3. Asegurar Content-Type JSON por defecto
        after((request, response) -> {
            if (!response.raw().containsHeader("Content-Type") &&
                    !request.requestMethod().equals("OPTIONS")) {
                response.type("application/json");
            }
        });
    }
}