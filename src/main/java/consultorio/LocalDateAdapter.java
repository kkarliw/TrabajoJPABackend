package consultorio;

import com.google.gson.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.lang.reflect.Type;

public class LocalDateAdapter implements JsonDeserializer<LocalDate>, JsonSerializer<LocalDate> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String dateString = json.getAsString();

        try {
            // Maneja formato ISO: "2001-10-19"
            if (dateString.contains("T")) {
                // Si viene con hora, extrae solo la fecha
                dateString = dateString.split("T")[0];
            }
            return LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            throw new JsonParseException("Error parsing date: " + dateString, e);
        }
    }

    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(formatter.format(src));
    }
}