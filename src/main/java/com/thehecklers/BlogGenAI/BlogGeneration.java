package com.thehecklers.BlogGenAI;

import java.util.List;

public record BlogGeneration(String content,
                             int iterations,
                             boolean approved,
                             long promptTokens,
                             long completionTokens,
                             long totalTokens,
                             String modelName,
                             List<String> editorFeedback) {
}
