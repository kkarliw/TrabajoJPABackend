package consultorio.modelo;

import javax.persistence.*;
import java.util.List;

@Entity
public class Consultorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;
    private String piso;

    @OneToMany(mappedBy = "consultorio")
    private List<Cita> citas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getPiso() {
        return piso;
    }

    public void setPiso(String piso) {
        this.piso = piso;
    }

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }

    public Consultorio() {}

    public Consultorio(String numero, String piso) {
        this.numero = numero;
        this.piso = piso;
    }
}