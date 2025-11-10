package consultorio.persistencia;

import consultorio.modelo.profesionales.ProfesionalSalud;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class ProfesionalSaludDAO {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public void crear(ProfesionalSalud profesional) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(profesional);
        em.getTransaction().commit();
        em.close();
    }

    public List<ProfesionalSalud> buscarTodos() {
        EntityManager em = emf.createEntityManager();
        List<ProfesionalSalud> lista = em.createQuery("SELECT p FROM ProfesionalSalud p", ProfesionalSalud.class).getResultList();
        em.close();
        return lista;
    }

    // ✅ CORREGIDO: Integer en lugar de Long
    public ProfesionalSalud buscarPorId(Integer id) {
        EntityManager em = emf.createEntityManager();
        ProfesionalSalud p = em.find(ProfesionalSalud.class, id);
        em.close();
        return p;
    }

    public void actualizar(ProfesionalSalud profesional) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(profesional);
        em.getTransaction().commit();
        em.close();
    }

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ProfesionalSalud p = em.find(ProfesionalSalud.class, id.intValue());
        if (p != null) em.remove(p);
        em.getTransaction().commit();
        em.close();
    }
}