package consultorio.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vacunas")
public class Vacuna extends BaseEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "aplicada_por", nullable = false)
    private Usuario aplicadaPor; // Médico o enfermero

    @Column(nullable = false)
    private String nombreVacuna;

    @Column(nullable = false)
    private LocalDate fechaAplicacion;

    private LocalDate proximaDosis; // Null si es dosis única

    private String lote;
    private String fabricante;

    @Column(length = 500)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    private EstadoVacuna estado;

    public enum EstadoVacuna {
        APLICADA,
        PENDIENTE,
        VENCIDA
    }

    public Vacuna() {
        this.estado = EstadoVacuna.APLICADA;
    }

    public Vacuna(Paciente paciente, Usuario aplicadaPor, String nombreVacuna,
                  LocalDate fechaAplicacion, LocalDate proximaDosis,
                  String lote, String fabricante, String observaciones) {
        this.paciente = paciente;
        this.aplicadaPor = aplicadaPor;
        this.nombreVacuna = nombreVacuna;
        this.fechaAplicacion = fechaAplicacion;
        this.proximaDosis = proximaDosis;
        this.lote = lote;
        this.fabricante = fabricante;
        this.observaciones = observaciones;
        this.estado = EstadoVacuna.APLICADA;
    }

    // Getters y Setters
    public Long getId() { return id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Usuario getAplicadaPor() { return aplicadaPor; }
    public void setAplicadaPor(Usuario aplicadaPor) { this.aplicadaPor = aplicadaPor; }

    public String getNombreVacuna() { return nombreVacuna; }
    public void setNombreVacuna(String nombreVacuna) { this.nombreVacuna = nombreVacuna; }

    public LocalDate getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDate fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }

    public LocalDate getProximaDosis() { return proximaDosis; }
    public void setProximaDosis(LocalDate proximaDosis) { this.proximaDosis = proximaDosis; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public EstadoVacuna getEstado() { return estado; }
    public void setEstado(EstadoVacuna estado) { this.estado = estado; }
}