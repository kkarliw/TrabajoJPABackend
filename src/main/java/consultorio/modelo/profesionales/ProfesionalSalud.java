package consultorio.modelo.profesionales;

import jakarta.persistence.*;

@Entity
@Table(name = "profesionales")
public class ProfesionalSalud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    private String telefono;

    @Column(nullable = false, unique = true)
    private String numeroLicencia;

    @Column(nullable = false)
    private String especialidad;

    // ✅ SIN @Enumerated - es un String
    @Column(nullable = false)
    private String tipoProfesional;

    @Column(nullable = false)
    private String email;

    // ✅ CONSTRUCTOR VACÍO OBLIGATORIO
    public ProfesionalSalud() {}

    // Constructor con parámetros
    public ProfesionalSalud(String nombre, String apellido, String telefono,
                            String numeroLicencia, String especialidad,
                            String tipoProfesional, String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.numeroLicencia = numeroLicencia;
        this.especialidad = especialidad;
        this.tipoProfesional = tipoProfesional;
        this.email = email;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getNumeroLicencia() { return numeroLicencia; }
    public void setNumeroLicencia(String numeroLicencia) { this.numeroLicencia = numeroLicencia; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getTipoProfesional() { return tipoProfesional; }
    public void setTipoProfesional(String tipoProfesional) { this.tipoProfesional = tipoProfesional; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}