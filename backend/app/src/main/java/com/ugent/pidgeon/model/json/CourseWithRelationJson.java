package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import java.time.OffsetDateTime;

public class CourseWithRelationJson {
    private Long courseId;
    private String url;
    private String name;
    private OffsetDateTime archivedAt;
    private CourseRelation relation;
    private Integer memberCount;



    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public CourseWithRelationJson(String url, CourseRelation relation, String name, Long courseId,
        OffsetDateTime archivedAt, Integer memberCount) {
        this.url = url;
        this.relation = relation;
        this.name = name;
        this.courseId = courseId;
        this.archivedAt = archivedAt;
        this.memberCount = memberCount;
    }


    public CourseRelation getRelation() {
        return relation;
    }

    public void setRelation(CourseRelation relation) {
        this.relation = relation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long id) {
        this.courseId = id;
    }

    public OffsetDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(OffsetDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }
}
