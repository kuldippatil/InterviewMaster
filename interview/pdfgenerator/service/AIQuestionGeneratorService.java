package com.interview.pdfgenerator.service;

import com.interview.pdfgenerator.model.InterviewQuestion;
import com.interview.pdfgenerator.model.JobDescription;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for generating interview questions using AI.
 */
@Service
public class AIQuestionGeneratorService {

    private static final String OLLAMA_API_URL = "http://localhost:11500/api/generate";
    private static final String DEFAULT_MODEL = "llama3.1:latest";
    private static final int QUESTIONS_PER_CATEGORY = 20;
    private static final int CONCURRENT_REQUESTS = 5;
    
    private final ExecutorService executorService;
    
    public AIQuestionGeneratorService() {
        this.executorService = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
    }
    
    /**
     * Generate interview questions based on job description.
     * 
     * @param jobDescription The job description to generate questions for
     * @return A map of categories to lists of questions
     */
    public Map<String, List<InterviewQuestion>> generateQuestionsForJobDescription(JobDescription jobDescription) {
        Map<String, List<InterviewQuestion>> result = new HashMap<>();
        
        // Define categories based on job description
        List<String> categories = getCategoriesFromJobDescription(jobDescription);
        
        // Generate questions for each category in parallel
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (String category : categories) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<InterviewQuestion> questions = generateQuestionsForCategory(category, jobDescription);
                synchronized (result) {
                    result.put(category, questions);
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        return result;
    }
    
    /**
     * Generate questions for a specific category.
     * 
     * @param category The category to generate questions for
     * @param jobDescription The job description to base questions on
     * @return A list of interview questions
     */
    private List<InterviewQuestion> generateQuestionsForCategory(String category, JobDescription jobDescription) {
        List<InterviewQuestion> questions = new ArrayList<>();
        
        // Define subcategories for each category
        List<String> subcategories = getSubcategoriesForCategory(category, jobDescription);
        
        // Generate questions for each subcategory
        for (String subcategory : subcategories) {
            try {
                // Generate questions using AI
                List<InterviewQuestion> generatedQuestions = 
                        generateQuestionsUsingAI(category, subcategory, QUESTIONS_PER_CATEGORY / subcategories.size(), jobDescription);
                
                questions.addAll(generatedQuestions);
            } catch (Exception e) {
                System.err.println("Error generating questions for " + category + " - " + subcategory + ": " + e.getMessage());
                // Add a fallback question if AI generation fails
                questions.add(new InterviewQuestion(
                        category, 
                        subcategory,
                        "Explain the key concepts of " + subcategory + " in " + category,
                        "This is a placeholder answer. The AI-generated content could not be retrieved."
                ));
            }
        }
        
        return questions;
    }
    
    /**
     * Generate questions using AI.
     * 
     * @param category The category of questions
     * @param subcategory The subcategory of questions
     * @param count The number of questions to generate
     * @param jobDescription The job description to base questions on
     * @return A list of generated questions
     * @throws Exception If question generation fails
     */
    private List<InterviewQuestion> generateQuestionsUsingAI(
            String category, String subcategory, int count, JobDescription jobDescription) throws Exception {
        
        List<InterviewQuestion> questions = new ArrayList<>();
        
        // Create prompt for AI
        String prompt = createPromptForAI(category, subcategory, count, jobDescription);
        
        // Call AI API
        String response = callOllamaAPI(prompt);
        
        // Parse response
        questions.addAll(parseAIResponse(response, category, subcategory));
        
        return questions;
    }
    
    /**
     * Create a prompt for the AI to generate questions.
     * 
     * @param category The category of questions
     * @param subcategory The subcategory of questions
     * @param count The number of questions to generate
     * @param jobDescription The job description to base questions on
     * @return A prompt for the AI
     */
    private String createPromptForAI(String category, String subcategory, int count, JobDescription jobDescription) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Generate ").append(count).append(" detailed technical interview questions and answers about ")
              .append(subcategory).append(" in ").append(category).append(" for a ")
              .append(jobDescription.getTitle()).append(" position.\n\n");
        
        prompt.append("Job skills include: ").append(String.join(", ", jobDescription.getSkills())).append("\n");
        prompt.append("Technologies include: ").append(String.join(", ", jobDescription.getTechnologies())).append("\n\n");
        
        prompt.append("For each question, provide:\n");
        prompt.append("1. A challenging, specific technical question that would be asked in an interview\n");
        prompt.append("2. A comprehensive, detailed answer (at least 400 words) with examples, best practices, and technical details\n\n");
        
        prompt.append("Format your response as JSON with this structure:\n");
        prompt.append("{\n");
        prompt.append("  \"questions\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"question\": \"Question text here\",\n");
        prompt.append("      \"answer\": \"Detailed answer here\"\n");
        prompt.append("    },\n");
        prompt.append("    ...\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * Call the Ollama API to generate questions.
     * 
     * @param prompt The prompt to send to the API
     * @return The API response
     * @throws Exception If the API call fails
     */
    private String callOllamaAPI(String prompt) throws Exception {
        URL url = new URL(OLLAMA_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", DEFAULT_MODEL);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        
        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        // Parse JSON response
        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getString("response");
    }
    
    /**
     * Parse the AI response to extract questions and answers.
     * 
     * @param response The AI response
     * @param category The category of questions
     * @param subcategory The subcategory of questions
     * @return A list of interview questions
     */
    private List<InterviewQuestion> parseAIResponse(String response, String category, String subcategory) {
        List<InterviewQuestion> questions = new ArrayList<>();
        
        try {
            // Try to parse as JSON
            if (response.contains("{") && response.contains("}")) {
                String jsonStr = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
                JSONObject json = new JSONObject(jsonStr);
                
                if (json.has("questions")) {
                    JSONArray questionsArray = json.getJSONArray("questions");
                    
                    for (int i = 0; i < questionsArray.length(); i++) {
                        JSONObject questionObj = questionsArray.getJSONObject(i);
                        String questionText = questionObj.getString("question");
                        String answerText = questionObj.getString("answer");
                        
                        questions.add(new InterviewQuestion(category, subcategory, questionText, answerText));
                    }
                }
            }
            
            // If JSON parsing failed or no questions were found, try to parse as text
            if (questions.isEmpty()) {
                String[] lines = response.split("\n");
                String currentQuestion = null;
                StringBuilder currentAnswer = new StringBuilder();
                
                for (String line : lines) {
                    if (line.startsWith("Q:") || line.startsWith("Question:")) {
                        // Save previous question if exists
                        if (currentQuestion != null && currentAnswer.length() > 0) {
                            questions.add(new InterviewQuestion(category, subcategory, currentQuestion, currentAnswer.toString().trim()));
                            currentAnswer = new StringBuilder();
                        }
                        
                        // Extract new question
                        currentQuestion = line.substring(line.indexOf(":") + 1).trim();
                    } else if (line.startsWith("A:") || line.startsWith("Answer:")) {
                        // Start of answer
                        currentAnswer.append(line.substring(line.indexOf(":") + 1).trim()).append("\n");
                    } else if (currentQuestion != null) {
                        // Continue answer
                        currentAnswer.append(line).append("\n");
                    }
                }
                
                // Add the last question
                if (currentQuestion != null && currentAnswer.length() > 0) {
                    questions.add(new InterviewQuestion(category, subcategory, currentQuestion, currentAnswer.toString().trim()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + e.getMessage());
        }
        
        // If still no questions, create a fallback question
        if (questions.isEmpty()) {
            questions.add(new InterviewQuestion(
                    category, 
                    subcategory,
                    "Explain the key concepts of " + subcategory + " in " + category,
                    "This is a placeholder answer. The AI-generated content could not be parsed correctly."
            ));
        }
        
        return questions;
    }
    
    /**
     * Get categories based on job description.
     * 
     * @param jobDescription The job description to analyze
     * @return A list of categories
     */
    private List<String> getCategoriesFromJobDescription(JobDescription jobDescription) {
        List<String> categories = new ArrayList<>();
        
        // Always include Core Java
        categories.add("Core Java");
        
        // Check for Spring
        if (containsAnyIgnoreCase(jobDescription.getTechnologies(), "spring", "spring boot", "spring framework")) {
            categories.add("Spring & Spring Boot");
        }
        
        // Check for REST/Microservices
        if (containsAnyIgnoreCase(jobDescription.getTechnologies(), "rest", "api", "microservices", "web services")) {
            categories.add("REST API & Microservices");
        }
        
        // Check for Database
        if (containsAnyIgnoreCase(jobDescription.getTechnologies(), "sql", "database", "mysql", "postgresql", "oracle", 
                "mongodb", "nosql", "hibernate", "jpa")) {
            categories.add("Database & ORM");
        }
        
        // Check for Cloud/Containers
        if (containsAnyIgnoreCase(jobDescription.getTechnologies(), "cloud", "aws", "azure", "gcp", "docker", 
                "kubernetes", "container", "devops", "ci/cd")) {
            categories.add("Cloud & Containerization");
        }
        
        // Always include System Design and Coding Challenges
        categories.add("System Design & Architecture");
        categories.add("Coding Challenges");
        
        return categories;
    }
    
    /**
     * Get subcategories for a category based on job description.
     * 
     * @param category The category to get subcategories for
     * @param jobDescription The job description to analyze
     * @return A list of subcategories
     */
    private List<String> getSubcategoriesForCategory(String category, JobDescription jobDescription) {
        switch (category) {
            case "Core Java":
                return Arrays.asList("OOP", "Collections", "Multithreading", "Streams", "Exception Handling", "JVM");
            case "Spring & Spring Boot":
                return Arrays.asList("Spring Core", "Spring Boot", "Spring Security", "Spring Data", "Spring Cloud");
            case "REST API & Microservices":
                return Arrays.asList("REST Principles", "Microservices", "API Security", "API Gateway", "Service Discovery");
            case "Database & ORM":
                return Arrays.asList("SQL", "NoSQL", "Hibernate", "JPA", "Transaction Management", "Database Design");
            case "Cloud & Containerization":
                return Arrays.asList("Docker", "Kubernetes", "AWS/Azure/GCP", "CI/CD", "Infrastructure as Code");
            case "System Design & Architecture":
                return Arrays.asList("Scalability", "Caching", "Load Balancing", "Messaging", "Event-Driven Architecture");
            case "Coding Challenges":
                return Arrays.asList("Algorithms", "Data Structures", "Problem Solving", "Design Patterns");
            default:
                return Arrays.asList("General");
        }
    }
    
    /**
     * Check if a list of strings contains any of the specified keywords (case-insensitive).
     * 
     * @param list The list to check
     * @param keywords The keywords to look for
     * @return true if any keyword is found, false otherwise
     */
    private boolean containsAnyIgnoreCase(List<String> list, String... keywords) {
        for (String item : list) {
            String lowerItem = item.toLowerCase();
            for (String keyword : keywords) {
                if (lowerItem.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Shutdown the executor service.
     */
    public void shutdown() {
        executorService.shutdown();
    }
} 