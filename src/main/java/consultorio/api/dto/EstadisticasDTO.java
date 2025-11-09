package consultorio.api.dto;

public class EstadisticasDTO {

    public long totalPacientes;
    public long citasHoy;
    public long citasCompletadas;
    public long citasPendientes;
    public long pacientesNuevos;
    public double porcentajeAsistencia;

    public EstadisticasDTO(long totalPacientes, long citasHoy, long citasCompletadas,
                           long citasPendientes, long pacientesNuevos, double porcentajeAsistencia) {
        this.totalPacientes = totalPacientes;
        this.citasHoy = citasHoy;
        this.citasCompletadas = citasCompletadas;
        this.citasPendientes = citasPendientes;
        this.pacientesNuevos = pacientesNuevos;
        this.porcentajeAsistencia = porcentajeAsistencia;
    }
}
