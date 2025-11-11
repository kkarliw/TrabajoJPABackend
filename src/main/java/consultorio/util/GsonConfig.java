package consultorio.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonConfig {

    public static Gson createGson() {
        return new GsonBuilder()
                // LocalDate adapter
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

                    @Override
                    public void write(JsonWriter out, LocalDate value) throws java.io.IOException {
                        out.value(value != null ? formatter.format(value) : null);
                    }

                    @Override
                    public LocalDate read(JsonReader in) throws java.io.IOException {
                        String dateStr = in.nextString();
                        return dateStr != null ? LocalDate.parse(dateStr, formatter) : null;
                    }
                })
                // LocalDateTime adapter
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

                    @Override
                    public void write(JsonWriter out, LocalDateTime value) throws java.io.IOException {
                        out.value(value != null ? formatter.format(value) : null);
                    }

                    @Override
                    public LocalDateTime read(JsonReader in) throws java.io.IOException {
                        String dateStr = in.nextString();
                        return dateStr != null ? LocalDateTime.parse(dateStr, formatter) : null;
                    }
                })
                // ✅ HIBERNATE PROXY ADAPTER - SOLUCIONA EL ERROR
                .registerTypeAdapter(HibernateProxy.class, new JsonSerializer<HibernateProxy>() {
                    @Override
                    public JsonElement serialize(HibernateProxy src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
                        try {
                            // Obtener el objeto real detrás del proxy
                            LazyInitializer initializer = ((HibernateProxy) src).getHibernateLazyInitializer();
                            Object target = initializer.getImplementation();
                            return context.serialize(target);
                        } catch (Exception e) {
                            return JsonNull.INSTANCE;
                        }
                    }
                })
                .serializeNulls()
                .setPrettyPrinting()
                .create();
    }
}