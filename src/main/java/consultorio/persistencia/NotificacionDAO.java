package consultorio.persistencia;

import consultorio.modelo.Notificacion;
import jakarta.persistence.*;
import java.util.List;

public class NotificacionDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public Notificacion crear(Notificacion n) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(n);
            em.getTransaction().commit();
            return n;
        } finally {
            em.close();
        }
    }

    public List<Notificacion> buscarPorDestinatario(Long usuarioId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT n FROM Notificacion n WHERE n.destinatario.id = :usuarioId ORDER BY n.createdAt DESC",
                    Notificacion.class
            ).setParameter("usuarioId", usuarioId).getResultList();
        } finally {
            em.close();
        }
    }

    public Notificacion buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Notificacion.class, id);
        } finally {
            em.close();
        }
    }

    public void actualizar(Notificacion n) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(n);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // ✅ NUEVO MÉTODO
    public List<Notificacion> buscarNoLeidas(Long usuarioId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT n FROM Notificacion n WHERE n.destinatario.id = :usuarioId AND n.leida = false ORDER BY n.createdAt DESC",
                            Notificacion.class
                    )
                    .setParameter("usuarioId", usuarioId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
