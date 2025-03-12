package com.interview.pdfgenerator.parser;

import com.interview.pdfgenerator.model.JobDescription;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parser implementation for YAML job description files.
 */
@Component
public class YamlJobDescriptionParser implements JobDescriptionParser {

    @Override
    public JobDescription parse(String filePath) throws Exception {
        JobDescription jd = new JobDescription();
        
        try (InputStream input = new FileInputStream(filePath)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(input);
            
            // Store the raw content
            jd.setDescription(data.toString());
            
            // Extract title
            if (data.containsKey("title") || data.containsKey("jobTitle") || data.containsKey("position")) {
                String title = getStringValue(data, "title", 
                        getStringValue(data, "jobTitle", 
                                getStringValue(data, "position", "Java Developer")));
                jd.setTitle(title);
            } else {
                jd.setTitle("Java Developer"); // Default title
            }
            
            // Extract company
            if (data.containsKey("company") || data.containsKey("organization") || data.containsKey("employer")) {
                String company = getStringValue(data, "company", 
                        getStringValue(data, "organization", 
                                getStringValue(data, "employer", "Unknown Company")));
                jd.setCompany(company);
            } else {
                jd.setCompany("Unknown Company");
            }
            
            // Extract skills
            extractListFromYaml(data, jd.getSkills(), "skills", "requirements", "qualifications");
            
            // Extract responsibilities
            extractListFromYaml(data, jd.getResponsibilities(), "responsibilities", "duties", "jobDuties");
            
            // Extract technologies
            extractListFromYaml(data, jd.getTechnologies(), "technologies", "techStack", "technicalEnvironment");
        }
        
        return jd;
    }

    @Override
    public boolean canParse(String filePath) {
        return filePath.toLowerCase().endsWith(".yml") || filePath.toLowerCase().endsWith(".yaml");
    }
    
    /**
     * Get a string value from a map with a default value.
     * 
     * @param data The map to get the value from
     * @param key The key to look for
     * @param defaultValue The default value to return if the key is not found or the value is not a string
     * @return The string value or the default value
     */
    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        if (data.containsKey(key) && data.get(key) instanceof String) {
            return (String) data.get(key);
        }
        return defaultValue;
    }
    
    /**
     * Extract a list of items from a YAML map.
     * 
     * @param data The map to extract from
     * @param resultList The list to add the extracted items to
     * @param keys The keys to look for in the map
     */
    private void extractListFromYaml(Map<String, Object> data, List<String> resultList, String... keys) {
        for (String key : keys) {
            if (data.containsKey(key)) {
                Object value = data.get(key);
                
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        if (item instanceof String) {
                            resultList.add((String) item);
                        } else if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> itemMap = (Map<String, Object>) item;
                            if (itemMap.containsKey("name") || itemMap.containsKey("value") || itemMap.containsKey("description")) {
                                String itemValue = getStringValue(itemMap, "name", 
                                        getStringValue(itemMap, "value", 
                                                getStringValue(itemMap, "description", "")));
                                if (!itemValue.isEmpty()) {
                                    resultList.add(itemValue);
                                }
                            }
                        }
                    }
                } else if (value instanceof String) {
                    // Split by commas, newlines, or semicolons
                    String[] items = ((String) value).split("[,;\n]+");
                    for (String item : items) {
                        String trimmed = item.trim();
                        if (!trimmed.isEmpty()) {
                            resultList.add(trimmed);
                        }
                    }
                }
                
                // If we found items, no need to check other keys
                if (!resultList.isEmpty()) {
                    break;
                }
            }
        }
    }
} 