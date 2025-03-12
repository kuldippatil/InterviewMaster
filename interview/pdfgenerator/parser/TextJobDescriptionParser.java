package com.interview.pdfgenerator.parser;

import com.interview.pdfgenerator.model.JobDescription;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser implementation for plain text job description files.
 */
@Component
public class TextJobDescriptionParser implements JobDescriptionParser {

    private static final List<String> SKILL_KEYWORDS = Arrays.asList(
            "skills", "requirements", "qualifications", "technical skills", 
            "required skills", "technical requirements", "competencies"
    );
    
    private static final List<String> RESPONSIBILITY_KEYWORDS = Arrays.asList(
            "responsibilities", "duties", "job duties", "key responsibilities", 
            "what you'll do", "role responsibilities", "job responsibilities"
    );
    
    private static final List<String> TECHNOLOGY_KEYWORDS = Arrays.asList(
            "technologies", "tech stack", "technical environment", "tools", 
            "programming languages", "frameworks", "software", "platforms"
    );

    @Override
    public JobDescription parse(String filePath) throws Exception {
        JobDescription jd = new JobDescription();
        StringBuilder contentBuilder = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }
        
        String content = contentBuilder.toString();
        jd.setDescription(content);
        
        // Extract title
        Pattern titlePattern = Pattern.compile("(?i)(?:job title|position|role)\\s*:?\\s*([^\\n]+)");
        Matcher titleMatcher = titlePattern.matcher(content);
        if (titleMatcher.find()) {
            jd.setTitle(titleMatcher.group(1).trim());
        } else {
            // Try to find the first line as title
            String[] lines = content.split("\\n");
            if (lines.length > 0) {
                jd.setTitle(lines[0].trim());
            } else {
                jd.setTitle("Java Developer"); // Default title
            }
        }
        
        // Extract company
        Pattern companyPattern = Pattern.compile("(?i)(?:company|organization|employer)\\s*:?\\s*([^\\n]+)");
        Matcher companyMatcher = companyPattern.matcher(content);
        if (companyMatcher.find()) {
            jd.setCompany(companyMatcher.group(1).trim());
        } else {
            jd.setCompany("Unknown Company");
        }
        
        // Extract skills
        extractSection(content, SKILL_KEYWORDS, jd.getSkills());
        
        // Extract responsibilities
        extractSection(content, RESPONSIBILITY_KEYWORDS, jd.getResponsibilities());
        
        // Extract technologies
        extractSection(content, TECHNOLOGY_KEYWORDS, jd.getTechnologies());
        
        // If no technologies were found, try to extract them from skills
        if (jd.getTechnologies().isEmpty()) {
            extractTechnologiesFromSkills(jd);
        }
        
        return jd;
    }

    @Override
    public boolean canParse(String filePath) {
        return filePath.toLowerCase().endsWith(".txt");
    }
    
    /**
     * Extract a section from the content based on keywords.
     * 
     * @param content The full content of the job description
     * @param keywords List of keywords that might indicate the start of the section
     * @param resultList List to add the extracted items to
     */
    private void extractSection(String content, List<String> keywords, List<String> resultList) {
        for (String keyword : keywords) {
            Pattern pattern = Pattern.compile("(?i)" + keyword + "\\s*:?\\s*([^\\n]+(?:\\n(?!\\n|[A-Z][a-z]+\\s*:)[^\\n]+)*)");
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                String sectionContent = matcher.group(1).trim();
                
                // Check if the section contains bullet points
                if (sectionContent.contains("•") || sectionContent.contains("-") || sectionContent.contains("*")) {
                    // Split by bullet points
                    String[] items = sectionContent.split("(?:\\n\\s*(?:•|-|\\*|\\d+\\.))");
                    for (String item : items) {
                        String trimmed = item.trim();
                        if (!trimmed.isEmpty()) {
                            resultList.add(trimmed);
                        }
                    }
                } else {
                    // Split by sentences or commas
                    String[] items = sectionContent.split("(?:\\.|,)\\s+");
                    for (String item : items) {
                        String trimmed = item.trim();
                        if (!trimmed.isEmpty()) {
                            resultList.add(trimmed);
                        }
                    }
                }
                
                // If we found items, no need to check other keywords
                if (!resultList.isEmpty()) {
                    break;
                }
            }
        }
    }
    
    /**
     * Extract technologies from the skills list.
     * 
     * @param jd The job description to update
     */
    private void extractTechnologiesFromSkills(JobDescription jd) {
        List<String> techKeywords = Arrays.asList(
            "java", "spring", "spring boot", "hibernate", "jpa", "sql", "nosql", "mongodb", 
            "postgresql", "mysql", "oracle", "aws", "azure", "gcp", "docker", "kubernetes", 
            "microservices", "rest", "soap", "api", "git", "jenkins", "ci/cd", "junit", 
            "mockito", "maven", "gradle", "kafka", "rabbitmq", "redis", "elasticsearch"
        );
        
        for (String skill : jd.getSkills()) {
            for (String tech : techKeywords) {
                if (skill.toLowerCase().contains(tech)) {
                    jd.addTechnology(tech);
                    break;
                }
            }
        }
    }
} 