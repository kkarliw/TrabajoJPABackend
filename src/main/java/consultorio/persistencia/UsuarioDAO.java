package consultorio.persistencia;

import consultorio.modelo.Usuario;
import jakarta.persistence.*;


public class UsuarioDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public UsuarioDAO() {
    }

    public void crear(Usuario u) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(u);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
    public Usuario buscarPorEmail(String email) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }
    public Usuario validarUsuario(String correo, String password) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.correo = :correo AND u.password = :password", Usuario.class);
            query.setParameter("correo", correo);
            query.setParameter("password", password);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }
    
    public Usuario buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        Usuario u = em.find(Usuario.class, id);
        em.close();
        return u;
    }

    public void guardarUsuario(Usuario nuevoUsuario) {
    }
}
