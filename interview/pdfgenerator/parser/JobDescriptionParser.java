package com.interview.pdfgenerator.parser;

import com.interview.pdfgenerator.model.JobDescription;

/**
 * Interface for parsing job descriptions from different file formats.
 */
public interface JobDescriptionParser {
    
    /**
     * Parse a job description from a file.
     * 
     * @param filePath Path to the job description file
     * @return Parsed JobDescription object
     * @throws Exception If parsing fails
     */
    JobDescription parse(String filePath) throws Exception;
    
    /**
     * Check if this parser can handle the given file.
     * 
     * @param filePath Path to the job description file
     * @return true if this parser can handle the file, false otherwise
     */
    boolean canParse(String filePath);
} 