package consultorio.persistencia;

import consultorio.modelo.Cita;
import consultorio.modelo.Paciente;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

public class CitaDAO extends GenericDAO<Cita, Number> {

    public CitaDAO() {
        super(Cita.class);
    }

    public List<Cita> buscarCitasPorPaciente(Paciente paciente) {
        EntityManager em = null;
        return em.createQuery("SELECT c FROM Cita c WHERE c.paciente = :paciente ORDER BY c.fechaHora DESC", Cita.class)
                .setParameter("paciente", paciente)
                .getResultList();
    }

    public List<Cita> buscarCitasEntreFechas(LocalDateTime inicio, LocalDateTime fin) {
        return null;
    }

    public void crear(Cita c) {
    }
}