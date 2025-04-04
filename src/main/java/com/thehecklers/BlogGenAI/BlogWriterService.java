package com.thehecklers.BlogGenAI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
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
    //private final ChatClient ollamaClient;
    private final ChatClient oaiClient;
    private final ChatClient aoaiClient;

//    /**
//     * Initialize the service with a ChatClient that has SimpleLoggerAdvisor.
//     *
//     * The SimpleLoggerAdvisor automatically logs all AI interactions (prompts and responses)
//     * when the application's logging level is set to DEBUG for the advisor package.
//     *
//     * @param builder Builder for creating a configured ChatClient
//     */
//    public BlogWriterService(ChatClient.Builder builder) {
//        // Add SimpleLoggerAdvisor to log requests and responses for debugging
//        this.aoaiClient = builder
//                .defaultAdvisors(new SimpleLoggerAdvisor())
//                .build();
//        logger.info("BlogWriterService initialized with ChatClient and SimpleLoggerAdvisor");
//    }
    //public BlogWriterService(@Qualifier("ollamaClient") ChatClient ollamaClient,
    public BlogWriterService(@Qualifier("openaiChatClient") ChatClient oaiClient,
                             @Qualifier("openaiChatClient") ChatClient aoaiClient) {
        // Add SimpleLoggerAdvisor to log requests and responses for debugging MH: Move to Config
        //this.ollamaClient = ollamaClient;
        this.oaiClient = oaiClient;
        this.aoaiClient = aoaiClient;
        //logger.info("BlogWriterService initialized with ChatClient and SimpleLoggerAdvisor");
    }

    public String aiTest(String message) {
        //return ollamaClient.prompt()
        return aoaiClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * This method ensures at least one feedback-improvement cycle occurs to demonstrate
     * the full evaluator-optimizer pattern in action, regardless of initial draft quality.
     *
     * @param topic The blog post topic
     * @return A BlogGenerationResult containing the content and metadata
     */
    public BlogGeneration generateBlogPostWithMetadata(String topic) {
        logger.info("Starting blog generation with metadata for topic: {}", topic);

        var totalPromptChars = 0L;
        var totalCompletionChars = 0L;
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
        //var draft = ollamaClient.prompt()
        var draft = aoaiClient.prompt()
                .user(prompt)
                .call()
                .content();

        totalPromptChars += prompt.length();
        totalCompletionChars += draft.length();
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
            prompt = String.format("""
                You are a critical blog editor with extremely high standards. Evaluate the following blog draft and respond with either:
                PASS - if the draft is exceptional, well-written, engaging, and complete
                NEEDS_IMPROVEMENT - followed by specific, actionable feedback on what to improve
                
                Focus on:
                - Clarity and flow of ideas
                - Engagement and reader interest
                - Professional yet conversational tone
                - Structure and organization
                - Strict adherence to the 15-sentence maximum length requirement
                
                IMPORTANT EVALUATION RULES:
                1. The blog MUST have no more than 15 sentences total. Count the sentences carefully.
                2. For the first iteration, ALWAYS respond with NEEDS_IMPROVEMENT regardless of quality.
                3. Be extremely thorough in your evaluation and provide detailed feedback.
                4. If the draft exceeds 15 sentences, it must receive a NEEDS_IMPROVEMENT rating.
                5. Even well-written drafts should receive suggestions for improvement in early iterations.
                
                Draft:
                %s
                """, draft);

            // Send the evaluation prompt to the AI model
            logger.info("Sending draft for editorial evaluation (iteration: {})", iteration);
            //var evaluation = aoaiClient.prompt()
            var evaluation = oaiClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            totalPromptChars += prompt.length();
            totalCompletionChars += evaluation.length();

            // Check if the Editor agent approves the draft
            if (evaluation.toUpperCase().contains("PASS") && iteration > 1) { // Only allow PASS after first iteration
                // Draft is approved, exit the loop
                approved = true;
                logger.info("Draft approved by editor on iteration {}", iteration);
            } else {
                // Draft needs improvement, extract the specific feedback
                var feedback = extractFeedback(evaluation);
                logger.info("Editor feedback received (iteration {}): {}", iteration, feedback);
                feedbackList.add(feedback);

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
                    """, feedback, draft);

                // Send the refinement prompt to the AI model
                logger.info("Requesting draft revision based on feedback (iteration: {})", iteration);
                //var revisedDraft = ollamaClient.prompt()
                var revisedDraft = aoaiClient.prompt()
                        .user(refinePrompt)
                        .call()
                        .content();

                totalPromptChars += refinePrompt.length();
                totalCompletionChars += revisedDraft.length();
                draft = revisedDraft;
                logger.info("Revised draft received for iteration {}", iteration);
            }
            iteration++;
        }

        // PHASE 3: FINALIZATION
        // Set final result properties
        // Estimating token counts for now (~4 characters per token)
        var gen = new BlogGeneration(draft,
                iteration - 1,
                approved,
                totalPromptChars / 4,
                totalCompletionChars / 4,
                (totalPromptChars + totalCompletionChars) / 4,
                "Azure OpenAI",
                feedbackList);

        if (!approved) {
            logger.warn("Maximum iterations ({}) reached without editor approval", MAX_ITERATIONS);
        } else {
            logger.info("Blog post generation completed successfully for topic: {}", topic);
        }

        return gen;
    }

    /**
     * Helper method to extract actionable feedback from the Editor agent's evaluation.
     * This extracts the text after "NEEDS_IMPROVEMENT" to get just the feedback portion.
     *
     * @param evaluation The full evaluation text from the Editor agent
     * @return Just the actionable feedback portion
     */
    private String extractFeedback(@NotNull String evaluation) {
        //if (evaluation == null) return "";
        int idx = evaluation.toUpperCase().indexOf("NEEDS_IMPROVEMENT");

        return idx != -1
                ? evaluation.substring(idx + "NEEDS_IMPROVEMENT".length()).trim()
                : evaluation;
    }
}