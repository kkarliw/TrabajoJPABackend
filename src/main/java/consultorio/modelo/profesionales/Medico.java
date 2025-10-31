package consultorio.modelo.profesionales;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("MEDICO")

public class Medico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private String especialidad;
    private String correo;

    public Medico() {}

    public Medico(String nombre, String especialidad, String correo) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.correo = correo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
