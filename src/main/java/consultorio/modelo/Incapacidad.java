package consultorio.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "incapacidades")
public class Incapacidad extends BaseEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "profesional_id")
    private consultorio.modelo.profesionales.ProfesionalSalud profesional;

    private LocalDate fechaInicio;
    private int diasReposo;

    @Column(length = 1000)
    private String motivo;

    public Incapacidad() {}

    public Incapacidad(Paciente paciente,
                       consultorio.modelo.profesionales.ProfesionalSalud profesional,
                       LocalDate fechaInicio,
                       int diasReposo,
                       String motivo) {
        this.paciente = paciente;
        this.profesional = profesional;
        this.fechaInicio = fechaInicio;
        this.diasReposo = diasReposo;
        this.motivo = motivo;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public consultorio.modelo.profesionales.ProfesionalSalud getProfesional() { return profesional; }
    public void setProfesional(consultorio.modelo.profesionales.ProfesionalSalud profesional) { this.profesional = profesional; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public int getDiasReposo() { return diasReposo; }
    public void setDiasReposo(int diasReposo) { this.diasReposo = diasReposo; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
