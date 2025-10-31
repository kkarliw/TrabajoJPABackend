package consultorio.modelo.profesionales;

import jakarta.persistence.*;

@Entity
@Table(name = "profesionales")
@Inheritance(strategy = InheritanceType.JOINED)
public class ProfesionalSalud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String apellido;
    private String especialidad;
    private String correo;

    public ProfesionalSalud() {}

    public ProfesionalSalud(String nombre, String apellido, String especialidad, String correo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.especialidad = especialidad;
        this.correo = correo;
    }

    public ProfesionalSalud(String nombre, String apellido, String telefono, String correo, String area) {
    }

    public ProfesionalSalud(String nombre, String licencia) {
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
