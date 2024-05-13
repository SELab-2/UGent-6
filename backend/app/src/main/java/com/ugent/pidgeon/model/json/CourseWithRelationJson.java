package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import java.time.OffsetDateTime;

public record CourseWithRelationJson (String url, CourseRelation relation, String name, Long courseId,
                                      OffsetDateTime archivedAt, Integer memberCount, OffsetDateTime createdAt, Integer year) { }

