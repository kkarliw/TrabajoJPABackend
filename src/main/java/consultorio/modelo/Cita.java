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
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "profesional_id", nullable = false)
    private ProfesionalSalud profesional;

    @ManyToOne
    @JoinColumn(name = "consultorio_id")
    private Consultorio consultorio;

    @Column(nullable = false)
    private LocalDate fecha;

    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado;

    // ✅ Constructor vacío (requerido por JPA)
    public Cita() {
        this.estado = EstadoCita.PENDIENTE;
    }

    // ✅ Constructor completo con consultorio
    public Cita(Paciente paciente, ProfesionalSalud profesional, Consultorio consultorio, LocalDate fecha, String motivo) {
        this.paciente = paciente;
        this.profesional = profesional;
        this.consultorio = consultorio;
        this.fecha = fecha;
        this.motivo = motivo;
        this.estado = EstadoCita.PENDIENTE;
    }

    // ✅ Constructor sin consultorio (para cuando no se asigna sala)
    public Cita(Paciente paciente, ProfesionalSalud profesional, LocalDate fecha, String motivo) {
        this.paciente = paciente;
        this.profesional = profesional;
        this.fecha = fecha;
        this.motivo = motivo;
        this.estado = EstadoCita.PENDIENTE;
        // consultorio queda null (puede asignarse después)
    }

    // ✅ Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public ProfesionalSalud getProfesional() {
        return profesional;
    }

    public void setProfesional(ProfesionalSalud profesional) {
        this.profesional = profesional;
    }

    public Consultorio getConsultorio() {
        return consultorio;
    }

    public void setConsultorio(Consultorio consultorio) {
        this.consultorio = consultorio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    // ✅ Método auxiliar para obtener número de sala
    public String getNumeroSala() {
        return this.consultorio != null ? this.consultorio.getNumeroSala() : "N/A";
    }

    // ✅ Override toString para debugging
    @Override
    public String toString() {
        return "Cita{" +
                "id=" + id +
                ", paciente=" + (paciente != null ? paciente.getNombre() : "null") +
                ", profesional=" + (profesional != null ? profesional.getNombre() : "null") +
                ", fecha=" + fecha +
                ", motivo='" + motivo + '\'' +
                ", estado=" + estado +
                '}';
    }
}