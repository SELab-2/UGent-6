package com.ugent.pidgeon.model.json;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.ugent.pidgeon.postgre.models.types.DockerTestType;
import java.io.IOException;

public class DockerTestFeedbackJsonSerializer extends JsonSerializer<DockerTestFeedbackJson> {

  @Override
  public void serialize(DockerTestFeedbackJson value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeStartObject();
    gen.writeStringField("type", value.type().toString());
    if (value.type() == DockerTestType.TEMPLATE) {
      gen.writeFieldName("feedback");
      gen.writeRawValue(value.feedback().replace("\n", "\\n"));
    } else {
      gen.writeStringField("feedback", value.feedback());
    }
    gen.writeBooleanField("allowed", value.allowed());
    gen.writeEndObject();
  }
}

