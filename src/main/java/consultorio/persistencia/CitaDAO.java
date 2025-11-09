package consultorio.persistencia;

import consultorio.modelo.Cita;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.time.LocalDate;
import java.util.List;

public class CitaDAO {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public void crear(Cita c) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(c);
        em.getTransaction().commit();
        em.close();
    }

    public List<Cita> buscarTodos() {
        EntityManager em = emf.createEntityManager();
        List<Cita> lista = em.createQuery("SELECT c FROM Cita c", Cita.class).getResultList();
        em.close();
        return lista;
    }

    public Cita buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        Cita c = em.find(Cita.class, id);
        em.close();
        return c;
    }

    public void actualizar(Cita c) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(c);
        em.getTransaction().commit();
        em.close();
    }

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Cita c = em.find(Cita.class, id);
        if (c != null) em.remove(c);
        em.getTransaction().commit();
        em.close();
    }

    public List<Cita> listarTodos() {
        return buscarTodos();
    }

    public List<Cita> buscarPorMedico(Long medicoId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Cita c WHERE c.profesional.id = :medicoId",
                    Cita.class
            ).setParameter("medicoId", medicoId).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cita> buscarPorPaciente(long pacienteId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Cita c WHERE c.paciente.id = :pacienteId",
                            Cita.class
                    )
                    .setParameter("pacienteId", pacienteId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // ✅ NUEVOS MÉTODOS
    public List<Cita> buscarPorFecha(LocalDate fecha) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Cita c WHERE c.fecha = :fecha ORDER BY c.fecha ASC",
                            Cita.class
                    )
                    .setParameter("fecha", fecha)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Cita> buscarEnRango(LocalDate inicio, LocalDate fin) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Cita c WHERE c.fecha BETWEEN :inicio AND :fin ORDER BY c.fecha ASC",
                            Cita.class
                    )
                    .setParameter("inicio", inicio)
                    .setParameter("fin", fin)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}