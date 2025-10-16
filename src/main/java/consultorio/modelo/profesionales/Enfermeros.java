package consultorio.modelo.profesionales;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
public class Enfermeros extends ProfesionalSalud {

    private String area;

    public Enfermeros() {}

    public Enfermeros(String nombre, String licencia, String area) {
        super(nombre, licencia);
        this.area = area;
    }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
}