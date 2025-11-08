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
    public List<Paciente> listarTodos() { return buscarTodos(); }
}
