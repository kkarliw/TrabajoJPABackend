package consultorio.persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtils {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    // ✅ CORREGIDO: Tipo correcto sin casting erróneo
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}