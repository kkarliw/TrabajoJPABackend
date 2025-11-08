package consultorio.persistencia;

import consultorio.modelo.Cuidador;
import jakarta.persistence.*;

import java.util.List;

public class CuidadorDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public Cuidador crear(Cuidador c) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(c);
        em.getTransaction().commit();
        em.close();
        return c;
    }

    public Cuidador buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        Cuidador c = em.find(Cuidador.class, id);
        em.close();
        return c;
    }

    public List<Cuidador> listarTodos() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Cuidador> q = em.createQuery("SELECT c FROM Cuidador c", Cuidador.class);
        List<Cuidador> res = q.getResultList();
        em.close();
        return res;
    }

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Cuidador c = em.find(Cuidador.class, id);
        if (c != null) em.remove(c);
        em.getTransaction().commit();
        em.close();
    }
}
