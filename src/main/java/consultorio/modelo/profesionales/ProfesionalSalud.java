package consultorio.modelo.profesionales;

import jakarta.persistence.*;

@Entity
@Table(name = "profesionales")
public class ProfesionalSalud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre;
    private String apellido;
    private String tipoProfesional;
    private String especialidad;
    private String email;
    private String telefono;
    private String numeroLicencia;

    public ProfesionalSalud(Integer id, String nombre, String apellido, String tipoProfesional, String especialidad, String email, String telefono, String numeroLicencia) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipoProfesional = tipoProfesional;
        this.especialidad = especialidad;
        this.email = email;
        this.telefono = telefono;
        this.numeroLicencia = numeroLicencia;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTipoProfesional() {
        return tipoProfesional;
    }

    public void setTipoProfesional(String tipoProfesional) {
        this.tipoProfesional = tipoProfesional;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNumeroLicencia() {
        return numeroLicencia;
    }

    public void setNumeroLicencia(String numeroLicencia) {
        this.numeroLicencia = numeroLicencia;
    }
}

