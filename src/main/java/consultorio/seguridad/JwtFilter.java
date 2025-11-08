package consultorio.seguridad;

import static spark.Spark.halt;

import io.jsonwebtoken.Claims;
import spark.Request;
import spark.Response;
import spark.Filter;

public class JwtFilter implements Filter {

    @Override
    public void handle(Request request, Response response) throws Exception {
        String path = request.pathInfo();
        if (path.startsWith("/api/auth")) return;

        String auth = request.headers("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            halt(401, "{\"error\":\"Token faltante o inválido\"}");
            return;
        }

        try {
            Claims claims = (Claims) JwtUtils.validateToken(auth.substring(7));
            String rol = claims.get("rol", String.class); // O claims.get("rol") si es Object
            request.attribute("rol", rol);

            // Control por rol
            if (path.startsWith("/api/cuidadores") && !rol.equals("ADMIN")) {
                halt(403, "{\"error\":\"Acceso denegado: se requiere rol ADMIN\"}");
            }
        } catch (Exception e) {
            halt(401, "{\"error\":\"Token inválido o expirado\"}");
        }

    }
}