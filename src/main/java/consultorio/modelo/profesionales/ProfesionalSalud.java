package consultorio.modelo.profesionales;

import javax.persistence.*;
import java.util.List;
import consultorio.modelo.Cita;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_profesional", discriminatorType = DiscriminatorType.STRING)
public abstract class ProfesionalSalud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;

    @OneToMany(mappedBy = "profesional")
    private List<Cita> citas;

    public ProfesionalSalud() {}

    public ProfesionalSalud(String nombre, String apellido, String telefono, String correo, String area) {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }

    public ProfesionalSalud(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
    }
}