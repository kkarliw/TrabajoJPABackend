package consultorio.api.dto;

import java.time.LocalDateTime;

public class CitaInput {
    private Integer pacienteId;
    private Integer profesionalId;
    private LocalDateTime fechaHora;
    private String motivo;

    public Integer getPacienteId() { return pacienteId; }
    public void setPacienteId(Integer pacienteId) { this.pacienteId = pacienteId; }
    public Integer getProfesionalId() { return profesionalId; }
    public void setProfesionalId(Integer profesionalId) { this.profesionalId = profesionalId; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
