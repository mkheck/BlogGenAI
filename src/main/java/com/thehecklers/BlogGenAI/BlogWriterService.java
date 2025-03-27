package com.thehecklers.BlogGenAI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;

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
 *
 * The pattern involves multiple AI agents working together to iteratively improve content:
 * 1. Writer agent - Creates the initial draft and refines based on feedback
 * 2. Editor agent - Evaluates the draft and provides actionable feedback
 *
 * This iterative refinement continues until the content is approved or reaches max iterations.
 */
@Service
public class BlogWriterService {
    private static final int MAX_ITERATIONS = 3;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ChatClient chatClient;

    /**
     * Initialize the service with a ChatClient that has SimpleLoggerAdvisor.
     *
     * The SimpleLoggerAdvisor automatically logs all AI interactions (prompts and responses)
     * when the application's logging level is set to DEBUG for the advisor package.
     *
     * @param builder Builder for creating a configured ChatClient
     */
    public BlogWriterService(ChatClient.Builder builder) {
        // Add SimpleLoggerAdvisor to log requests and responses for debugging
        this.chatClient = builder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        logger.info("BlogWriterService initialized with ChatClient and SimpleLoggerAdvisor");
    }

    /**
     * Generates a concise blog post (max 10 sentences) using the Evaluator-Optimizer agent pattern.
     *
     * The method uses multiple AI agents to: (MH: Originally, no. Stay tuned.)
     * 1. Generate an initial draft
     * 2. Evaluate the draft for quality and brevity
     * 3. Provide feedback for improvement
     * 4. Refine the draft based on feedback
     * 5. Repeat until approved or max iterations reached
     *
     * @param topic The blog post topic
     * @return A refined blog post with a maximum of 10 sentences
     */
//    public String generateBlogPost(String topic) {
//        logger.info("Starting blog generation for topic: {}", topic);
//
//        // PHASE 1: WRITER AGENT
//        // Prompt the Writer agent to generate the initial blog draft
//        var initialPrompt = String.format("""
//            You are a professional blog writer. Write a well-structured, engaging blog post about "%s".
//            The post should have a clear introduction, body paragraphs, and conclusion.
//            Include relevant examples and maintain a conversational yet professional tone.
//
//            IMPORTANT FORMATTING REQUIREMENTS:
//            1. Format as plain text only (no Markdown, HTML, or special formatting)
//            2. Use simple ASCII characters only
//            3. For the title, simply put it on the first line and use ALL CAPS instead of "#" symbols
//            4. Separate paragraphs with blank lines
//            5. The blog post must be concise and contain NO MORE THAN 10 SENTENCES total.
//            """, topic);
//
//        // Using Spring AI's fluent API to send the prompt and get the response
//        logger.info("Sending initial draft generation prompt to AI model");
//        var draft = chatClient.prompt()
//                .user(initialPrompt)
//                .call()
//                .content();
//        logger.info("Initial draft successfully generated for topic: {}", topic);
//
//        // PHASE 2: EVALUATION & REFINEMENT LOOP
//        // Setup for the iterative improvement process
//        boolean approved = false;
//        int iteration = 1;
//        boolean forceFirstIteration = true; // Force at least one round of feedback to demonstrate the pattern
//
//        // Continue until we reach max iterations or get approval (but always do at least one iteration)
//        while ((!approved && iteration <= MAX_ITERATIONS) || forceFirstIteration) {
//            logger.info("Starting iteration {} of blog refinement", iteration);
//
//            // PHASE 2A: EDITOR AGENT
//            // Prompt the Editor agent to evaluate the current draft
//            var evalPrompt = String.format("""
//                You are a critical blog editor with extremely high standards. Evaluate the following blog draft and respond with either:
//                PASS - if the draft is exceptional, well-written, engaging, and complete
//                NEEDS_IMPROVEMENT - followed by specific, actionable feedback on what to improve
//
//                Focus on:
//                - Clarity and flow of ideas
//                - Engagement and reader interest
//                - Professional yet conversational tone
//                - Structure and organization
//                - Strict adherence to the 10-sentence maximum length requirement
//
//                IMPORTANT EVALUATION RULES:
//                1. The blog MUST have no more than 10 sentences total. Count the sentences carefully.
//                2. For the first iteration, ALWAYS respond with NEEDS_IMPROVEMENT regardless of quality.
//                3. Be extremely thorough in your evaluation and provide detailed feedback.
//                4. If the draft exceeds 10 sentences, it must receive a NEEDS_IMPROVEMENT rating.
//                5. Even well-written drafts should receive suggestions for improvement in early iterations.
//
//                Draft:
//                %s
//                """, draft);
//
//            // Send the evaluation prompt to the AI model
//            logger.info("Sending draft for editorial evaluation (iteration: {})", iteration);
//            var evaluation = chatClient.prompt()
//                    .user(evalPrompt)
//                    .call()
//                    .content();
//
//            // After first iteration, remove the force flag
//            if (forceFirstIteration) {
//                forceFirstIteration = false;
//            }
//
//            // Check if the Editor agent approves the draft
//            if (evaluation.toUpperCase().contains("PASS") && iteration > 1) { // Only allow PASS after first iteration
//                // Draft is approved, exit the loop
//                approved = true;
//                logger.info("Draft approved by editor on iteration {}", iteration);
//            } else {
//                // Draft needs improvement, extract the specific feedback
//                String feedback = extractFeedback(evaluation);
//                logger.info("Editor feedback received (iteration {}): {}", iteration, feedback);
//
//                // PHASE 2B: WRITER AGENT (REFINEMENT)
//                // Prompt the Writer agent to refine the draft based on the feedback
//                var refinePrompt = String.format("""
//                    You are a blog writer. Improve the following blog draft based on this editorial feedback:
//
//                    Feedback: %s
//
//                    Current Draft:
//                    %s
//
//                    IMPORTANT REQUIREMENTS:
//                    1. The final blog post MUST NOT exceed 10 sentences total.
//                    2. Maintain a clear introduction, body, and conclusion structure.
//                    3. Keep formatting as plain text only (NO Markdown, HTML, or special formatting)
//                    4. For the title, use ALL CAPS instead of any special formatting
//                    5. Separate paragraphs with blank lines
//                    6. Use only simple ASCII characters
//                    7. Provide the complete improved version while addressing the feedback.
//                    8. Count your sentences carefully before submitting.
//                    """, feedback, draft);
//
//                // Send the refinement prompt to the AI model
//                logger.info("Requesting draft revision based on feedback (iteration: {})", iteration);
//                draft = chatClient.prompt()
//                        .user(refinePrompt)
//                        .call()
//                        .content();
//                logger.info("Revised draft received for iteration {}", iteration);
//            }
//            iteration++;
//        }
//
//        // PHASE 3: FINALIZATION
//        // Return the final draft, either approved or after reaching max iterations
//        if (!approved) {
//            logger.warn("Maximum iterations ({}) reached without editor approval", MAX_ITERATIONS);
//        } else {
//            logger.info("Blog post generation completed successfully for topic: {}", topic);
//        }
//
//        return draft;
//    }

