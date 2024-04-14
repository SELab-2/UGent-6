package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.model.ProjectResponseJson;
import java.util.List;

public record userProjectsJson(List<ProjectResponseJsonWithStatus> enrolledProjects, List<ProjectResponseJson> adminProjects) {

}