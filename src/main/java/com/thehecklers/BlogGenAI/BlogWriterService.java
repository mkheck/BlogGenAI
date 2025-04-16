package com.thehecklers.BlogGenAI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;

/*
    Mark's notes:
    Version 1 of this service was copied directly from the following training module:
    https://learn.microsoft.com/en-us/training/modules/build-enterprise-ai-agents-with-java-spring/

    Subsequent versions will be modified in various ways, including changing/updating AI model(s) used,
    code refinements, and other tweaks that will be documented in the commit messages.

    Additionally, blog posts will be published -- and clearly marked as AI-generated -- at thehecklers.com
    under the "AI" category. This is intended to be an informative exercise in AI capabilities and limitations.
 */

/**
 * This service demonstrates the Evaluator-Optimizer agent pattern using Spring AI.
 * <p>
 * The pattern involves multiple AI agents working together to iteratively improve content:
 * 1. Writer agent - Creates the initial draft and refines based on feedback
 * 2. Editor agent - Evaluates the draft and provides actionable feedback (remote)
 * <p>
 * This iterative refinement continues until the content is approved or reaches max iterations.
 */
@Service
public class BlogWriterService {
    private static final int MAX_ITERATIONS = 3;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ChatClient aiClient;
    private final RestClient remoteClient;
    @Value("${ai.editor.url:http://localhost:8090/api/edit}")
    private String editorUrl;

    public BlogWriterService(ChatClient.Builder builder) {
        // Add SimpleLoggerAdvisor to log requests and responses for debugging
        this.aiClient = builder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        remoteClient = RestClient.builder()
                .baseUrl(editorUrl)
                .build();
        logger.info("BlogWriterService initialized with ChatClient and SimpleLoggerAdvisor");
    }

    public BlogGeneration generateBlogPostWithMetadata(String topic) {
        logger.info("Starting blog generation with metadata for topic: {}", topic);

        var feedbackList = new ArrayList<String>();

        // PHASE 1: WRITER AGENT
        // Prompt the Writer agent to generate the initial blog draft
        var prompt = String.format("""
                You are a professional blog writer. Write a well-structured, engaging blog post about "%s".
                The post should have a clear introduction, body paragraphs, and conclusion.
                Include relevant examples and maintain a conversational yet professional tone.
                
                IMPORTANT FORMATTING REQUIREMENTS:
                1. Format as plain text only (no Markdown, HTML, or special formatting)
                2. Use simple ASCII characters only
                3. For the title, simply put it on the first line and use ALL CAPS instead of "#" symbols
                4. Separate paragraphs with blank lines
                5. The blog post must be concise and contain NO MORE THAN 15 SENTENCES total.
                """, topic);

        // Using Spring AI's fluent API to send the prompt and get the response
        logger.info("Sending initial draft generation prompt to AI model");
        var draft = aiClient.prompt()
                .user(prompt)
                .call()
                .content();
        logger.info("Initial draft successfully generated for topic: {}", topic);

        // PHASE 2: EVALUATION & REFINEMENT LOOP
        // Setup for the iterative improvement process
        boolean approved = false;
        int iteration = 1;

        // Continue until we reach max iterations or get approval (but always do at least one iteration)
        while (!approved && iteration <= MAX_ITERATIONS) {
            logger.info("Starting iteration {} of blog refinement", iteration);

            // PHASE 2A: EDITOR AGENT
            // Prompt the Editor agent to evaluate the current draft
            logger.info("Sending draft for editorial evaluation (iteration: {})", iteration);
            DraftCritique editorFeedback = remoteClient.post()
                    .body(new DraftRequestSpec(15, draft))
                    .retrieve()
                    .toEntity(DraftCritique.class)
                    .getBody();

            // Check if the Editor agent approves the draft
            if (editorFeedback.approval()) {
                // Draft is approved, exit the loop
                approved = true;
                logger.info("Draft approved by editor on iteration {}", iteration);
            } else {
                // Draft needs improvement, extract the specific feedback
                logger.info("Editor feedback received (iteration {}): {}", iteration, editorFeedback.critique());
                feedbackList.add(editorFeedback.critique());

                // PHASE 2B: WRITER AGENT (REFINEMENT)
                // Prompt the Writer agent to refine the draft based on the feedback
                var refinePrompt = String.format("""
                        You are a blog writer. Improve the following blog draft based on this editorial feedback:
                        
                        Feedback: %s
                        
                        Current Draft:
                        %s
                        
                        IMPORTANT REQUIREMENTS:
                        1. The final blog post MUST NOT exceed 15 sentences total.
                        2. Maintain a clear introduction, body, and conclusion structure.
                        3. Keep formatting as plain text only (NO Markdown, HTML, or special formatting)
                        4. For the title, use ALL CAPS instead of any special formatting
                        5. Separate paragraphs with blank lines
                        6. Use only simple ASCII characters
                        7. Provide the complete improved version while addressing the feedback.
                        8. Count your sentences carefully before submitting.
                        """, editorFeedback.critique(), draft);

                // Send the refinement prompt to the AI model
                logger.info("Requesting draft revision based on feedback (iteration: {})", iteration);
                draft = aiClient.prompt()
                        .user(refinePrompt)
                        .call()
                        .content();
                logger.info("Revised draft received for iteration {}", iteration);
            }
            iteration++;
        }

        // PHASE 3: FINALIZATION
        // Set final result properties
        var gen = new BlogGeneration(draft,
                iteration - 1,
                approved,
                -1L,
                -1L,
                -1L,
                "Azure OpenAI",
                feedbackList);

        if (!approved) {
            logger.warn("Maximum iterations ({}) reached without editor approval", MAX_ITERATIONS);
        } else {
            logger.info("Blog post generation completed successfully for topic: {}", topic);
        }

        return gen;
    }

/*    private String extractFeedback(@NotNull String evaluation) {
        //if (evaluation == null) return "";
        int idx = evaluation.toUpperCase().indexOf("NEEDS_IMPROVEMENT");

        return idx != -1
                ? evaluation.substring(idx + "NEEDS_IMPROVEMENT".length()).trim()
                : evaluation;
    }*/
}