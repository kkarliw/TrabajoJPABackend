package consultorio.persistencia;

import jakarta.persistence.EntityManager;
import java.util.List;

public abstract class GenericDAO<T, N> {
    protected EntityManager em;
    private Class<T> entityClass;

    public GenericDAO(Class<T> entityClass) {
        this.em = JPAUtils.getEntityManager();
        this.entityClass = entityClass;
    }

    public void crear(T entidad) {
        try {
            em.getTransaction().begin();
            em.persist(entidad);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public T buscarPorId(Long id) {
        return em.find(entityClass, id);
    }

    public List<T> buscarTodos() {
        return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass).getResultList();
    }

    public void actualizar(T entidad) {
        try {
            em.getTransaction().begin();
            em.merge(entidad);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void eliminar(Long id) {
        try {
            em.getTransaction().begin();
            T e = em.find(entityClass, id);
            if (e != null) em.remove(e);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        }
    }
}
