package consultorio.api.dto;

public class RegistroRequest {
    private String nombre;
    private String email;
    private String password;
    private String rol;
    private String apellido;

    public String getRol() { return rol; }

    public void setRol(String rol) { this.rol = rol; }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}




