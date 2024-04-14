package com.ugent.pidgeon.postgre.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.time.OffsetDateTime;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;


public class OffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Override
    public void serialize(OffsetDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Format the OffsetDateTime into a string with the desired format
        String formattedValue = formatter.format(value);
        Logger.getGlobal().info("Formatted value: " + formattedValue);
        // Write the formatted string to the JSON generator
        gen.writeString(formattedValue);
    }


}