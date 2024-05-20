package com.ugent.pidgeon.model.json;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ugent.pidgeon.postgre.models.types.DockerTestType;

@JsonSerialize(using = DockerTestFeedbackJsonSerializer.class)
public record DockerTestFeedbackJson(
    DockerTestType type,
    String feedback,
    boolean allowed
) {

}
