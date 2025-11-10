package consultorio.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reportes_diarios")
public class ReporteDiario extends BaseEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "cuidador_id", nullable = false)
    private Cuidador cuidador;

    @Column(nullable = false)
    private LocalDate fecha;

    // Estado General
    @Column(length = 1000)
    private String estadoGeneral;

    @Column(length = 1000)
    private String alimentacion;

    @Column(length = 1000)
    private String hidratacion;

    @Column(length = 1000)
    private String sueno;

    @Column(length = 1000)
    private String movilidad;

    @Column(length = 1000)
    private String estadoEmocional;

    // Medicamentos
    @Column(length = 2000)
    private String medicamentosAdministrados;

    // Incidentes
    @Column(length = 2000)
    private String incidentes;

    @Column(length = 2000)
    private String observaciones;

    public ReporteDiario() {
        this.fecha = LocalDate.now();
    }

    public ReporteDiario(Paciente paciente, Cuidador cuidador, LocalDate fecha,
                         String estadoGeneral, String alimentacion, String hidratacion,
                         String sueno, String movilidad, String estadoEmocional,
                         String medicamentosAdministrados, String incidentes,
                         String observaciones) {
        this.paciente = paciente;
        this.cuidador = cuidador;
        this.fecha = fecha;
        this.estadoGeneral = estadoGeneral;
        this.alimentacion = alimentacion;
        this.hidratacion = hidratacion;
        this.sueno = sueno;
        this.movilidad = movilidad;
        this.estadoEmocional = estadoEmocional;
        this.medicamentosAdministrados = medicamentosAdministrados;
        this.incidentes = incidentes;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public Long getId() { return id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Cuidador getCuidador() { return cuidador; }
    public void setCuidador(Cuidador cuidador) { this.cuidador = cuidador; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getEstadoGeneral() { return estadoGeneral; }
    public void setEstadoGeneral(String estadoGeneral) { this.estadoGeneral = estadoGeneral; }

    public String getAlimentacion() { return alimentacion; }
    public void setAlimentacion(String alimentacion) { this.alimentacion = alimentacion; }

    public String getHidratacion() { return hidratacion; }
    public void setHidratacion(String hidratacion) { this.hidratacion = hidratacion; }

    public String getSueno() { return sueno; }
    public void setSueno(String sueno) { this.sueno = sueno; }

    public String getMovilidad() { return movilidad; }
    public void setMovilidad(String movilidad) { this.movilidad = movilidad; }

    public String getEstadoEmocional() { return estadoEmocional; }
    public void setEstadoEmocional(String estadoEmocional) { this.estadoEmocional = estadoEmocional; }

    public String getMedicamentosAdministrados() { return medicamentosAdministrados; }
    public void setMedicamentosAdministrados(String medicamentosAdministrados) { this.medicamentosAdministrados = medicamentosAdministrados; }

    public String getIncidentes() { return incidentes; }
    public void setIncidentes(String incidentes) { this.incidentes = incidentes; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}