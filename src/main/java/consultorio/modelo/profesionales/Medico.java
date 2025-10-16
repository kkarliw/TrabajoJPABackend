package consultorio.modelo.profesionales;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("MEDICO")
public class Medico extends ProfesionalSalud {
    private String especialidad;

    public Medico() {}

    public Medico(String nombre, String apellido, String especialidad) {
        super(nombre, apellido);
        this.especialidad = especialidad;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
}