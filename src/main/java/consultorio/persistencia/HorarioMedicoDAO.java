package consultorio.persistencia;

import consultorio.modelo.HorarioMedico;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HorarioMedicoDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public HorarioMedico buscarPorProfesionalId(Long profesionalId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT h FROM HorarioMedico h WHERE h.profesional.id = :profesionalId",
                    HorarioMedico.class
            ).setParameter("profesionalId", profesionalId).getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public HorarioMedico buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(HorarioMedico.class, id);
        } finally {
            em.close();
        }
    }

    public void crear(HorarioMedico horario) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(horario);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void actualizar(HorarioMedico horario) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(horario);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            HorarioMedico h = em.find(HorarioMedico.class, id);
            if (h != null) em.remove(h);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}