package consultorio.persistencia;

import consultorio.modelo.profesionales.ProfesionalSalud;
import java.util.List;

public class ProfesionalSaludDAO<N> extends GenericDAO<ProfesionalSalud, N> {

    public ProfesionalSaludDAO() {
        super(ProfesionalSalud.class);
    }

    public List<ProfesionalSalud> buscarPorApellido(String apellido) {
        return em.createQuery("SELECT p FROM ProfesionalSalud p WHERE p.apellido = :apellido", ProfesionalSalud.class)
                 .setParameter("apellido", apellido)
                 .getResultList();
    }
}