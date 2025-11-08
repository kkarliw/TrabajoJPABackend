package consultorio.persistencia;

import consultorio.modelo.HistoriaClinica;
import jakarta.persistence.*;
import java.util.List;

public class HistoriaClinicaDAO {
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public HistoriaClinica crear(HistoriaClinica h) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(h);
            em.getTransaction().commit();
            return h;
        } finally {
            em.close();
        }
    }

    public static HistoriaClinica buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(HistoriaClinica.class, id);
        } finally {
            em.close();
        }
    }

    public static List<HistoriaClinica> buscarPorPaciente(long idPaciente) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT h FROM HistoriaClinica h WHERE h.paciente.id = :id ORDER BY h.fecha DESC",
                            HistoriaClinica.class)
                    .setParameter("id", idPaciente)
                    .getResultList();
        }
    }

    public static List<HistoriaClinica> buscarTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT h FROM HistoriaClinica h ORDER BY h.fecha DESC", HistoriaClinica.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