    /**
     * Enhanced version of generateBlogPost that also returns metadata about the generation process.
     *
     * This method ensures at least one feedback-improvement cycle occurs to demonstrate
     * the full evaluator-optimizer pattern in action, regardless of initial draft quality.
     *
     * @param topic The blog post topic
     * @return A BlogGenerationResult containing the content and metadata
     */
    public BlogGenerationResult generateBlogPostWithMetadata(String topic) {
        logger.info("Starting blog generation with metadata for topic: {}", topic);

        var result = new BlogGenerationResult();
        result.setModelName("Azure OpenAI");

        // PHASE 1: WRITER AGENT
        // Prompt the Writer agent to generate the initial blog draft
        var initialPrompt = String.format("""
            You are a professional blog writer. Write a well-structured, engaging blog post about "%s".
            The post should have a clear introduction, body paragraphs, and conclusion.
            Include relevant examples and maintain a conversational yet professional tone.
            
            IMPORTANT FORMATTING REQUIREMENTS:
            1. Format as plain text only (no Markdown, HTML, or special formatting)
            2. Use simple ASCII characters only
            3. For the title, simply put it on the first line and use ALL CAPS instead of "#" symbols
            4. Separate paragraphs with blank lines
            5. The blog post must be concise and contain NO MORE THAN 10 SENTENCES total.
            """, topic);

        // Using Spring AI's fluent API to send the prompt and get the response
        logger.info("Sending initial draft generation prompt to AI model");
        var draft = chatClient.prompt()
                .user(initialPrompt)
                .call()
                .content();

        // Estimate token usage as we can't directly access it
        estimateTokenUsage(result, initialPrompt, draft);
        logger.info("Initial draft successfully generated for topic: {}", topic);

        // PHASE 2: EVALUATION & REFINEMENT LOOP
        // Setup for the iterative improvement process
        boolean approved = false;
        int iteration = 1;
        boolean forceFirstIteration = true; // Force at least one feedback cycle to demonstrate the pattern

        // Continue until we reach max iterations or get approval (but always do at least one iteration)
        while ((!approved && iteration <= MAX_ITERATIONS) || forceFirstIteration) {
            logger.info("Starting iteration {} of blog refinement", iteration);

            // PHASE 2A: EDITOR AGENT
            // Prompt the Editor agent to evaluate the current draft
            var evalPrompt = String.format("""
                You are a critical blog editor with extremely high standards. Evaluate the following blog draft and respond with either:
                PASS - if the draft is exceptional, well-written, engaging, and complete
                NEEDS_IMPROVEMENT - followed by specific, actionable feedback on what to improve
                
                Focus on:
                - Clarity and flow of ideas
                - Engagement and reader interest
                - Professional yet conversational tone
                - Structure and organization
                - Strict adherence to the 10-sentence maximum length requirement
                
                IMPORTANT EVALUATION RULES:
                1. The blog MUST have no more than 10 sentences total. Count the sentences carefully.
                2. For the first iteration, ALWAYS respond with NEEDS_IMPROVEMENT regardless of quality.
                3. Be extremely thorough in your evaluation and provide detailed feedback.
                4. If the draft exceeds 10 sentences, it must receive a NEEDS_IMPROVEMENT rating.
                5. Even well-written drafts should receive suggestions for improvement in early iterations.
                
                Draft:
                %s
                """, draft);

            // Send the evaluation prompt to the AI model
            logger.info("Sending draft for editorial evaluation (iteration: {})", iteration);
            var evaluation = chatClient.prompt()
                    .user(evalPrompt)
                    .call()
                    .content();

            // After first iteration, remove the force flag
            if (forceFirstIteration) {
                forceFirstIteration = false;
            }

            estimateTokenUsage(result, evalPrompt, evaluation);

            // Check if the Editor agent approves the draft
            if (evaluation.toUpperCase().contains("PASS") && iteration > 1) { // Only allow PASS after first iteration
                // Draft is approved, exit the loop
                approved = true;
                logger.info("Draft approved by editor on iteration {}", iteration);
            } else {
                // Draft needs improvement, extract the specific feedback
                var feedback = extractFeedback(evaluation);
                logger.info("Editor feedback received (iteration {}): {}", iteration, feedback);
                result.addEditorFeedback(feedback);

                // PHASE 2B: WRITER AGENT (REFINEMENT)
                // Prompt the Writer agent to refine the draft based on the feedback
                var refinePrompt = String.format("""
                    You are a blog writer. Improve the following blog draft based on this editorial feedback:
                    
                    Feedback: %s
                    
                    Current Draft:
                    %s
                    
                    IMPORTANT REQUIREMENTS:
                    1. The final blog post MUST NOT exceed 10 sentences total.
                    2. Maintain a clear introduction, body, and conclusion structure.
                    3. Keep formatting as plain text only (NO Markdown, HTML, or special formatting)
                    4. For the title, use ALL CAPS instead of any special formatting
                    5. Separate paragraphs with blank lines
                    6. Use only simple ASCII characters
                    7. Provide the complete improved version while addressing the feedback.
                    8. Count your sentences carefully before submitting.
                    """, feedback, draft);

                // Send the refinement prompt to the AI model
                logger.info("Requesting draft revision based on feedback (iteration: {})", iteration);
                var revisedDraft = chatClient.prompt()
                        .user(refinePrompt)
                        .call()
                        .content();

                estimateTokenUsage(result, refinePrompt, revisedDraft);
                draft = revisedDraft;
                logger.info("Revised draft received for iteration {}", iteration);
            }
            iteration++;
        }

        // PHASE 3: FINALIZATION
        // Set final result properties
        result.setContent(draft);
        result.setApproved(approved);
        result.setIterations(iteration - 1);

        if (!approved) {
            logger.warn("Maximum iterations ({}) reached without editor approval", MAX_ITERATIONS);
        } else {
            logger.info("Blog post generation completed successfully for topic: {}", topic);
        }

        return result;
    }

