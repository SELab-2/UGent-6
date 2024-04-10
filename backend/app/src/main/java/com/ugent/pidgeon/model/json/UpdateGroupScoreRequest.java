package com.ugent.pidgeon.model.json;

public class UpdateGroupScoreRequest {
    private Float score;
    private String feedback;


    public Float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
