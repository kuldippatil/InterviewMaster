package com.interview.pdfgenerator.parser;

import com.interview.pdfgenerator.model.JobDescription;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser implementation for JSON job description files.
 */
@Component
public class JsonJobDescriptionParser implements JobDescriptionParser {

    @Override
    public JobDescription parse(String filePath) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject json = new JSONObject(content);
        
        JobDescription jd = new JobDescription();
        jd.setDescription(content);
        
        // Extract title
        if (json.has("title") || json.has("jobTitle") || json.has("position")) {
            String title = json.optString("title", 
                    json.optString("jobTitle", 
                            json.optString("position", "Java Developer")));
            jd.setTitle(title);
        } else {
            jd.setTitle("Java Developer"); // Default title
        }
        
        // Extract company
        if (json.has("company") || json.has("organization") || json.has("employer")) {
            String company = json.optString("company", 
                    json.optString("organization", 
                            json.optString("employer", "Unknown Company")));
            jd.setCompany(company);
        } else {
            jd.setCompany("Unknown Company");
        }
        
        // Extract skills
        extractListFromJson(json, jd.getSkills(), "skills", "requirements", "qualifications");
        
        // Extract responsibilities
        extractListFromJson(json, jd.getResponsibilities(), "responsibilities", "duties", "jobDuties");
        
        // Extract technologies
        extractListFromJson(json, jd.getTechnologies(), "technologies", "techStack", "technicalEnvironment");
        
        return jd;
    }

    @Override
    public boolean canParse(String filePath) {
        return filePath.toLowerCase().endsWith(".json");
    }
    
    /**
     * Extract a list of items from a JSON object.
     * 
     * @param json The JSON object to extract from
     * @param resultList The list to add the extracted items to
     * @param keys The keys to look for in the JSON object
     */
    private void extractListFromJson(JSONObject json, List<String> resultList, String... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                try {
                    Object value = json.get(key);
                    
                    if (value instanceof JSONArray) {
                        JSONArray array = (JSONArray) value;
                        for (int i = 0; i < array.length(); i++) {
                            try {
                                Object item = array.get(i);
                                if (item instanceof String) {
                                    resultList.add((String) item);
                                } else if (item instanceof JSONObject) {
                                    JSONObject itemObj = (JSONObject) item;
                                    if (itemObj.has("name") || itemObj.has("value") || itemObj.has("description")) {
                                        String itemValue = itemObj.optString("name", 
                                                itemObj.optString("value", 
                                                        itemObj.optString("description", "")));
                                        if (!itemValue.isEmpty()) {
                                            resultList.add(itemValue);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                // Skip this item if there's an error
                                continue;
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
                } catch (JSONException e) {
                    // Skip this key if there's an error
                    continue;
                }
            }
        }
    }
} 