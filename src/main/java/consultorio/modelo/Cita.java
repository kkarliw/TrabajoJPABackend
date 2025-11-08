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
    @JoinColumn(name = "consultorio_id", nullable = false)
    private Consultorio consultorio;


    private LocalDate fecha;

    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado;

    public Cita() {
        this.estado = EstadoCita.PENDIENTE;
    }

    public Cita(Paciente paciente, ProfesionalSalud profesional, Consultorio consultorio, LocalDate fecha, String motivo) {
        this.paciente = paciente;
        this.profesional = profesional;
        this.consultorio = consultorio;
        this.fecha = fecha;
        this.motivo = motivo;
        this.estado = EstadoCita.PENDIENTE;
    }

    public Cita(Paciente p, ProfesionalSalud prof, LocalDate fecha, String motivo) {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public ProfesionalSalud getProfesional() { return profesional; }
    public void setProfesional(ProfesionalSalud profesional) { this.profesional = profesional; }

    public Consultorio getConsultorio() { return consultorio; }
    public void setConsultorio(Consultorio consultorio) { this.consultorio = consultorio; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    
    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public String getNumeroSala() {
        return this.consultorio != null ? this.consultorio.getNumeroSala() : "N/A";
    }

}