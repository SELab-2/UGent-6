package com.ugent.pidgeon.model.json;

public enum ProjectStatus {
  not_started,
  correct,
  incorrect,
  no_group;

  @Override
  public String toString() {
    if (this == ProjectStatus.not_started) {
      return "not started";
    } else if (this == ProjectStatus.no_group) {
      return "no group";
    }
    return super.toString();
  }
}
