package consultorio.modelo;

public enum TipoProfesional {
    MEDICO("Médico"),
    ENFERMERO("Enfermero"),
    FISIOTERAPEUTA("Fisioterapeuta"),
    PSICOLOGO("Psicólogo"),
    DENTISTA("Dentista"),
    NUTRICIONISTA("Nutricionista"),
    OTRO("Otro");

    private final String descripcion;

    TipoProfesional(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}