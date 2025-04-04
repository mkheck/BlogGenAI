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
public class BlogWriterController {

    private final BlogWriterService blogWriterService;

    public BlogWriterController(BlogWriterService blogWriterService) {
        this.blogWriterService = blogWriterService;
    }

    @GetMapping
    public String quickTest(@RequestParam(defaultValue = "Tell me a joke") String message) {
        return blogWriterService.aiTest(message);
    }

    @GetMapping("/api/blog")
    public Map<String, Object> generateBlogPost(@RequestParam String topic) {
        var result = blogWriterService.generateBlogPostWithMetadata(topic);

        return Map.of("topic", topic,
                "content", result.content(),
                "metadata", createMetadataObject(result));
    }

    //private Map<String, Object> createMetadataObject(BlogGenerationResult result) {
    private Map<String, Object> createMetadataObject(BlogGeneration result) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("iterations", result.iterations());
        metadata.put("approved", result.approved());
        metadata.put("totalTokensUsed", result.totalTokens());

        if (result.editorFeedback() != null && !result.editorFeedback().isEmpty()) {
            List<Map<String, Object>> feedbackHistory = new ArrayList<>();

            for (int i = 0; i < result.editorFeedback().size(); i++) {
                Map<String, Object> feedbackEntry = Map.of("iteration", i + 1,
                        "feedback", result.editorFeedback().get(i));

                feedbackHistory.add(feedbackEntry);
            }

            metadata.put("editorFeedback", feedbackHistory);
        }

        // Include token usage statistics if available
        if (result.promptTokens() > 0) {
            Map<String, Object> tokenUsage = Map.of("promptTokens", result.promptTokens(),
                    "completionTokens", result.completionTokens(),
                    "totalTokens", result.totalTokens());

            metadata.put("tokenUsage", tokenUsage);
        }

        // Include model information if available
        if (result.modelName() != null) {
            metadata.put("model", result.modelName());
        }

        return metadata;
    }
}