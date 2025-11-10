package consultorio.persistencia;

import consultorio.modelo.Vacuna;
import jakarta.persistence.*;
import java.util.List;

public class VacunaDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public Vacuna crear(Vacuna v) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(v);
            em.getTransaction().commit();
            return v;
        } finally {
            em.close();
        }
    }

    public Vacuna buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Vacuna.class, id);
        } finally {
            em.close();
        }
    }

    public List<Vacuna> buscarPorPaciente(Long pacienteId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT v FROM Vacuna v WHERE v.paciente.id = :pacienteId ORDER BY v.fechaAplicacion DESC",
                    Vacuna.class
            ).setParameter("pacienteId", pacienteId).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Vacuna> buscarPendientes(Long pacienteId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT v FROM Vacuna v WHERE v.paciente.id = :pacienteId " +
                            "AND v.estado = 'PENDIENTE' ORDER BY v.proximaDosis ASC",
                    Vacuna.class
            ).setParameter("pacienteId", pacienteId).getResultList();
        } finally {
            em.close();
        }
    }

    public void actualizar(Vacuna v) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(v);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Vacuna v = em.find(Vacuna.class, id);
            if (v != null) em.remove(v);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}