    /**
     * Helper method to extract actionable feedback from the Editor agent's evaluation.
     * This extracts the text after "NEEDS_IMPROVEMENT" to get just the feedback portion.
     *
     * @param evaluation The full evaluation text from the Editor agent
     * @return Just the actionable feedback portion
     */
    private String extractFeedback(String evaluation) {
        if (evaluation == null) return "";
        int idx = evaluation.toUpperCase().indexOf("NEEDS_IMPROVEMENT");

        return idx != -1
                ? evaluation.substring(idx + "NEEDS_IMPROVEMENT".length()).trim()
                : evaluation;
    }

    /**
     * Helper method to estimate token usage as we can't directly access it
     * This is a rough estimation: approximately 4 characters per token
     */
    private void estimateTokenUsage(BlogGenerationResult result, String prompt, String response) {
        try {
            // Very rough estimation: ~4 characters per token
            int estimatedPromptTokens = prompt.length() / 4;
            int estimatedCompletionTokens = response.length() / 4;

            result.addPromptTokens(estimatedPromptTokens);
            result.addCompletionTokens(estimatedCompletionTokens);

            logger.debug("Estimated token usage: prompt={}, completion={}, total={}",
                    estimatedPromptTokens, estimatedCompletionTokens,
                    estimatedPromptTokens + estimatedCompletionTokens);
        } catch (Exception e) {
            logger.warn("Failed to estimate token usage", e);
        }
    }
}