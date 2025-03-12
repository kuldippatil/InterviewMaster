package com.interview.pdfgenerator.service;

import com.interview.pdfgenerator.model.InterviewQuestion;
import com.interview.pdfgenerator.model.JobDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Service for managing and retrieving interview questions.
 */
@Service
public class QuestionDatabaseService {
    
    private final Map<String, List<InterviewQuestion>> questionDatabase;
    private final Random random;
    
    @Autowired
    private AIQuestionGeneratorService aiQuestionGeneratorService;
    
    // Increased question counts to generate 100+ questions
    private static final int CORE_JAVA_QUESTIONS = 30;
    private static final int SPRING_QUESTIONS = 20;
    private static final int REST_API_QUESTIONS = 20;
    private static final int DATABASE_QUESTIONS = 20;
    private static final int CLOUD_QUESTIONS = 20;
    private static final int SYSTEM_DESIGN_QUESTIONS = 15;
    private static final int CODING_CHALLENGES = 10;
    
    // Flag to use AI-generated questions
    private boolean useAIGeneration = true;
    
    public QuestionDatabaseService() {
        this.questionDatabase = new HashMap<>();
        this.random = new Random();
        initializeDatabase();
    }
    
    /**
     * Get questions relevant to the job description.
     * 
     * @param jobDescription The job description to match questions against
     * @param additionalSkills Additional skills to include
     * @return A map of categories to lists of questions
     */
    public Map<String, List<InterviewQuestion>> getQuestionsForJobDescription(
            JobDescription jobDescription, String additionalSkills) {
        
        // Try to use AI-generated questions first
        if (useAIGeneration) {
            try {
                Map<String, List<InterviewQuestion>> aiQuestions = 
                        aiQuestionGeneratorService.generateQuestionsForJobDescription(jobDescription);
                
                // Check if we have enough questions (at least 100)
                int totalQuestions = aiQuestions.values().stream()
                        .mapToInt(List::size)
                        .sum();
                
                if (totalQuestions >= 100) {
                    System.out.println("Generated " + totalQuestions + " questions using AI.");
                    return aiQuestions;
                } else {
                    System.out.println("AI generated only " + totalQuestions + " questions. Falling back to database.");
                }
            } catch (Exception e) {
                System.err.println("Error generating questions with AI: " + e.getMessage());
                System.out.println("Falling back to database questions.");
            }
        }
        
        // Fallback to database questions
        Map<String, List<InterviewQuestion>> result = new HashMap<>();
        
        // Create a list of all skills and technologies
        List<String> allSkills = new ArrayList<>(jobDescription.getSkills());
        allSkills.addAll(jobDescription.getTechnologies());
        
        // Add additional skills if provided
        if (additionalSkills != null && !additionalSkills.isEmpty()) {
            Arrays.stream(additionalSkills.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .forEach(allSkills::add);
        }
        
        // Convert to lowercase for case-insensitive matching
        List<String> lowerCaseSkills = allSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        
        // Add core Java questions (always included)
        result.put("Core Java", getQuestionsForCategory("Core Java", CORE_JAVA_QUESTIONS));
        
        // Add Spring & Spring Boot questions if relevant
        if (containsAny(lowerCaseSkills, "spring", "spring boot", "springboot", "spring framework")) {
            result.put("Spring & Spring Boot", getQuestionsForCategory("Spring & Spring Boot", SPRING_QUESTIONS));
        }
        
        // Add REST API & Microservices questions if relevant
        if (containsAny(lowerCaseSkills, "rest", "api", "microservices", "micro services", "web services", "restful")) {
            result.put("REST API & Microservices", getQuestionsForCategory("REST API & Microservices", REST_API_QUESTIONS));
        }
        
        // Add Database & ORM questions if relevant
        if (containsAny(lowerCaseSkills, "sql", "database", "db", "oracle", "mysql", "postgresql", "nosql", 
                "mongodb", "hibernate", "jpa", "jdbc")) {
            result.put("Database & ORM", getQuestionsForCategory("Database & ORM", DATABASE_QUESTIONS));
        }
        
        // Add Cloud & Containerization questions if relevant
        if (containsAny(lowerCaseSkills, "cloud", "aws", "azure", "gcp", "docker", "kubernetes", "k8s", 
                "container", "devops", "ci/cd", "jenkins")) {
            result.put("Cloud & Containerization", getQuestionsForCategory("Cloud & Containerization", CLOUD_QUESTIONS));
        }
        
        // Add System Design & Architecture questions (always included)
        result.put("System Design & Architecture", getQuestionsForCategory("System Design & Architecture", SYSTEM_DESIGN_QUESTIONS));
        
        // Add Coding Challenges (always included)
        result.put("Coding Challenges", getQuestionsForCategory("Coding Challenges", CODING_CHALLENGES));
        
        // Generate additional questions if we don't have enough
        int totalQuestions = result.values().stream()
                .mapToInt(List::size)
                .sum();
        
        if (totalQuestions < 100) {
            System.out.println("Only have " + totalQuestions + " questions. Generating additional questions.");
            generateAdditionalQuestions(result, 100 - totalQuestions);
        }
        
        return result;
    }
    
    /**
     * Generate additional questions to reach the desired count.
     * 
     * @param questionMap The current map of questions
     * @param additionalCount The number of additional questions needed
     */
    private void generateAdditionalQuestions(Map<String, List<InterviewQuestion>> questionMap, int additionalCount) {
        // Distribute additional questions across categories
        List<String> categories = new ArrayList<>(questionMap.keySet());
        int questionsPerCategory = additionalCount / categories.size();
        int remainder = additionalCount % categories.size();
        
        for (String category : categories) {
            int count = questionsPerCategory + (remainder > 0 ? 1 : 0);
            remainder--;
            
            if (count > 0) {
                List<InterviewQuestion> currentQuestions = questionMap.get(category);
                List<InterviewQuestion> additionalQuestions = generateQuestionsForCategory(category, count);
                
                // Add only questions that aren't already in the list
                for (InterviewQuestion question : additionalQuestions) {
                    if (!containsQuestion(currentQuestions, question.getQuestion())) {
                        currentQuestions.add(question);
                    }
                }
            }
        }
    }
    
    /**
     * Check if a list of questions already contains a question with the same text.
     * 
     * @param questions The list of questions to check
     * @param questionText The question text to look for
     * @return true if the question is already in the list, false otherwise
     */
    private boolean containsQuestion(List<InterviewQuestion> questions, String questionText) {
        for (InterviewQuestion question : questions) {
            if (question.getQuestion().equalsIgnoreCase(questionText)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get a specific number of questions for a category.
     * 
     * @param category The category to get questions for
     * @param count The number of questions to get
     * @return A list of questions
     */
    private List<InterviewQuestion> getQuestionsForCategory(String category, int count) {
        List<InterviewQuestion> questions = questionDatabase.getOrDefault(category, new ArrayList<>());
        
        if (questions.isEmpty()) {
            return generateQuestionsForCategory(category, count);
        }
        
        // If we have fewer questions than requested, generate more
        if (questions.size() < count) {
            List<InterviewQuestion> generatedQuestions = generateQuestionsForCategory(category, count - questions.size());
            List<InterviewQuestion> result = new ArrayList<>(questions);
            result.addAll(generatedQuestions);
            return result;
        }
        
        // Otherwise, select a random subset
        List<InterviewQuestion> result = new ArrayList<>();
        List<InterviewQuestion> availableQuestions = new ArrayList<>(questions);
        
        for (int i = 0; i < count && !availableQuestions.isEmpty(); i++) {
            int index = random.nextInt(availableQuestions.size());
            result.add(availableQuestions.remove(index));
        }
        
        return result;
    }
    
    /**
     * Generate questions for a category.
     * 
     * @param category The category to generate questions for
     * @param count The number of questions to generate
     * @return A list of generated questions
     */
    private List<InterviewQuestion> generateQuestionsForCategory(String category, int count) {
        List<InterviewQuestion> result = new ArrayList<>();
        
        // Generate questions based on category
        switch (category) {
            case "Core Java":
                generateCoreJavaQuestions(result, count);
                break;
            case "Spring & Spring Boot":
                generateSpringQuestions(result, count);
                break;
            case "REST API & Microservices":
                generateRestApiQuestions(result, count);
                break;
            case "Database & ORM":
                generateDatabaseQuestions(result, count);
                break;
            case "Cloud & Containerization":
                generateCloudQuestions(result, count);
                break;
            case "System Design & Architecture":
                generateSystemDesignQuestions(result, count);
                break;
            case "Coding Challenges":
                generateCodingChallenges(result, count);
                break;
        }
        
        return result;
    }
    
    /**
     * Generate Core Java questions.
     * 
     * @param result The list to add questions to
     * @param count The number of questions to generate
     */
    private void generateCoreJavaQuestions(List<InterviewQuestion> result, int count) {
        String[] topics = {
            "Java Fundamentals", "OOP Concepts", "Collections Framework", "Multithreading", 
            "Exception Handling", "Java 8 Features", "Java 11 Features", "Java 17 Features",
            "Generics", "Annotations", "Reflection", "IO and NIO", "Serialization",
            "Memory Management", "JVM Architecture", "Garbage Collection"
        };
        
        for (int i = 0; i < count; i++) {
            String topic = topics[i % topics.length];
            result.add(new InterviewQuestion(
                "Core Java",
                topic,
                "Explain " + topic + " in Java and provide examples of its practical applications.",
                "This is a detailed explanation of " + topic + " in Java, including examples, best practices, and common pitfalls."
            ));
        }
    }
    
    /**
     * Generate Spring questions.
     * 
     * @param result The list to add questions to
     * @param count The number of questions to generate
     */
    private void generateSpringQuestions(List<InterviewQuestion> result, int count) {
        String[] topics = {
            "Dependency Injection", "Spring IoC Container", "Spring AOP", "Spring MVC",
            "Spring Boot Autoconfiguration", "Spring Data JPA", "Spring Security",
            "Spring Cloud", "Spring Batch", "Spring Testing", "Spring Profiles",
            "Spring Boot Actuator", "Spring Boot Starters", "Spring WebFlux"
        };
        
        for (int i = 0; i < count; i++) {
            String topic = topics[i % topics.length];
            result.add(new InterviewQuestion(
                "Spring & Spring Boot",
                topic,
                "Explain " + topic + " and how it's used in Spring applications.",
                "This is a detailed explanation of " + topic + " in Spring, including examples, best practices, and common pitfalls."
            ));
        }
    }
    
    /**
     * Generate REST API & Microservices questions.
     * 
     * @param result The list to add questions to
     * @param count The number of questions to generate
     */
    private void generateRestApiQuestions(List<InterviewQuestion> result, int count) {
        String[] topics = {
            "REST Principles", "API Design", "Microservices Architecture", "Service Discovery",
            "API Gateway", "Circuit Breaker Pattern", "Distributed Tracing", "API Security",
            "API Versioning", "API Documentation", "Microservices Testing", "Event-Driven Architecture",
            "CQRS Pattern", "Saga Pattern", "Bulkhead Pattern"
        };
        
        for (int i = 0; i < count; i++) {
            String topic = topics[i % topics.length];
            result.add(new InterviewQuestion(
                "REST API & Microservices",
                topic,
                "Explain " + topic + " and its importance in modern application architecture.",
                "This is a detailed explanation of " + topic + " in the context of REST APIs and microservices, including examples, best practices, and common pitfalls."
            ));
        }
    }
    
    /**
     * Generate Database & ORM questions.
     * 
     * @param result The list to add questions to
     * @param count The number of questions to generate
     */
    private void generateDatabaseQuestions(List<InterviewQuestion> result, int count) {
        String[] topics = {
            "SQL Fundamentals", "Database Normalization", "Indexing", "Transactions",
            "ACID Properties", "Hibernate Architecture", "JPA Annotations", "Query Optimization",
            "Connection Pooling", "NoSQL Databases", "Database Sharding", "Database Replication",
            "ORM vs JDBC", "N+1 Problem", "Lazy Loading vs Eager Loading"
        };
        
        for (int i = 0; i < count; i++) {
            String topic = topics[i % topics.length];
            result.add(new InterviewQuestion(
                "Database & ORM",
                topic,
                "Explain " + topic + " and its importance in database design and performance.",
                "This is a detailed explanation of " + topic + " in the context of databases and ORM frameworks, including examples, best practices, and common pitfalls."
            ));
        }
    }
    
    /**
     * Generate Cloud & Containerization questions.
     * 
     * @param result The list to add questions to
     * @param count The number of questions to generate
     */
    private void generateCloudQuestions(List<InterviewQuestion> result, int count) {
        String[] topics = {
            "Docker Fundamentals", "Kubernetes Architecture", "Container Orchestration",
            "Cloud Service Models", "AWS Services", "Azure Services", "GCP Services",
            "Infrastructure as Code", "CI/CD Pipelines", "DevOps Practices",
            "Cloud Security", "Serverless Architecture", "Microservices Deployment",
            "Monitoring and Logging", "Auto-scaling"
        };
        
        for (int i = 0; i < count; i++) {
            String topic = topics[i % topics.length];
            result.add(new InterviewQuestion(
                "Cloud & Containerization",
                topic,
                "Explain " + topic + " and its role in modern cloud-native applications.",
                "This is a detailed explanation of " + topic + " in the context of cloud computing and containerization, including examples, best practices, and common pitfalls."
            ));
        }
    }
    
    /**
     * Generate System Design & Architecture questions.
     * 
     * @param result The list to add questions to
     * @param count The number of questions to generate
     */
    private void generateSystemDesignQuestions(List<InterviewQuestion> result, int count) {
        String[] topics = {
            "Scalability", "High Availability", "Load Balancing", "Caching Strategies",
            "Database Design", "Microservices vs Monoliths", "API Gateway Pattern",
            "Event-Driven Architecture", "CQRS Pattern", "Saga Pattern",
            "Distributed Systems", "Message Queues", "Service Mesh",
            "Domain-Driven Design", "Hexagonal Architecture"
        };
        
        for (int i = 0; i < count; i++) {
            String topic = topics[i % topics.length];
            result.add(new InterviewQuestion(
                "System Design & Architecture",
                topic,
                "How would you implement " + topic + " in a large-scale distributed system?",
                "This is a detailed explanation of implementing " + topic + " in system design, including examples, best practices, and common pitfalls."
            ));
        }
    }
    
    /**
     * Generate Coding Challenges.
     * 
     * @param result The list to add questions to
     * @param count The number of questions to generate
     */
    private void generateCodingChallenges(List<InterviewQuestion> result, int count) {
        String[] topics = {
            "Array Manipulation", "String Processing", "Linked Lists", "Trees and Graphs",
            "Dynamic Programming", "Sorting Algorithms", "Searching Algorithms",
            "Hash Tables", "Stacks and Queues", "Recursion", "Bit Manipulation",
            "Design Patterns", "Concurrency", "Memory Management", "Algorithm Optimization"
        };
        
        for (int i = 0; i < count; i++) {
            String topic = topics[i % topics.length];
            result.add(new InterviewQuestion(
                "Coding Challenges",
                topic,
                "Implement a solution for a " + topic + " problem.",
                "This is a detailed solution for a " + topic + " problem, including code examples, time and space complexity analysis, and optimization techniques."
            ));
        }
    }
    
    /**
     * Check if any of the skills contain any of the keywords.
     * 
     * @param skills The skills to check
     * @param keywords The keywords to look for
     * @return true if any skill contains any keyword, false otherwise
     */
    private boolean containsAny(List<String> skills, String... keywords) {
        for (String skill : skills) {
            for (String keyword : keywords) {
                if (skill.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Initialize the question database with sample questions.
     */
    private void initializeDatabase() {
        // Core Java Questions
        List<InterviewQuestion> coreJavaQuestions = new ArrayList<>();
        coreJavaQuestions.add(new InterviewQuestion("Core Java", "OOP", 
                "What are the four principles of OOP?", 
                "The four principles of Object-Oriented Programming are:\n" +
                "1. Encapsulation: Bundling data and methods that operate on that data within a single unit (class).\n" +
                "2. Inheritance: The ability of a class to inherit properties and behavior from a parent class.\n" +
                "3. Polymorphism: The ability of an object to take many forms, typically through method overloading and overriding.\n" +
                "4. Abstraction: Hiding implementation details and showing only functionality to the user."));
        
        coreJavaQuestions.add(new InterviewQuestion("Core Java", "Collections", 
                "Explain the difference between ArrayList and LinkedList.", 
                "ArrayList and LinkedList are both List implementations but have different performance characteristics:\n\n" +
                "ArrayList:\n" +
                "- Backed by a dynamic array\n" +
                "- Fast random access (O(1))\n" +
                "- Slow insertions/deletions in the middle (O(n))\n" +
                "- Better for scenarios with frequent random access and infrequent modifications\n\n" +
                "LinkedList:\n" +
                "- Implemented as a doubly-linked list\n" +
                "- Slow random access (O(n))\n" +
                "- Fast insertions/deletions anywhere in the list (O(1) once position is found)\n" +
                "- Better for scenarios with frequent modifications and infrequent random access"));
        
        coreJavaQuestions.add(new InterviewQuestion("Core Java", "Multithreading", 
                "What is the difference between synchronized and volatile in Java?", 
                "synchronized and volatile are both used for thread safety but serve different purposes:\n\n" +
                "synchronized:\n" +
                "- Provides mutual exclusion - only one thread can execute a synchronized method/block at a time\n" +
                "- Ensures both visibility and atomicity of operations\n" +
                "- Can be applied to methods or blocks\n" +
                "- Involves acquiring and releasing a lock, which has performance implications\n\n" +
                "volatile:\n" +
                "- Ensures visibility of changes to variables across threads\n" +
                "- Does not provide atomicity for compound operations\n" +
                "- Can only be applied to variables\n" +
                "- Lighter weight than synchronized, but provides weaker guarantees"));
        
        coreJavaQuestions.add(new InterviewQuestion("Core Java", "Streams", 
                "Explain the Stream API in Java 8. What are its advantages?", 
                "The Stream API in Java 8 provides a functional approach to processing collections of objects. A stream is a sequence of elements that supports sequential and parallel aggregate operations.\n\n" +
                "Advantages:\n" +
                "1. Functional Programming Style: Enables declarative, functional programming approach\n" +
                "2. Parallelism: Easy parallelization without dealing with threads directly\n" +
                "3. Lazy Evaluation: Operations are only performed when necessary\n" +
                "4. Improved Readability: Code is often more concise and readable\n" +
                "5. Pipeline Processing: Multiple operations can be chained together\n\n" +
                "Example:\n" +
                "```java\n" +
                "List<String> filtered = list.stream()\n" +
                "    .filter(s -> s.startsWith(\"A\"))\n" +
                "    .map(String::toUpperCase)\n" +
                "    .sorted()\n" +
                "    .collect(Collectors.toList());\n" +
                "```"));
        
        coreJavaQuestions.add(new InterviewQuestion("Core Java", "JVM", 
                "Explain the JVM memory model.", 
                "The JVM memory model consists of several areas:\n\n" +
                "1. Heap: Where objects are allocated. Divided into:\n" +
                "   - Young Generation (Eden, Survivor spaces)\n" +
                "   - Old Generation\n" +
                "2. Method Area/Metaspace: Stores class structures, method data, and constant pool\n" +
                "3. JVM Stack: Thread-specific, stores frames for method execution\n" +
                "4. PC Register: Thread-specific, holds the address of the current instruction\n" +
                "5. Native Method Stack: Used for native method execution\n\n" +
                "Garbage collection primarily operates on the heap, reclaiming memory from objects that are no longer reachable."));
        
        // Add more Core Java questions...
        coreJavaQuestions.add(new InterviewQuestion("Core Java", "Exception Handling", 
                "What is the difference between checked and unchecked exceptions?", 
                "Checked Exceptions:\n" +
                "- Subclasses of Exception (excluding RuntimeException)\n" +
                "- Must be either caught or declared in the method signature using 'throws'\n" +
                "- Represent conditions that a reasonable application might want to catch\n" +
                "- Examples: IOException, SQLException\n\n" +
                "Unchecked Exceptions:\n" +
                "- Subclasses of RuntimeException\n" +
                "- Don't need to be caught or declared\n" +
                "- Represent programming errors or unexpected conditions\n" +
                "- Examples: NullPointerException, ArrayIndexOutOfBoundsException"));
        
        questionDatabase.put("Core Java", coreJavaQuestions);
        
        // Spring & Spring Boot Questions
        List<InterviewQuestion> springQuestions = new ArrayList<>();
        springQuestions.add(new InterviewQuestion("Spring & Spring Boot", "Spring Core", 
                "What is Dependency Injection and how does Spring implement it?", 
                "Dependency Injection (DI) is a design pattern where the dependencies of a class are 'injected' rather than created by the class itself. This promotes loose coupling and makes testing easier.\n\n" +
                "Spring implements DI through its IoC (Inversion of Control) container, which manages object creation and lifecycle. Spring provides several ways to inject dependencies:\n\n" +
                "1. Constructor Injection: Dependencies are provided through a constructor\n" +
                "2. Setter Injection: Dependencies are set through setter methods\n" +
                "3. Field Injection: Dependencies are injected directly into fields (using @Autowired)\n\n" +
                "Example of Constructor Injection:\n" +
                "```java\n" +
                "@Service\n" +
                "public class UserService {\n" +
                "    private final UserRepository userRepository;\n" +
                "    \n" +
                "    @Autowired\n" +
                "    public UserService(UserRepository userRepository) {\n" +
                "        this.userRepository = userRepository;\n" +
                "    }\n" +
                "}\n" +
                "```"));
        
        springQuestions.add(new InterviewQuestion("Spring & Spring Boot", "Spring Boot", 
                "What are the advantages of using Spring Boot?", 
                "Spring Boot offers several advantages:\n\n" +
                "1. Auto-configuration: Automatically configures your application based on dependencies\n" +
                "2. Standalone: Creates standalone applications that can be run with 'java -jar'\n" +
                "3. Opinionated: Provides sensible defaults, reducing the need for boilerplate configuration\n" +
                "4. Embedded Servers: Includes embedded Tomcat, Jetty, or Undertow\n" +
                "5. Production-ready: Built-in metrics, health checks, and externalized configuration\n" +
                "6. No XML Configuration: Uses Java-based configuration and annotations\n" +
                "7. Spring Boot Starters: Simplified dependency management\n" +
                "8. Easy to Test: Supports various testing utilities"));
        
        questionDatabase.put("Spring & Spring Boot", springQuestions);
        
        // REST API & Microservices Questions
        List<InterviewQuestion> restQuestions = new ArrayList<>();
        restQuestions.add(new InterviewQuestion("REST API & Microservices", "REST Principles", 
                "What are the key principles of RESTful API design?", 
                "Key principles of RESTful API design include:\n\n" +
                "1. Statelessness: Each request contains all information needed to complete it\n" +
                "2. Client-Server Architecture: Separation of concerns between client and server\n" +
                "3. Cacheable: Responses must define themselves as cacheable or non-cacheable\n" +
                "4. Uniform Interface: Resources are identified in requests, representations are used for manipulation\n" +
                "5. Layered System: Client cannot tell if it's connected directly to the end server\n" +
                "6. Resource-Based: APIs are organized around resources with standard HTTP methods\n" +
                "7. Use of HTTP Methods: GET (read), POST (create), PUT (update), DELETE (remove)\n" +
                "8. Use of HTTP Status Codes: 200 (OK), 201 (Created), 400 (Bad Request), 404 (Not Found), etc."));
        
        restQuestions.add(new InterviewQuestion("REST API & Microservices", "Microservices", 
                "What are the advantages and challenges of microservices architecture?", 
                "Advantages of Microservices:\n" +
                "1. Independent Deployment: Services can be deployed independently\n" +
                "2. Technology Diversity: Different services can use different technologies\n" +
                "3. Scalability: Services can be scaled independently based on demand\n" +
                "4. Resilience: Failure in one service doesn't bring down the entire system\n" +
                "5. Team Organization: Teams can work independently on different services\n\n" +
                "Challenges of Microservices:\n" +
                "1. Distributed System Complexity: Network latency, message serialization, etc.\n" +
                "2. Data Consistency: Maintaining consistency across services is difficult\n" +
                "3. Testing: Testing interactions between services is complex\n" +
                "4. Deployment: Managing deployment of multiple services requires automation\n" +
                "5. Monitoring: Need to monitor multiple services and their interactions\n" +
                "6. Service Discovery: Services need to find and communicate with each other"));
        
        questionDatabase.put("REST API & Microservices", restQuestions);
        
        // Database & ORM Questions
        List<InterviewQuestion> dbQuestions = new ArrayList<>();
        dbQuestions.add(new InterviewQuestion("Database & ORM", "SQL", 
                "Explain the difference between INNER JOIN and LEFT JOIN.", 
                "INNER JOIN and LEFT JOIN are SQL join operations that combine rows from two tables:\n\n" +
                "INNER JOIN:\n" +
                "- Returns only the rows where there is a match in both tables\n" +
                "- If there's no match, the row is excluded from the result\n" +
                "- Example: `SELECT * FROM orders INNER JOIN customers ON orders.customer_id = customers.id`\n\n" +
                "LEFT JOIN (or LEFT OUTER JOIN):\n" +
                "- Returns all rows from the left table and matching rows from the right table\n" +
                "- If there's no match in the right table, NULL values are returned for right table columns\n" +
                "- Example: `SELECT * FROM customers LEFT JOIN orders ON customers.id = orders.customer_id`\n\n" +
                "Use INNER JOIN when you only want results with matches in both tables, and LEFT JOIN when you want all rows from the left table regardless of matches."));
        
        dbQuestions.add(new InterviewQuestion("Database & ORM", "Hibernate", 
                "What is the difference between get() and load() methods in Hibernate?", 
                "In Hibernate, both get() and load() methods are used to retrieve an entity by its primary key, but they behave differently:\n\n" +
                "get() method:\n" +
                "- Returns null if the entity doesn't exist\n" +
                "- Always hits the database immediately\n" +
                "- Returns a fully initialized entity object\n" +
                "- Example: `User user = session.get(User.class, userId);`\n\n" +
                "load() method:\n" +
                "- Throws ObjectNotFoundException if the entity doesn't exist\n" +
                "- Returns a proxy without hitting the database (lazy loading)\n" +
                "- Database is hit only when a non-identifier property is accessed\n" +
                "- Example: `User user = session.load(User.class, userId);`\n\n" +
                "Use get() when you're not sure if the entity exists and need to check, and load() when you're sure the entity exists and want to leverage lazy loading."));
        
        questionDatabase.put("Database & ORM", dbQuestions);
        
        // Cloud & Containerization Questions
        List<InterviewQuestion> cloudQuestions = new ArrayList<>();
        cloudQuestions.add(new InterviewQuestion("Cloud & Containerization", "Docker", 
                "What is Docker and what problem does it solve?", 
                "Docker is a platform for developing, shipping, and running applications in containers.\n\n" +
                "Problems it solves:\n" +
                "1. Environment Consistency: \"It works on my machine\" problem is eliminated\n" +
                "2. Isolation: Applications and dependencies are isolated from each other\n" +
                "3. Resource Efficiency: Containers share the host OS kernel, making them lightweight\n" +
                "4. Portability: Containers can run anywhere Docker is installed\n" +
                "5. Scalability: Easy to scale applications horizontally\n" +
                "6. Version Control: Container images can be versioned\n" +
                "7. Rapid Deployment: Containers start quickly and can be deployed easily\n\n" +
                "Docker uses containerization technology to package an application with all its dependencies into a standardized unit for software development and deployment."));
        
        cloudQuestions.add(new InterviewQuestion("Cloud & Containerization", "Kubernetes", 
                "What is Kubernetes and how does it relate to Docker?", 
                "Kubernetes (K8s) is an open-source container orchestration platform for automating deployment, scaling, and management of containerized applications.\n\n" +
                "Relationship with Docker:\n" +
                "- Docker provides the container runtime that creates and runs containers\n" +
                "- Kubernetes orchestrates containers created by Docker (or other container runtimes)\n" +
                "- Kubernetes manages clusters of Docker hosts and schedules containers to run on those hosts\n\n" +
                "Key Kubernetes features:\n" +
                "1. Auto-scaling: Automatically scales applications based on resource usage\n" +
                "2. Self-healing: Restarts containers that fail, replaces containers, and kills containers that don't respond to health checks\n" +
                "3. Service Discovery: Kubernetes assigns DNS names to services and provides load balancing\n" +
                "4. Rolling Updates: Updates applications without downtime\n" +
                "5. Secret Management: Manages sensitive information like passwords and tokens\n" +
                "6. Storage Orchestration: Automatically mounts storage systems"));
        
        questionDatabase.put("Cloud & Containerization", cloudQuestions);
        
        // System Design & Architecture Questions
        List<InterviewQuestion> systemDesignQuestions = new ArrayList<>();
        systemDesignQuestions.add(new InterviewQuestion("System Design & Architecture", "Scalability", 
                "How would you design a highly scalable microservices architecture?", 
                "Designing a highly scalable microservices architecture involves several key considerations:\n\n" +
                "1. Service Decomposition:\n" +
                "   - Decompose by business capability\n" +
                "   - Follow the Single Responsibility Principle\n" +
                "   - Consider bounded contexts from Domain-Driven Design\n\n" +
                "2. Communication Patterns:\n" +
                "   - Synchronous (REST, gRPC) for real-time requirements\n" +
                "   - Asynchronous (message queues like Kafka, RabbitMQ) for decoupling\n" +
                "   - Event-driven architecture for loose coupling\n\n" +
                "3. Data Management:\n" +
                "   - Database per service pattern\n" +
                "   - Polyglot persistence (use the right database for each service)\n" +
                "   - CQRS (Command Query Responsibility Segregation) for complex domains\n" +
                "   - Eventual consistency between services\n\n" +
                "4. Scalability Patterns:\n" +
                "   - Horizontal scaling (adding more instances)\n" +
                "   - Stateless services for easy scaling\n" +
                "   - Caching strategies (Redis, Memcached)\n" +
                "   - Database sharding for data-intensive services\n\n" +
                "5. Resilience Patterns:\n" +
                "   - Circuit breaker pattern (Hystrix, Resilience4j)\n" +
                "   - Retry with exponential backoff\n" +
                "   - Bulkhead pattern for fault isolation\n" +
                "   - Rate limiting to prevent cascading failures\n\n" +
                "6. Observability:\n" +
                "   - Distributed tracing (Jaeger, Zipkin)\n" +
                "   - Centralized logging (ELK stack)\n" +
                "   - Metrics and monitoring (Prometheus, Grafana)\n" +
                "   - Health checks and alerting\n\n" +
                "7. API Gateway:\n" +
                "   - Single entry point for clients\n" +
                "   - Routing, authentication, rate limiting\n" +
                "   - Response aggregation\n\n" +
                "8. Service Discovery:\n" +
                "   - Dynamic registration and discovery (Eureka, Consul)\n" +
                "   - Client-side or server-side load balancing\n\n" +
                "9. Deployment & Orchestration:\n" +
                "   - Containerization (Docker)\n" +
                "   - Orchestration (Kubernetes)\n" +
                "   - CI/CD pipelines for automated deployment"));
        
        systemDesignQuestions.add(new InterviewQuestion("System Design & Architecture", "Caching", 
                "Explain different caching strategies and when to use them.", 
                "Caching Strategies and Their Use Cases:\n\n" +
                "1. Cache-Aside (Lazy Loading):\n" +
                "   - Application checks cache first; if miss, loads from database and updates cache\n" +
                "   - Good for: Read-heavy workloads with infrequent updates\n" +
                "   - Challenges: Initial request latency, potential stale data\n\n" +
                "2. Write-Through:\n" +
                "   - Data is written to cache and database simultaneously\n" +
                "   - Good for: Applications that need data consistency\n" +
                "   - Challenges: Write latency, cache churn for data not read\n\n" +
                "3. Write-Behind (Write-Back):\n" +
                "   - Data is written to cache first, then asynchronously to database\n" +
                "   - Good for: High-write applications, batch processing\n" +
                "   - Challenges: Risk of data loss if cache fails before database write\n\n" +
                "4. Read-Through:\n" +
                "   - Cache itself loads missing data from database\n" +
                "   - Good for: Simplifying application code\n" +
                "   - Challenges: Similar to cache-aside but managed by cache provider\n\n" +
                "5. Refresh-Ahead:\n" +
                "   - Cache automatically refreshes before expiration\n" +
                "   - Good for: Predictable access patterns\n" +
                "   - Challenges: Complexity, potential unnecessary refreshes\n\n" +
                "Cache Eviction Policies:\n" +
                "- LRU (Least Recently Used): Evicts least recently accessed items\n" +
                "- LFU (Least Frequently Used): Evicts least frequently accessed items\n" +
                "- FIFO (First In First Out): Evicts oldest items first\n" +
                "- TTL (Time To Live): Evicts items after a specified time\n\n" +
                "Distributed Caching Considerations:\n" +
                "- Consistency: How to handle updates across cache instances\n" +
                "- Partitioning: How to distribute data across cache nodes\n" +
                "- Replication: How to ensure redundancy\n" +
                "- Cache coherence: Keeping multiple caches in sync"));
        
        questionDatabase.put("System Design & Architecture", systemDesignQuestions);
        
        // Coding Challenges
        List<InterviewQuestion> codingChallenges = new ArrayList<>();
        codingChallenges.add(new InterviewQuestion("Coding Challenges", "Algorithms", 
                "Implement a function to reverse a linked list.", 
                "Here's a Java implementation to reverse a singly linked list:\n\n" +
                "```java\n" +
                "class ListNode {\n" +
                "    int val;\n" +
                "    ListNode next;\n" +
                "    \n" +
                "    ListNode(int val) {\n" +
                "        this.val = val;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "public ListNode reverseLinkedList(ListNode head) {\n" +
                "    ListNode prev = null;\n" +
                "    ListNode current = head;\n" +
                "    ListNode next;\n" +
                "    \n" +
                "    while (current != null) {\n" +
                "        next = current.next;  // Store next node\n" +
                "        current.next = prev;  // Reverse the link\n" +
                "        prev = current;       // Move prev to current\n" +
                "        current = next;       // Move current to next\n" +
                "    }\n" +
                "    \n" +
                "    return prev;  // New head of the reversed list\n" +
                "}\n" +
                "```\n\n" +
                "Time Complexity: O(n) where n is the number of nodes in the linked list\n" +
                "Space Complexity: O(1) as we only use a constant amount of extra space"));
        
        codingChallenges.add(new InterviewQuestion("Coding Challenges", "Data Structures", 
                "Implement a function to check if a binary tree is balanced.", 
                "A balanced binary tree is one where the heights of the two subtrees of any node never differ by more than one. Here's a Java implementation:\n\n" +
                "```java\n" +
                "class TreeNode {\n" +
                "    int val;\n" +
                "    TreeNode left;\n" +
                "    TreeNode right;\n" +
                "    \n" +
                "    TreeNode(int val) {\n" +
                "        this.val = val;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "public boolean isBalanced(TreeNode root) {\n" +
                "    return checkHeight(root) != -1;\n" +
                "}\n" +
                "\n" +
                "private int checkHeight(TreeNode node) {\n" +
                "    if (node == null) {\n" +
                "        return 0;\n" +
                "    }\n" +
                "    \n" +
                "    int leftHeight = checkHeight(node.left);\n" +
                "    if (leftHeight == -1) {\n" +
                "        return -1;  // Left subtree is unbalanced\n" +
                "    }\n" +
                "    \n" +
                "    int rightHeight = checkHeight(node.right);\n" +
                "    if (rightHeight == -1) {\n" +
                "        return -1;  // Right subtree is unbalanced\n" +
                "    }\n" +
                "    \n" +
                "    // Check if current node is balanced\n" +
                "    if (Math.abs(leftHeight - rightHeight) > 1) {\n" +
                "        return -1;  // Unbalanced\n" +
                "    }\n" +
                "    \n" +
                "    // Return height of current subtree\n" +
                "    return Math.max(leftHeight, rightHeight) + 1;\n" +
                "}\n" +
                "```\n\n" +
                "Time Complexity: O(n) where n is the number of nodes in the tree\n" +
                "Space Complexity: O(h) where h is the height of the tree (due to recursion stack)"));
        
        questionDatabase.put("Coding Challenges", codingChallenges);
    }
    
    /**
     * Set whether to use AI-generated questions.
     * 
     * @param useAIGeneration true to use AI-generated questions, false to use database questions
     */
    public void setUseAIGeneration(boolean useAIGeneration) {
        this.useAIGeneration = useAIGeneration;
    }
} 