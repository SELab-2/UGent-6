package com.ugent.pidgeon.model.submissionTesting;

public class DockerSubtestResult implements DockerOutput {
    private String correct;
    private String output;
    private String testName;
    private String testDescription = "";
    private boolean required = true;

    public DockerSubtestResult() {
    }

    public DockerSubtestResult(String correct, String output, String testName, String testDescription, boolean required) {
        this.correct = correct;
        this.output = output;
        this.testName = testName;
        this.testDescription = testDescription;
        this.required = required;
    }


    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {

        this.output = output;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean isAllowed() {
        return false;
    }

    @Override
    public String getFeedbackAsString() {
        // Display feedback as a json, only display testName and testDescription if they are not empty
        String testDescription = this.testDescription.isEmpty() ? "" : "\",\"testDescription\":\"" + this.testDescription;
        return "{\"testName\":\"" + testName + testDescription + "\",\"correct\":\"" + correct + "\",\"output\":\"" + output + "\"}";
    }
}
