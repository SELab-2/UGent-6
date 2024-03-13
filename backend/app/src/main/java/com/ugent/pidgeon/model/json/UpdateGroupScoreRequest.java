package com.ugent.pidgeon.model.json;

public class UpdateGroupScoreRequest {
    private float score;
    private String feedback;

    // Getters and setters (omitted for brevity)

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }
}
