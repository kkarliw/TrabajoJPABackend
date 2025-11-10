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

    // ✅ CORREGIDO: Cambiar Integer a Long
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

    // ✅ CORREGIDO: Ya no necesitas .intValue()
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

    // ✅ CORREGIDO: Ya no necesitas .intValue()
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

    // ✅ CORREGIDO: Cambiar long primitivo a Long objeto y eliminar cast
    public List<Cita> buscarPorPaciente(Long pacienteId) {
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
