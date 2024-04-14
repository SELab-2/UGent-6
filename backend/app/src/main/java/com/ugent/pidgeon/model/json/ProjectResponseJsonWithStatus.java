package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.model.ProjectResponseJson;

public record ProjectResponseJsonWithStatus(ProjectResponseJson project, String status) {}
