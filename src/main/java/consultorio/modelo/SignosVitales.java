package consultorio.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signos_vitales")
public class SignosVitales extends BaseEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "registrado_por", nullable = false)
    private Usuario registradoPor; // Puede ser cuidador o médico

    private LocalDateTime fechaRegistro;

    // Signos vitales
    private Double temperatura;  // °C
    private Integer presionSistolica;  // mmHg
    private Integer presionDiastolica; // mmHg
    private Integer frecuenciaCardiaca; // latidos/min
    private Integer frecuenciaRespiratoria; // resp/min
    private Double saturacionOxigeno; // %
    private Double peso; // kg
    private Double altura; // cm

    @Column(length = 500)
    private String observaciones;

    public SignosVitales() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public SignosVitales(Paciente paciente, Usuario registradoPor, Double temperatura,
                         Integer presionSistolica, Integer presionDiastolica,
                         Integer frecuenciaCardiaca, Integer frecuenciaRespiratoria,
                         Double saturacionOxigeno, Double peso, Double altura,
                         String observaciones) {
        this.paciente = paciente;
        this.registradoPor = registradoPor;
        this.fechaRegistro = LocalDateTime.now();
        this.temperatura = temperatura;
        this.presionSistolica = presionSistolica;
        this.presionDiastolica = presionDiastolica;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.frecuenciaRespiratoria = frecuenciaRespiratoria;
        this.saturacionOxigeno = saturacionOxigeno;
        this.peso = peso;
        this.altura = altura;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public Long getId() { return id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Usuario getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(Usuario registradoPor) { this.registradoPor = registradoPor; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }

    public Integer getPresionSistolica() { return presionSistolica; }
    public void setPresionSistolica(Integer presionSistolica) { this.presionSistolica = presionSistolica; }

    public Integer getPresionDiastolica() { return presionDiastolica; }
    public void setPresionDiastolica(Integer presionDiastolica) { this.presionDiastolica = presionDiastolica; }

    public Integer getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) { this.frecuenciaCardiaca = frecuenciaCardiaca; }

    public Integer getFrecuenciaRespiratoria() { return frecuenciaRespiratoria; }
    public void setFrecuenciaRespiratoria(Integer frecuenciaRespiratoria) { this.frecuenciaRespiratoria = frecuenciaRespiratoria; }

    public Double getSaturacionOxigeno() { return saturacionOxigeno; }
    public void setSaturacionOxigeno(Double saturacionOxigeno) { this.saturacionOxigeno = saturacionOxigeno; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public Double getAltura() { return altura; }
    public void setAltura(Double altura) { this.altura = altura; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}