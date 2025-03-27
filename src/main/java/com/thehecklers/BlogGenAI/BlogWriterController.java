package com.thehecklers.BlogGenAI;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/*
    Mark's notes:
    Version 1 of this class was copied directly from the following training module:
    https://learn.microsoft.com/en-us/training/modules/build-enterprise-ai-agents-with-java-spring/

    Subsequent versions will be modified in various ways, especially code refinements,
    and other tweaks that will be documented in the commit messages.

    Additionally, blog posts will be published -- and clearly marked as AI-generated -- at thehecklers.com
    under the "AI" category. This is intended to be an informative exercise in AI capabilities and limitations.
 */
@RestController
@RequestMapping("/api/blog")
public class BlogWriterController {

    private final BlogWriterService blogWriterService;

    public BlogWriterController(BlogWriterService blogWriterService) {
        this.blogWriterService = blogWriterService;
    }

    @GetMapping
    public Map<String, Object> generateBlogPost(@RequestParam String topic) {
        var result = blogWriterService.generateBlogPostWithMetadata(topic);

        return Map.of("topic", topic,
                "content", result.getContent(),
                "metadata", createMetadataObject(result));
    }

    private Map<String, Object> createMetadataObject(BlogGenerationResult result) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("iterations", result.getIterations());
        metadata.put("approved", result.isApproved());
        metadata.put("totalTokensUsed", result.getTotalTokens());

        if (result.getEditorFeedback() != null && !result.getEditorFeedback().isEmpty()) {
            List<Map<String, Object>> feedbackHistory = new ArrayList<>();

            for (int i = 0; i < result.getEditorFeedback().size(); i++) {
                Map<String, Object> feedbackEntry = Map.of("iteration", i + 1,
                        "feedback", result.getEditorFeedback().get(i));

                feedbackHistory.add(feedbackEntry);
            }

            metadata.put("editorFeedback", feedbackHistory);
        }

        // Include token usage statistics if available
        if (result.getPromptTokens() > 0) {
            Map<String, Object> tokenUsage = Map.of("promptTokens", result.getPromptTokens(),
                    "completionTokens", result.getCompletionTokens(),
                    "totalTokens", result.getTotalTokens());

            metadata.put("tokenUsage", tokenUsage);
        }

        // Include model information if available
        if (result.getModelName() != null) {
            metadata.put("model", result.getModelName());
        }

        return metadata;
    }
}