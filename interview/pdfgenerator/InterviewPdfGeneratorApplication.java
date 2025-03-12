package com.interview.pdfgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;

import com.interview.pdfgenerator.service.PdfGeneratorService;
import com.interview.pdfgenerator.service.QuestionDatabaseService;
import com.interview.pdfgenerator.util.ConsoleInputReader;

import java.io.File;

@SpringBootApplication
public class InterviewPdfGeneratorApplication implements CommandLineRunner {

    @Autowired
    private PdfGeneratorService pdfGeneratorService;
    
    @Autowired
    private ConsoleInputReader consoleInputReader;
    
    @Autowired
    private QuestionDatabaseService questionDatabaseService;

    public static void main(String[] args) {
        SpringApplication.run(InterviewPdfGeneratorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Technical Interview PDF Generator ===");
        System.out.println("This application generates a comprehensive technical interview guide based on a job description.");
        
        // Get JD file path from user
        String jdFilePath = consoleInputReader.readInput("Enter the path to the JD file (txt, JSON, or YAML): ");
        File jdFile = new File(jdFilePath);
        
        if (!jdFile.exists()) {
            System.out.println("File not found. Using default JD template.");
            jdFilePath = "src/main/resources/data/jd/default-jd.txt";
        }
        
        // Ask for additional skills
        String additionalSkills = consoleInputReader.readInput("Enter additional skills (comma-separated) or press Enter to skip: ");
        
        // Ask if user wants to use AI-generated questions
        boolean useAI = consoleInputReader.readYesNoInput("Do you want to use AI to generate questions?", true);
        questionDatabaseService.setUseAIGeneration(useAI);
        
        if (useAI) {
            System.out.println("Using AI to generate questions. This may take a few minutes...");
        } else {
            System.out.println("Using pre-defined question database.");
        }
        
        // Generate the PDF
        String outputPath = pdfGeneratorService.generateInterviewGuidePdf(jdFilePath, additionalSkills);
        
        System.out.println("PDF generated successfully at: " + outputPath);
        System.exit(0);
    }
} 