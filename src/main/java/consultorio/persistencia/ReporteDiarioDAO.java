package consultorio.persistencia;

import consultorio.modelo.ReporteDiario;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

public class ReporteDiarioDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("consultorioPU");

    public ReporteDiario crear(ReporteDiario rd) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(rd);
            em.getTransaction().commit();
            return rd;
        } finally {
            em.close();
        }
    }

    public ReporteDiario buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(ReporteDiario.class, id);
        } finally {
            em.close();
        }
    }

    public List<ReporteDiario> buscarPorPaciente(Long pacienteId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT rd FROM ReporteDiario rd WHERE rd.paciente.id = :pacienteId ORDER BY rd.fecha DESC",
                    ReporteDiario.class
            ).setParameter("pacienteId", pacienteId).getResultList();
        } finally {
            em.close();
        }
    }

    public List<ReporteDiario> buscarPorCuidador(Long cuidadorId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT rd FROM ReporteDiario rd WHERE rd.cuidador.id = :cuidadorId ORDER BY rd.fecha DESC",
                    ReporteDiario.class
            ).setParameter("cuidadorId", cuidadorId).getResultList();
        } finally {
            em.close();
        }
    }

    public ReporteDiario buscarPorPacienteYFecha(Long pacienteId, LocalDate fecha) {
        EntityManager em = emf.createEntityManager();
        try {
            List<ReporteDiario> resultado = em.createQuery(
                            "SELECT rd FROM ReporteDiario rd WHERE rd.paciente.id = :pacienteId AND rd.fecha = :fecha",
                            ReporteDiario.class
                    )
                    .setParameter("pacienteId", pacienteId)
                    .setParameter("fecha", fecha)
                    .getResultList();

            return resultado.isEmpty() ? null : resultado.get(0);
        } finally {
            em.close();
        }
    }

    public void actualizar(ReporteDiario rd) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(rd);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void eliminar(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            ReporteDiario rd = em.find(ReporteDiario.class, id);
            if (rd != null) em.remove(rd);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}