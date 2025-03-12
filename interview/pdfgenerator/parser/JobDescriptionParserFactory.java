package com.interview.pdfgenerator.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory class for selecting the appropriate job description parser.
 */
@Component
public class JobDescriptionParserFactory {
    
    private final List<JobDescriptionParser> parsers;
    
    @Autowired
    public JobDescriptionParserFactory(List<JobDescriptionParser> parsers) {
        this.parsers = parsers;
    }
    
    /**
     * Get the appropriate parser for the given file path.
     * 
     * @param filePath Path to the job description file
     * @return The appropriate parser for the file
     * @throws IllegalArgumentException If no parser can handle the file
     */
    public JobDescriptionParser getParser(String filePath) {
        for (JobDescriptionParser parser : parsers) {
            if (parser.canParse(filePath)) {
                return parser;
            }
        }
        
        throw new IllegalArgumentException("No parser available for file: " + filePath);
    }
} 