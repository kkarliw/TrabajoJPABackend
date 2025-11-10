package consultorio.persistencia;

import consultorio.modelo.SignosVitales;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

public class SignosVitalesDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public SignosVitales crear(SignosVitales sv) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(sv);
            em.getTransaction().commit();
            return sv;
        } finally {
            em.close();
        }
    }

    public SignosVitales buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(SignosVitales.class, id);
        } finally {
            em.close();
        }
    }

    public List<SignosVitales> buscarPorPaciente(Long pacienteId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT sv FROM SignosVitales sv WHERE sv.paciente.id = :pacienteId ORDER BY sv.fechaRegistro DESC",
                    SignosVitales.class
            ).setParameter("pacienteId", pacienteId).getResultList();
        } finally {
            em.close();
        }
    }

    public List<SignosVitales> buscarPorPacienteRangoFecha(Long pacienteId, LocalDateTime desde, LocalDateTime hasta) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT sv FROM SignosVitales sv WHERE sv.paciente.id = :pacienteId " +
                                    "AND sv.fechaRegistro BETWEEN :desde AND :hasta ORDER BY sv.fechaRegistro DESC",
                            SignosVitales.class
                    )
                    .setParameter("pacienteId", pacienteId)
                    .setParameter("desde", desde)
                    .setParameter("hasta", hasta)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public SignosVitales obtenerUltimo(Long pacienteId) {
        EntityManager em = emf.createEntityManager();
        try {
            List<SignosVitales> resultado = em.createQuery(
                            "SELECT sv FROM SignosVitales sv WHERE sv.paciente.id = :pacienteId ORDER BY sv.fechaRegistro DESC",
                            SignosVitales.class
                    )
                    .setParameter("pacienteId", pacienteId)
                    .setMaxResults(1)
                    .getResultList();

            return resultado.isEmpty() ? null : resultado.get(0);
        } finally {
            em.close();
        }
    }

    public void actualizar(SignosVitales sv) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(sv);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            SignosVitales sv = em.find(SignosVitales.class, id);
            if (sv != null) em.remove(sv);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}