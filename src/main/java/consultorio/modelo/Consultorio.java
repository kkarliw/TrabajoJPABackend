package consultorio.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "consultorios")
public class Consultorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_sala", unique = true, nullable = false)
    private String numeroSala;

    private String ubicacion;

    public Consultorio() {}

    public Consultorio(String numeroSala, String ubicacion) {
        this.numeroSala = numeroSala;
        this.ubicacion = ubicacion;
    }

    public Integer getId() { return id; }
    public String getNumeroSala() { return numeroSala; }
    public void setNumeroSala(String numeroSala) { this.numeroSala = numeroSala; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
}