package com.ugent.pidgeon.json;

import com.ugent.pidgeon.postgre.models.types.CourseRelation;

public class CourseMemberRequestJson {
    private Long userId;
    private String relation;

    // Constructor
    public CourseMemberRequestJson() {}

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public CourseRelation getRelationAsEnum() {
        try {
            return CourseRelation.valueOf(relation);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
    public String getRelation() {
        return relation;
    }
}
