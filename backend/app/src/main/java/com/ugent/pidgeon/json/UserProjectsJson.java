package com.ugent.pidgeon.json;

import com.ugent.pidgeon.model.ProjectResponseJson;
import java.util.List;

public record UserProjectsJson(List<ProjectResponseJsonWithStatus> enrolledProjects, List<ProjectResponseJson> adminProjects) {

}
