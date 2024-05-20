package com.ugent.pidgeon.model.json;

// Hulpklasse die gebruikt wordt in Requestbodies.
public class CourseJson{
    private String name;

    private String description;

    private Boolean isArchived;

    private Integer year;

    public CourseJson(String name, String description, Boolean isArchived, Integer courseYear) {
        this.name = name;
        this.description = description;
        this.isArchived = isArchived;
        this.year = courseYear;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getArchived() {
        return isArchived;
    }

    public void setArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer courseYear) {
        this.year = courseYear;
    }
}

