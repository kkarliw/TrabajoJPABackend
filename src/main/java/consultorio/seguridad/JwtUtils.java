package consultorio.seguridad;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtils {

    // Clave secreta segura para firmar el token
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Tiempo de expiración (1 hora)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    // Generar token
    public static String generateToken(Long userId, String rol) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    // Validar token (revisa si es válido y no expiró)
    public static boolean isValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false; // Token inválido o expirado
        }
    }

    // Obtener rol del token
    public static String getRol(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("rol", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Obtener ID de usuario del token
    public static Long getUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    // Validación completa opcional (si quieres una función de apoyo)
    public static Object validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims; // devuelve los datos dentro del token
        } catch (JwtException e) {
            return null;
        }
    }
}
