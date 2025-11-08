package consultorio.persistencia;

import consultorio.modelo.Incapacidad;
import jakarta.persistence.*;
import java.util.List;

public class IncapacidadDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public Incapacidad crear(Incapacidad i) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(i);
            em.getTransaction().commit();
            return i;
        } finally {
            em.close();
        }
    }

    public List<Incapacidad> buscarPorPaciente(long pacienteId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT i FROM Incapacidad i WHERE i.paciente.id = :id ORDER BY i.fechaInicio DESC", Incapacidad.class)
                    .setParameter("id", pacienteId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Incapacidad buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Incapacidad.class, id);
        } finally {
            em.close();
        }
    }
}
