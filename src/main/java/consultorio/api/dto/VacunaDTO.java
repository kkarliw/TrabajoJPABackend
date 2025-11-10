package consultorio.api.dto;

public class VacunaDTO {
    private Long pacienteId;
    private Long aplicadaPorId;
    private String nombreVacuna;
    private String fechaAplicacion; // String en formato YYYY-MM-DD
    private String proximaDosis;    // String en formato YYYY-MM-DD (nullable)
    private String lote;
    private String fabricante;
    private String observaciones;

    // Getters y Setters
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Long getAplicadaPorId() { return aplicadaPorId; }
    public void setAplicadaPorId(Long aplicadaPorId) { this.aplicadaPorId = aplicadaPorId; }

    public String getNombreVacuna() { return nombreVacuna; }
    public void setNombreVacuna(String nombreVacuna) { this.nombreVacuna = nombreVacuna; }

    public String getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(String fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }

    public String getProximaDosis() { return proximaDosis; }
    public void setProximaDosis(String proximaDosis) { this.proximaDosis = proximaDosis; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}