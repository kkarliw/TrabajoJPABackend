package consultorio.services;

import consultorio.api.dto.EstadisticasDTO;
import consultorio.persistencia.*;
import java.time.LocalDate;

public class EstadisticasService {
    private PacienteDAO pacienteDAO = new PacienteDAO();
    private CitaDAO citaDAO = new CitaDAO();
    private ProfesionalSaludDAO profDAO = new ProfesionalSaludDAO();

    public EstadisticasDTO obtenerEstadisticas() {
        long totalPacientes = pacienteDAO.buscarTodos().size();
        long citasHoy = citaDAO.buscarTodos().stream()
                .filter(c -> c.getFecha().equals(LocalDate.now()))
                .count();
        long citasCompletadas = citaDAO.buscarTodos().stream()
                .filter(c -> c.getEstado().name().equals("COMPLETADA"))
                .count();
        long citasPendientes = citaDAO.buscarTodos().stream()
                .filter(c -> c.getEstado().name().equals("PENDIENTE"))
                .count();
        long totalCitas = citaDAO.buscarTodos().size();
        double porcentajeAsistencia = totalCitas > 0 ? (citasCompletadas / (double) totalCitas) * 100 : 0;

        long pacientesNuevos = pacienteDAO.buscarTodos().stream()
                .filter(p -> p.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        return new EstadisticasDTO(totalPacientes, citasHoy, citasCompletadas,
                citasPendientes, pacientesNuevos, porcentajeAsistencia);
    }

    public EstadisticasDTO obtenerEstadisticasPorMedico(Long medicoId) {
        long citasDelMedico = citaDAO.buscarTodos().stream()
                .filter(c -> c.getProfesional().getId().equals(Math.toIntExact(medicoId)))
                .count();
        long citasCompletadas = citaDAO.buscarTodos().stream()
                .filter(c -> c.getProfesional().getId().equals(Math.toIntExact(medicoId))
                        && c.getEstado().name().equals("COMPLETADA"))
                .count();
        long citasPendientes = citaDAO.buscarTodos().stream()
                .filter(c -> c.getProfesional().getId().equals(Math.toIntExact(medicoId))
                        && c.getEstado().name().equals("PENDIENTE"))
                .count();

        double porcentajeAsistencia = citasDelMedico > 0 ? (citasCompletadas / (double) citasDelMedico) * 100 : 0;

        return new EstadisticasDTO(0, citasDelMedico, citasCompletadas, citasPendientes, 0, porcentajeAsistencia);
    }
}
