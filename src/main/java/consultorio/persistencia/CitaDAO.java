package consultorio.persistencia;

import consultorio.modelo.Cita;
import consultorio.modelo.Paciente;
import java.time.LocalDateTime;
import java.util.List;

public class CitaDAO extends GenericDAO<Cita, Number> {

    public CitaDAO() {
        super(Cita.class);
    }

    public List<Cita> buscarCitasPorPaciente(Paciente paciente) {
        return em.createQuery("SELECT c FROM Cita c WHERE c.paciente = :paciente ORDER BY c.fechaHora DESC", Cita.class)
                .setParameter("paciente", paciente)
                .getResultList();
    }

    public List<Cita> buscarCitasEntreFechas(LocalDateTime inicio, LocalDateTime fin) {
        return em.createQuery("SELECT c FROM Cita c WHERE c.fechaHora BETWEEN :inicio AND :fin", Cita.class)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList();
    }
}