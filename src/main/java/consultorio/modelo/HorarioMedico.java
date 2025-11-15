package consultorio.modelo;

import consultorio.modelo.profesionales.ProfesionalSalud;
import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "horarios_medico")
public class HorarioMedico extends BaseEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "profesional_id", nullable = false, unique = true)
    private ProfesionalSalud profesional;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private LocalTime almuerzoInicio;

    @Column(nullable = false)
    private LocalTime almuerzoFin;

    @Column(nullable = false)
    private Integer duracionCita; // en minutos (ej: 20, 30, 60)

    @Column(nullable = false)
    private String diasLaborales; // LUNES,MARTES,MIERCOLES,JUEVES,VIERNES

    @Column(nullable = false)
    private Boolean activo = true;

    public HorarioMedico() {}

    public HorarioMedico(ProfesionalSalud profesional, LocalTime horaInicio, LocalTime horaFin,
                         LocalTime almuerzoInicio, LocalTime almuerzoFin, Integer duracionCita,
                         String diasLaborales) {
        this.profesional = profesional;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.almuerzoInicio = almuerzoInicio;
        this.almuerzoFin = almuerzoFin;
        this.duracionCita = duracionCita;
        this.diasLaborales = diasLaborales;
        this.activo = true;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProfesionalSalud getProfesional() { return profesional; }
    public void setProfesional(ProfesionalSalud profesional) { this.profesional = profesional; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public LocalTime getAlmuerzoInicio() { return almuerzoInicio; }
    public void setAlmuerzoInicio(LocalTime almuerzoInicio) { this.almuerzoInicio = almuerzoInicio; }

    public LocalTime getAlmuerzoFin() { return almuerzoFin; }
    public void setAlmuerzoFin(LocalTime almuerzoFin) { this.almuerzoFin = almuerzoFin; }

    public Integer getDuracionCita() { return duracionCita; }
    public void setDuracionCita(Integer duracionCita) { this.duracionCita = duracionCita; }

    public String getDiasLaborales() { return diasLaborales; }
    public void setDiasLaborales(String diasLaborales) { this.diasLaborales = diasLaborales; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}