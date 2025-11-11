package consultorio.util;

import com.google.gson.*;
import consultorio.modelo.Paciente;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonConfig {
    public static Gson createGson() {
        return new GsonBuilder()
                // ✅ CRÍTICO: Registrar ANTES de construir el Gson
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws java.io.IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(formatter.format(value));
                        }
                    }

                    @Override
                    public LocalDateTime read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
                        switch (in.peek()) {
                            case NULL:
                                in.nextNull();
                                return null;
                            default:
                                String dateString = in.nextString();
                                if (dateString.isEmpty()) {
                                    return null;
                                }
                                try {
                                    return LocalDateTime.parse(dateString, formatter);
                                } catch (Exception e) {
                                    try {
                                        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
                                        return date.atStartOfDay();
                                    } catch (Exception e2) {
                                        return LocalDateTime.now();
                                    }
                                }
                        }
                    }
                })

                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, LocalDate value) throws java.io.IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(formatter.format(value));
                        }
                    }

                    @Override
                    public LocalDate read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
                        switch (in.peek()) {
                            case NULL:
                                in.nextNull();
                                return null;
                            default:
                                String dateString = in.nextString();
                                if (dateString.contains("T")) {
                                    dateString = dateString.split("T")[0];
                                }
                                return LocalDate.parse(dateString, formatter);
                        }
                    }
                })

                .registerTypeAdapter(Paciente.Genero.class, new TypeAdapter<Paciente.Genero>() {
                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, Paciente.Genero value) throws java.io.IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(value.name());
                        }
                    }

                    @Override
                    public Paciente.Genero read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
                        switch (in.peek()) {
                            case NULL:
                                in.nextNull();
                                return null;
                            default:
                                String value = in.nextString().toUpperCase();
                                return Paciente.Genero.valueOf(value);
                        }
                    }
                })

                .setPrettyPrinting()
                .create();
    }
}