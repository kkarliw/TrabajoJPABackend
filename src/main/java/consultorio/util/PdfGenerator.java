package consultorio.util;

import consultorio.modelo.HistoriaClinica;
import consultorio.modelo.Incapacidad;
import consultorio.modelo.Paciente;
import consultorio.modelo.profesionales.ProfesionalSalud;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

public class PdfGenerator {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static byte[] generarHistoria(HistoriaClinica h) throws Exception {
        PDDocument doc = new PDDocument();
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float marginLeft = 50;
            float y = 800;
            float leading = 16;

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.newLineAtOffset(marginLeft, y);
            cs.showText("Resumen de Consulta / Historia Clínica");
            cs.endText();

            y -= leading * 2;

            // Datos paciente + profesional
            PatientAndProf(h, cs, marginLeft, y);
            y -= leading * 6;

            // Motivo
            writeSection(cs, "Motivo", h.getMotivoConsulta(), marginLeft, y);
            y -= leading * 4;

            // Diagnóstico
            writeSection(cs, "Diagnóstico", h.getDiagnostico(), marginLeft, y);
            y -= leading * 4;

            // Tratamiento
            writeSection(cs, "Tratamiento", h.getTratamiento(), marginLeft, y);
            y -= leading * 4;

            // Observaciones
            writeSection(cs, "Observaciones", h.getObservaciones(), marginLeft, y);
            y -= leading * 4;

            // Fórmula/Receta
            writeSection(cs, "Receta / Fórmula", h.getFormulaMedica(), marginLeft, y);
            y -= leading * 4;

            // Incapacidad (si aplica)
            if (h.setRequiereIncapacidad()) {
                writeSection(cs, "Nota: Se ha solicitado incapacidad (ver registro)", "", marginLeft, y);
                y -= leading * 2;
            }

            cs.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } finally {
            doc.close();
        }
    }

    private static void PatientAndProf(HistoriaClinica h, PDPageContentStream cs, float x, float y) throws Exception {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(x, y);
        Paciente p = h.getPaciente();
        ProfesionalSalud pro = h.getProfesional();
        cs.showText("Paciente: " + (p != null ? p.getNombre() : "N/D") + "   ID: " + (p != null && p.getId() != null ? p.getId() : "N/D"));
        cs.newLineAtOffset(0, -14);
        cs.showText("Profesional: " + (pro != null ? pro.getNombre() : "N/D"));
        cs.newLineAtOffset(0, -14);
        cs.showText("Fecha: " + (h.getFecha() != null ? h.getFecha().format(DTF) : "N/D"));
        cs.endText();
    }

    private static void writeSection(PDPageContentStream cs, String title, String text, float x, float y) throws Exception {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(x, y);
        cs.showText(title + ":");
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(x, y - 14);
        if (text == null || text.isEmpty()) text = "-";
        // simple wrapping: split lines to 90 chars
        int max = 90;
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + max, text.length());
            String line = text.substring(start, end);
            cs.showText(line);
            cs.newLineAtOffset(0, -12);
            start = end;
        }
        cs.endText();
    }

    public static byte[] generarIncapacidad(Incapacidad i) throws Exception {
        PDDocument doc = new PDDocument();
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float marginLeft = 50;
            float y = 800;
            float leading = 16;

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.newLineAtOffset(marginLeft, y);
            cs.showText("Certificado de Incapacidad");
            cs.endText();

            y -= leading * 2;

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 11);
            cs.newLineAtOffset(marginLeft, y);
            cs.showText("Paciente: " + (i.getPaciente() != null ? i.getPaciente().getNombre() : "N/D"));
            cs.newLineAtOffset(0, -14);
            cs.showText("Profesional: " + (i.getProfesional() != null ? i.getProfesional().getNombre() : "N/D"));
            cs.newLineAtOffset(0, -14);
            cs.showText("Fecha inicio: " + (i.getFechaInicio() != null ? i.getFechaInicio().format(DTF) : "N/D"));
            cs.newLineAtOffset(0, -14);
            cs.showText("Días de reposo: " + i.getDiasReposo());
            cs.newLineAtOffset(0, -14);
            cs.showText("Motivo: " + (i.getMotivo() != null ? i.getMotivo() : "-"));
            cs.endText();

            cs.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } finally {
            doc.close();
        }
    }
}
