package com.ugent.pidgeon.json;

public class UserReferenceWithRelation {
  private UserReferenceJson user;
  private String relation;

  public UserReferenceWithRelation(UserReferenceJson user, String relation) {
    this.user = user;
    this.relation = relation;
  }

  public UserReferenceJson getUser() {
    return user;
  }

  public void setUser(UserReferenceJson user) {
    this.user = user;
  }

  public String getRelation() {
    return relation;
  }

  public void setRelation(String relation) {
    this.relation = relation;
  }

}
