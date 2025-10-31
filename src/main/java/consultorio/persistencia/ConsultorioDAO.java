package consultorio.persistencia;

import consultorio.modelo.Consultorio;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ConsultorioDAO<N> extends GenericDAO<Consultorio, N> {

    public ConsultorioDAO() {
        super(Consultorio.class);
    }

    public List<Consultorio> buscarPorPiso(String piso) {
        EntityManager em = null;
        return em.createQuery("SELECT c FROM Consultorio c WHERE c.piso = :piso", Consultorio.class)
                 .setParameter("piso", piso)
                 .getResultList();
    }

}