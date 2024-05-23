package com.ugent.pidgeon.postgre.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class OffsetDateTimeSerializerTest {

  private OffsetDateTimeSerializer offsetDateTimeSerializer;
  private JsonGenerator jsonGenerator;
  private SerializerProvider serializerProvider;

  @BeforeEach
  public void setUp() {
    offsetDateTimeSerializer = new OffsetDateTimeSerializer();
    jsonGenerator = Mockito.mock(JsonGenerator.class);
    serializerProvider = Mockito.mock(SerializerProvider.class);
  }

  @Test
  public void testSerialize() throws IOException {
    OffsetDateTime now = OffsetDateTime.now();
    offsetDateTimeSerializer.serialize(now, jsonGenerator, serializerProvider);
    Mockito.verify(jsonGenerator).writeString(now.format(OffsetDateTimeSerializer.formatter));
  }
}