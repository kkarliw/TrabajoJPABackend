package consultorio.modelo.profesionales;

import jakarta.persistence.*;

@Entity
@Table(name = "especialista")
public class Especialista extends ProfesionalSalud {

    private String campo;

    public Especialista() {}

    public Especialista(String nombre, String apellido, String especialidad, String email, String telefono, String numeroLicencia, String campo) {
        super(nombre, apellido, especialidad, email, telefono, numeroLicencia);
        this.campo = campo;
    }

    public String getCampo() { return campo; }
    public void setCampo(String campo) { this.campo = campo; }
}
