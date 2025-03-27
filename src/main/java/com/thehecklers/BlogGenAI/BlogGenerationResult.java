package com.thehecklers.BlogGenAI;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold blog generation result, including the content and metadata.
 */
public class BlogGenerationResult {
    private String content;
    private int iterations;
    private boolean approved;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private String modelName;
    private List<String> editorFeedback = new ArrayList<>();

    // Getters and setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
        this.totalTokens = this.promptTokens + this.completionTokens;
    }

    public void addPromptTokens(int tokens) {
        this.promptTokens += tokens;
        this.totalTokens = this.promptTokens + this.completionTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
        this.totalTokens = this.promptTokens + this.completionTokens;
    }

    public void addCompletionTokens(int tokens) {
        this.completionTokens += tokens;
        this.totalTokens = this.promptTokens + this.completionTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<String> getEditorFeedback() {
        return editorFeedback;
    }

    public void setEditorFeedback(List<String> editorFeedback) {
        this.editorFeedback = editorFeedback;
    }

    public void addEditorFeedback(String feedback) {
        if (this.editorFeedback == null) {
            this.editorFeedback = new ArrayList<>();
        }
        this.editorFeedback.add(feedback);
    }
}