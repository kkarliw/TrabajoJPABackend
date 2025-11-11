package consultorio.persistencia;

import consultorio.modelo.HistoriaClinica;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.NoResultException;
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

    public List<HistoriaClinica> buscarTodos() {
        EntityManager em = emf.createEntityManager();
        List<HistoriaClinica> lista = em.createQuery(
                "SELECT h FROM HistoriaClinica h",
                HistoriaClinica.class
        ).getResultList();
        em.close();
        return lista;
    }

    public HistoriaClinica buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        HistoriaClinica h = em.find(HistoriaClinica.class, id);
        em.close();
        return h;
    }

    public List<HistoriaClinica> buscarPorPaciente(Long pacienteId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT h FROM HistoriaClinica h WHERE h.paciente.id = :pacienteId",
                            HistoriaClinica.class
                    )
                    .setParameter("pacienteId", pacienteId)
                    .getResultList(); // ✅ devuelve List<HistoriaClinica>
        } finally {
            em.close();
        }
    }


    public List<HistoriaClinica> buscarPorProfesional(Long profesionalId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT h FROM HistoriaClinica h WHERE h.profesional.id = :profesionalId",
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
