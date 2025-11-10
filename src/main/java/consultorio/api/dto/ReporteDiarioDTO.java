package consultorio.api.dto;

public class ReporteDiarioDTO {
    private Long pacienteId;
    private Long cuidadorId;
    private String fecha; // String en formato YYYY-MM-DD
    private String estadoGeneral;
    private String alimentacion;
    private String hidratacion;
    private String sueno;
    private String movilidad;
    private String estadoEmocional;
    private String medicamentosAdministrados;
    private String incidentes;
    private String observaciones;

    // Getters y Setters
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Long getCuidadorId() { return cuidadorId; }
    public void setCuidadorId(Long cuidadorId) { this.cuidadorId = cuidadorId; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

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