package consultorio.persistencia;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtils {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public static <EntityManager> EntityManager getEntityManager() {
        return (EntityManager) emf.createEntityManager();
    }
}
