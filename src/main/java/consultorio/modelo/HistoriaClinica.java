package consultorio.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "historias_clinicas")
public class HistoriaClinica extends BaseEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "profesional_id")
    private consultorio.modelo.profesionales.ProfesionalSalud profesional;

    @Column(length = 1000)
    private String motivoConsulta;

    @Column(length = 1000)
    private String diagnostico;

    @Column(length = 2000)
    private String tratamiento;

    @Column(length = 2000)
    private String observaciones;

    @Column(length = 2000)
    private String formulaMedica; // texto libre: receta/fórmula

    private boolean requiereIncapacidad;

    public HistoriaClinica() {}

    public HistoriaClinica(Paciente paciente,
                           consultorio.modelo.profesionales.ProfesionalSalud profesional,
                           LocalDate fecha,
                           String motivoConsulta,
                           String diagnostico,
                           String tratamiento,
                           String observaciones,
                           String formulaMedica,
                           boolean requiereIncapacidad) {
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

    public Long getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public consultorio.modelo.profesionales.ProfesionalSalud getProfesional() { return profesional; }
    public void setProfesional(consultorio.modelo.profesionales.ProfesionalSalud profesional) { this.profesional = profesional; }
    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getFormulaMedica() { return formulaMedica; }
    public void setFormulaMedica(String formulaMedica) { this.formulaMedica = formulaMedica; }
    public boolean isRequiereIncapacidad() { return requiereIncapacidad; }
    public void setRequiereIncapacidad(boolean requiereIncapacidad) { this.requiereIncapacidad = requiereIncapacidad; }
}
