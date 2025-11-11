package consultorio.modelo;

import consultorio.modelo.profesionales.ProfesionalSalud;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "historiaclinica")
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "formulaMedica", columnDefinition = "TEXT")
    private String formulaMedica;

    @Column(name = "motivoConsulta", columnDefinition = "TEXT")
    private String motivoConsulta;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "requiereIncapacidad")
    private Boolean requiereIncapacidad;

    @Column(name = "tratamiento", columnDefinition = "TEXT")
    private String tratamiento;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id")
    private ProfesionalSalud profesional;

    // ✅ Constructor vacío (requerido por JPA)
    public HistoriaClinica() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        this.requiereIncapacidad = false; // Default
    }

    // ✅ Constructor básico (3 parámetros)
    public HistoriaClinica(Paciente paciente, ProfesionalSalud profesional, LocalDate fecha) {
        this();
        this.paciente = paciente;
        this.profesional = profesional;
        this.fecha = fecha;
    }

    // ✅ CORREGIDO: Constructor completo con TODOS los parámetros
    public HistoriaClinica(Paciente paciente, ProfesionalSalud profesional, LocalDate fecha,
                           String motivoConsulta, String diagnostico, String tratamiento,
                           String observaciones, String formulaMedica, boolean requiereIncapacidad) {
        this(); // Llama al constructor vacío primero
        this.paciente = paciente;
        this.profesional = profesional;
        this.fecha = fecha;
        this.motivoConsulta = motivoConsulta;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.observaciones = observaciones;
        this.formulaMedica = formulaMedica;
        this.requiereIncapacidad = requiereIncapacidad;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
        this.updatedAt = LocalDate.now();
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
        this.updatedAt = LocalDate.now();
    }

    public String getFormulaMedica() { return formulaMedica; }
    public void setFormulaMedica(String formulaMedica) {
        this.formulaMedica = formulaMedica;
        this.updatedAt = LocalDate.now();
    }

    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
        this.updatedAt = LocalDate.now();
    }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
        this.updatedAt = LocalDate.now();
    }

    // ✅ CORREGIDO: Getter y Setter de requiereIncapacidad
    public Boolean getRequiereIncapacidad() {
        return requiereIncapacidad != null ? requiereIncapacidad : false;
    }

    public boolean setRequiereIncapacidad(Boolean requiereIncapacidad) {
        this.requiereIncapacidad = this.requiereIncapacidad;
        this.updatedAt = LocalDate.now();
        return false;
    }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
        this.updatedAt = LocalDate.now();
    }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public ProfesionalSalud getProfesional() { return profesional; }
    public void setProfesional(ProfesionalSalud profesional) { this.profesional = profesional; }

    @Override
    public String toString() {
        return "HistoriaClinica{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", paciente=" + (paciente != null ? paciente.getId() : null) +
                ", profesional=" + (profesional != null ? profesional.getId() : null) +
                '}';
    }

}