package consultorio.modelo;

import consultorio.modelo.profesionales.ProfesionalSalud;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "citas")
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Paciente paciente;

    @ManyToOne
    private ProfesionalSalud profesional;

    @ManyToOne
    @JoinColumn(name = "consultorio_id")
    private Consultorio consultorio;


    private LocalDate fecha;

    private String motivo;

    public Cita() {}

    public Cita(Paciente paciente, ProfesionalSalud profesional, LocalDate fecha, String motivo) {
        this.paciente = paciente;
        this.profesional = profesional;
        this.fecha = fecha;
        this.motivo = motivo;
    }

    // getters y setters
    public Integer getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public ProfesionalSalud getProfesional() { return profesional; }
    public void setProfesional(ProfesionalSalud profesional) { this.profesional = profesional; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
