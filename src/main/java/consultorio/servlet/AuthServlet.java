package consultorio.servlet;


import com.google.gson.Gson;
import consultorio.api.dto.LoginRequest;
import consultorio.api.dto.RegistroRequest;
import consultorio.modelo.Usuario;
import consultorio.persistencia.UsuarioDAO;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/api/auth/*")
public class AuthServlet<RegisterRequest> extends HttpServlet {

    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo(); // /login o /register
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            if ("/login".equals(path)) {
                LoginRequest loginRequest = gson.fromJson(sb.toString(), LoginRequest.class);
                Usuario usuario = usuarioDAO.validarUsuario(loginRequest.getEmail(), loginRequest.getPassword());

                if (usuario != null) {
                    String json = gson.toJson(usuario); // Devuelve datos del usuario
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write(json);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Credenciales inválidas\"}");
                }

            } else if ("/register".equals(path)) {
                RegisterRequest registerRequest = (RegisterRequest) gson.fromJson(sb.toString(),RegistroRequest.class);

                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setNombre(String.valueOf(registerRequest.getClass()));
                nuevoUsuario.setApellido(String.valueOf(registerRequest.getClass()));
                nuevoUsuario.setEmail(String.valueOf(registerRequest.getClass()));
                nuevoUsuario.setPasswordHash(String.valueOf(registerRequest.getClass()));

                usuarioDAO.guardarUsuario(nuevoUsuario);

                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write("{\"message\": \"Usuario registrado con éxito\"}");

            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Ruta no encontrada\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}