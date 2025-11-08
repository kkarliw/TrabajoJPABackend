package consultorio.modelo.profesionales;

import jakarta.persistence.*;

@Entity
@Table(name = "medico")
public class Medico extends ProfesionalSalud {

    public Medico() {}

    public Medico(String nombre, String apellido, String especialidad, String email, String telefono, String numeroLicencia) {
        super(nombre, apellido, especialidad, email, telefono, numeroLicencia);
    }
}
