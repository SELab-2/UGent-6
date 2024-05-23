package com.ugent.pidgeon.json;

import com.ugent.pidgeon.model.ProjectResponseJson;

public record ProjectResponseJsonWithStatus(ProjectResponseJson project, String status) {}
