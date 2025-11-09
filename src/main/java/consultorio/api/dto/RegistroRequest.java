package consultorio.api.dto;

public class RegistroRequest {
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String telefono;
    private String rol;

    // Getters
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }

    // ✅ MÉTODO CORRECTO
    public String getPassword() { return password; }

    public String getTelefono() { return telefono; }
    public String getRol() { return rol; }

    // Setters
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setRol(String rol) { this.rol = rol; }
}