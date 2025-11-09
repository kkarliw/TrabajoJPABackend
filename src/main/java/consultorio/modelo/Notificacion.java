package consultorio.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "notificaciones")
public class Notificacion extends BaseEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String mensaje;

    @ManyToOne
    @JoinColumn(name = "remitente_id")
    private Usuario remitente;

    @ManyToOne
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;

    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipo;

    private boolean leida;

    @ManyToOne
    @JoinColumn(name = "cita_id", nullable = true)
    private Cita cita;

    public Notificacion() {}

    public Notificacion(String titulo, String mensaje, Usuario remitente, Usuario destinatario,
                        TipoNotificacion tipo, Cita cita) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.tipo = tipo;
        this.leida = false;
        this.cita = cita;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public Usuario getRemitente() { return remitente; }
    public void setRemitente(Usuario remitente) { this.remitente = remitente; }
    public Usuario getDestinatario() { return destinatario; }
    public void setDestinatario(Usuario destinatario) { this.destinatario = destinatario; }
    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }
}