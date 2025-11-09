
package consultorio.persistencia;

import consultorio.modelo.Paciente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class PacienteDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public void crear(Paciente p) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(p);
        em.getTransaction().commit();
        em.close();
    }

    public List<Paciente> buscarTodos() {
        EntityManager em = emf.createEntityManager();
        List<Paciente> lista = em.createQuery("SELECT p FROM Paciente p", Paciente.class).getResultList();
        em.close();
        return lista;
    }

    public Paciente buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        Paciente p = em.find(Paciente.class, id);
        em.close();
        return p;
    }

    public void actualizar(Paciente p) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(p);
        em.getTransaction().commit();
        em.close();
    }

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Paciente p = em.find(Paciente.class, id);
        if (p != null) em.remove(p);
        em.getTransaction().commit();
        em.close();
    }

    public List<Paciente> listarTodos() {
        return buscarTodos();
    }

    // ✅ NUEVOS MÉTODOS
    public List<Paciente> buscarPorNombre(String nombre) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Paciente p WHERE LOWER(p.nombre) LIKE LOWER(:nombre) OR LOWER(p.apellido) LIKE LOWER(:nombre)",
                            Paciente.class
                    )
                    .setParameter("nombre", "%" + nombre + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Paciente buscarPorDocumento(String numeroDocumento) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Paciente p WHERE p.numeroDocumento = :documento",
                            Paciente.class
                    )
                    .setParameter("documento", numeroDocumento)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }
}