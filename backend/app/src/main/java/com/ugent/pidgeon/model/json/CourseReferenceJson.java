package com.ugent.pidgeon.model.json;

public class CourseReferenceJson {
    private String name;
    private String url;
    private Long courseId;
    private Integer memberCount;
    private Boolean archived;

    public CourseReferenceJson(String name, String url, Long courseId, Integer memberCount,
        Boolean archived) {
        this.name = name;
        this.url = url;
        this.courseId = courseId;
        this.memberCount = memberCount;
        this.archived = archived;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}
