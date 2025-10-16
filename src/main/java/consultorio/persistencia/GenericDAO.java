package consultorio.persistencia;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class GenericDAO<T, N> {
    private final Class<T> entityClass;
    protected EntityManager em;

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.em = JPAUtils.getEntityManagerFactory().createEntityManager();
    }

    public void guardar(T entidad) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entidad);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public T buscarPorId(Object id) {
        return em.find(entityClass, id);
    }

    public void actualizar(T entidad) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(entidad);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public void eliminar(Object id) {
        T entidad = buscarPorId(id);
        if (entidad != null) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.remove(entidad);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    public List<T> buscarTodos() {
        return em.createQuery("FROM " + entityClass.getName(), entityClass).getResultList();
    }
}