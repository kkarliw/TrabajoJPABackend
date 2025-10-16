package consultorio.persistencia;

import consultorio.modelo.Paciente;
import javax.persistence.NoResultException;

public class PacienteDAO extends GenericDAO<Paciente, Number> {

    public PacienteDAO() {
        super(Paciente.class);
    }

    public Paciente buscarPorDocumento(String documento) {
        try {

            return em.createQuery("SELECT p FROM Paciente p WHERE p.documentoIdentidad = :doc", Paciente.class)
                    .setParameter("doc", documento)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}