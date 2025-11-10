package consultorio.persistencia;

import consultorio.modelo.Consultorio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class ConsultorioDAO {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public void crear(Consultorio c) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(c);
        em.getTransaction().commit();
        em.close();
    }

    public Consultorio buscarPorId(Integer id) {
        EntityManager em = emf.createEntityManager();
        Consultorio c = em.find(Consultorio.class, id);
        em.close();
        return c;
    }

    public List<Consultorio> buscarTodos() {
        EntityManager em = emf.createEntityManager();
        List<Consultorio> lista = em.createQuery("SELECT c FROM Consultorio c", Consultorio.class).getResultList();
        em.close();
        return lista;
    }

    public void actualizar(Consultorio c) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(c);
        em.getTransaction().commit();
        em.close();
    }

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Consultorio c = em.find(Consultorio.class, id.intValue());
        if (c != null) em.remove(c);
        em.getTransaction().commit();
        em.close();
    }

    public List<Consultorio> buscarPorUbicacion(String ubicacion) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Consultorio c WHERE c.ubicacion = :ubicacion", Consultorio.class)
                    .setParameter("ubicacion", ubicacion)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}