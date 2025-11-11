package consultorio.api.controller;

import com.google.gson.Gson;
import consultorio.modelo.HistoriaClinica;
import consultorio.modelo.Paciente;
import consultorio.modelo.profesionales.ProfesionalSalud;
import consultorio.persistencia.HistoriaClinicaDAO;
import consultorio.persistencia.PacienteDAO;
import consultorio.persistencia.ProfesionalSaludDAO;
import consultorio.util.GsonConfig;
import jakarta.persistence.EntityManager;
import spark.Spark;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HistoriaClinicaController {
    private HistoriaClinicaDAO historiaDAO;
    private PacienteDAO pacienteDAO;
    private ProfesionalSaludDAO profesionalDAO;
    private EntityManager em;
    private Gson gson;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public HistoriaClinicaController(HistoriaClinicaDAO historiaDAO, PacienteDAO pacienteDAO,
                                     ProfesionalSaludDAO profesionalDAO, EntityManager em) {
        this.historiaDAO = historiaDAO;
        this.pacienteDAO = pacienteDAO;
        this.profesionalDAO = profesionalDAO;
        this.em = em;
        this.gson = GsonConfig.createGson();
    }

    // DTO simple para evitar proxies
    public static class HistoriaDTO {
        public Long id;
        public String diagnostico;
        public String motivoConsulta;
        public String tratamiento;
        public String formulaMedica;
        public String observaciones;
        public Boolean requiereIncapacidad;
        public LocalDate fecha;
        public Map<String, Object> paciente;
        public Map<String, Object> profesional;

        public HistoriaDTO(HistoriaClinica h) {
            this.id = h.getId();
            this.diagnostico = h.getDiagnostico();
            this.motivoConsulta = h.getMotivoConsulta();
            this.tratamiento = h.getTratamiento();
            this.formulaMedica = h.getFormulaMedica();
            this.observaciones = h.getObservaciones();
            this.requiereIncapacidad = h.getRequiereIncapacidad();
            this.fecha = h.getFecha();

            // Crear mapas simples sin proxies
            this.paciente = new HashMap<>();
            if (h.getPaciente() != null) {
                this.paciente.put("id", h.getPaciente().getId());
                this.paciente.put("nombre", h.getPaciente().getNombre());
                this.paciente.put("apellido", h.getPaciente().getApellido());
                this.paciente.put("numeroDocumento", h.getPaciente().getNumeroDocumento());
            }

            this.profesional = new HashMap<>();
            if (h.getProfesional() != null) {
                this.profesional.put("id", h.getProfesional().getId());
                this.profesional.put("nombre", h.getProfesional().getNombre());
                this.profesional.put("especialidad", h.getProfesional().getEspecialidad());
            }
        }
    }

    public void registerRoutes() {
        // POST - Crear historia clínica
        Spark.post("/api/historias", (req, res) -> {
            res.type("application/json");
            System.out.println("🎯 === CREANDO HISTORIA CLÍNICA ===");
            System.out.println("BODY: " + req.body());

            try {
                Map<String, Object> input = gson.fromJson(req.body(), Map.class);
                System.out.println("✅ Input parseado:");

                // Obtener datos
                Double pacienteIdDouble = (Double) input.get("pacienteId");
                Double profesionalIdDouble = (Double) input.get("profesionalId");
                String diagnostico = (String) input.get("diagnostico");
                String motivoConsulta = (String) input.get("motivoConsulta");
                String tratamiento = (String) input.get("tratamiento");
                String formulaMedica = (String) input.get("formulaMedica");
                String observaciones = (String) input.get("observaciones");
                Object requiereObj = input.get("requiereIncapacidad");
                Boolean requiereIncapacidad = requiereObj != null ? (Boolean) requiereObj : false;

                int pacienteId = pacienteIdDouble != null ? pacienteIdDouble.intValue() : 0;
                int profesionalId = profesionalIdDouble != null ? profesionalIdDouble.intValue() : 0;

                System.out.println("   - Paciente ID: " + pacienteId);
                System.out.println("   - Profesional ID: " + profesionalId);

                if (pacienteId == 0 || profesionalId == 0) {
                    System.out.println("❌ Datos incompletos");
                    res.status(400);
                    return gson.toJson(Map.of("error", "Paciente y Profesional son requeridos"));
                }

                // Obtener entidades
                Paciente paciente = pacienteDAO.buscarPorId((long) pacienteId);
                ProfesionalSalud profesional = profesionalDAO.buscarPorId(profesionalId);

                if (paciente == null || profesional == null) {
                    System.out.println("❌ Paciente o Profesional no encontrado");
                    res.status(404);
                    return gson.toJson(Map.of("error", "Paciente o Profesional no encontrado"));
                }

                // Crear historia
                HistoriaClinica historia = new HistoriaClinica();
                historia.setPaciente(paciente);
                historia.setProfesional(profesional);
                historia.setDiagnostico(diagnostico);
                historia.setMotivoConsulta(motivoConsulta);
                historia.setTratamiento(tratamiento);
                historia.setFormulaMedica(formulaMedica);
                historia.setObservaciones(observaciones);
                historia.setRequiereIncapacidad(requiereIncapacidad);
                historia.setFecha(LocalDate.now());

                historiaDAO.crear(historia);
                System.out.println("✅ Historia clínica creada: " + historia.getId());

                // ✅ Retornar como DTO (sin proxies)
                HistoriaDTO dto = new HistoriaDTO(historia);
                res.status(201);
                return gson.toJson(dto);

            } catch (Exception e) {
                System.out.println("❌ Error al guardar historia: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Error: " + e.getMessage()));
            }
        });

        // GET - Listar todas las historias
        Spark.get("/api/historias", (req, res) -> {
            res.type("application/json");
            System.out.println("🎯 === LISTANDO HISTORIAS CLÍNICAS ===");
            try {
                List<HistoriaClinica> historias = historiaDAO.buscarTodas();
                System.out.println("✅ Historias encontradas: " + historias.size());

                // ✅ Convertir a DTOs (sin proxies)
                List<HistoriaDTO> dtos = new ArrayList<>();
                for (HistoriaClinica h : historias) {
                    try {
                        dtos.add(new HistoriaDTO(h));
                    } catch (Exception e) {
                        System.out.println("⚠️ Error convirtiendo historia " + h.getId() + ": " + e.getMessage());
                    }
                }

                System.out.println("✅ DTOs creados: " + dtos.size());
                return gson.toJson(dtos);

            } catch (Exception e) {
                System.out.println("❌ Error al listar historias: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of("error", "Error: " + e.getMessage()));
            }
        });

        // GET - Obtener por ID
        Spark.get("/api/historias/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                HistoriaClinica historia = historiaDAO.buscarPorId((long) id);

                if (historia == null) {
                    res.status(404);
                    return gson.toJson(Map.of("error", "Historia no encontrada"));
                }

                // ✅ Retornar como DTO
                HistoriaDTO dto = new HistoriaDTO(historia);
                return gson.toJson(dto);

            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Error: " + e.getMessage()));
            }
        });
    }
}