package consultorio.persistencia;

import consultorio.modelo.Cita;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
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

    public List<Cita> buscarPorPaciente(long pacienteId) {
        EntityManager em = emf.createEntityManager();
        List<Cita> citas = null;
        try {
            citas = em.createQuery(
                            "SELECT c FROM Cita c WHERE c.paciente.id = :pacienteId",
                            Cita.class
                    )
                    .setParameter("pacienteId", pacienteId)
                    .getResultList();
        } finally {
            em.close();
        }
        return citas;
    }

}