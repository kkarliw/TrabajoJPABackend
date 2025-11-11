package consultorio.persistencia;

import consultorio.modelo.HistoriaClinica;
import jakarta.persistence.*;

import java.util.List;

public class HistoriaClinicaDAO {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public void crear(HistoriaClinica historia) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(historia);
        em.getTransaction().commit();
        em.close();
    }

    // ✅ ARREGLADO - Usar EAGER LOADING con JOIN FETCH
    public List<HistoriaClinica> buscarTodas() {
        EntityManager em = emf.createEntityManager();
        try {
            // Usar JOIN FETCH para cargar paciente y profesional en la misma consulta
            Query query = em.createQuery(
                    "SELECT h FROM HistoriaClinica h " +
                            "JOIN FETCH h.paciente " +
                            "JOIN FETCH h.profesional",
                    HistoriaClinica.class
            );
            List<HistoriaClinica> result = query.getResultList();

            // ✅ NO cerrar la sesión aún - dejar que el controller use los datos
            // em.close(); // Comentado
            return result;
        } catch (Exception e) {
            System.out.println("❌ Error en buscarTodas: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        } finally {
            em.close(); // Cerrar después de usar
        }
    }

    public HistoriaClinica buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            HistoriaClinica h = em.find(HistoriaClinica.class, id);
            return h;
        } finally {
            em.close();
        }
    }

    public List<HistoriaClinica> buscarPorPaciente(Long pacienteId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT h FROM HistoriaClinica h " +
                                    "JOIN FETCH h.paciente " +
                                    "JOIN FETCH h.profesional " +
                                    "WHERE h.paciente.id = :pacienteId",
                            HistoriaClinica.class
                    )
                    .setParameter("pacienteId", pacienteId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<HistoriaClinica> buscarPorProfesional(Long profesionalId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT h FROM HistoriaClinica h " +
                                    "JOIN FETCH h.paciente " +
                                    "JOIN FETCH h.profesional " +
                                    "WHERE h.profesional.id = :profesionalId",
                            HistoriaClinica.class
                    ).setParameter("profesionalId", profesionalId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void actualizar(HistoriaClinica historia) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(historia);
        em.getTransaction().commit();
        em.close();
    }

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        HistoriaClinica h = em.find(HistoriaClinica.class, id);
        if (h != null) {
            em.remove(h);
        }
        em.getTransaction().commit();
        em.close();
    }
}