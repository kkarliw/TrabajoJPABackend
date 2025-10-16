package consultorio.modelo.profesionales;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ESPECIALISTA")
public class Especialista extends ProfesionalSalud {

    private String campo;

    public Especialista() {}

    public Especialista(String nombre, String apellido, String telefono, String correo, String area, String campo) {
        super(nombre, apellido, telefono, correo, area);
        this.campo = campo;
    }

    public String getCampo() { return campo; }
    public void setCampo(String campo) { this.campo = campo; }
}
