package consultorio.api.dto;

public class RegistroRequest {
    private String nombre;
    private String email;
    private String passwordHash;
    private String telefono; // Asumo que lo necesitas por el formulario
    private String rol;      // <--- CLAVE PARA QUE FUNCIONE EL REGISTRO CON ROL DINÁMICO
    private String apellido;
    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getRol() { // <--- CLAVE PARA EL CONTROLLER
        return rol;
    }

    // Setters (Si GSON los necesita, aunque generalmente solo con los campos es suficiente)
    // ...
}