package consultorio.modelo;

import consultorio.modelo.profesionales.Medico;
import consultorio.modelo.profesionales.ProfesionalSalud;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fecha;
    private String hora;

    @ManyToOne
    private Paciente paciente;

    @ManyToOne
    private ProfesionalSalud profesional;

    @ManyToOne
    private Consultorio consultorio;

    public Cita() {}

    public Cita(String fecha, String hora, Paciente paciente, ProfesionalSalud profesional, Consultorio consultorio) {
        this.fecha = fecha;
        this.hora = hora;
        this.paciente = paciente;
        this.profesional = profesional;
        this.consultorio = consultorio;
    }

    public Cita(LocalDateTime localDateTime, String chequeoGeneral, Paciente paciente1, Medico medico1, Consultorio consultorio1) {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
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
}
