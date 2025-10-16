package MainTest;

import consultorio.modelo.Cita;
import consultorio.modelo.Consultorio;
import consultorio.modelo.Paciente;
import consultorio.modelo.profesionales.Medico;
import consultorio.persistencia.CitaDAO;
import consultorio.persistencia.ConsultorioDAO;
import consultorio.persistencia.JPAUtils;
import consultorio.persistencia.PacienteDAO;
import consultorio.persistencia.ProfesionalSaludDAO;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        PacienteDAO pacienteDAO = new PacienteDAO();
        ProfesionalSaludDAO profesionalDAO = new ProfesionalSaludDAO();
        ConsultorioDAO consultorioDAO = new ConsultorioDAO();
        CitaDAO citaDAO = new CitaDAO();

        System.out.println("--- CREANDO ENTIDADES ---");

        Paciente paciente1 = new Paciente("Carlos", "Rojas", "123456");
        pacienteDAO.guardar(paciente1);
        System.out.println("Paciente guardado con ID: " + paciente1.getId());

        Medico medico1 = new Medico("Ana", "Gomez", "Cardiología");
        profesionalDAO.guardar(medico1);
        System.out.println("Médico guardado con ID: " + medico1.getId());

        Consultorio consultorio1 = new Consultorio("101", "Piso 1");
        consultorioDAO.guardar(consultorio1);
        System.out.println("Consultorio guardado con ID: " + consultorio1.getId());

        Cita cita1 = new Cita(LocalDateTime.now().plusDays(5), "Chequeo general", paciente1, medico1, consultorio1);
        citaDAO.guardar(cita1);
        System.out.println("Cita guardada con ID: " + cita1.getId());

        System.out.println("\n--- REALIZANDO CONSULTAS ---");

        Paciente pacienteEncontrado = pacienteDAO.buscarPorId(paciente1.getId());
        System.out.println("Paciente encontrado: " + pacienteEncontrado.getNombre() + " " + pacienteEncontrado.getApellido());

        pacienteEncontrado.setApellido("Perez");
        pacienteDAO.actualizar(pacienteEncontrado);
        System.out.println("Apellido del paciente actualizado a: " + pacienteDAO.buscarPorId(paciente1.getId()).getApellido());

        System.out.println("\n--- Lista de todos los pacientes ---");
        pacienteDAO.buscarTodos().forEach(p -> System.out.println("- " + p.getNombre()));

        System.out.println("\n--- ELIMINANDO CITA ---");
        citaDAO.eliminar(cita1.getId());
        Cita citaEliminada = citaDAO.buscarPorId(cita1.getId());
        System.out.println("Cita después de eliminar: " + (citaEliminada == null ? "No encontrada (correcto)" : "Error, no se eliminó"));


        JPAUtils.shutdown();
    }
}