package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.postgre.models.types.CourseRelation;

public class RelationRequest {
  private String relation;

  public RelationRequest() {
  }

  public RelationRequest(String relation) {
    this.relation = relation;
  }

  public String getRelation() {
    return relation;
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
}
