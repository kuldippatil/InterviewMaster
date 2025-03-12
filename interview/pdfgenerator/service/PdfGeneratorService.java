package com.interview.pdfgenerator.service;

import com.interview.pdfgenerator.model.InterviewGuide;
import com.interview.pdfgenerator.model.InterviewQuestion;
import com.interview.pdfgenerator.model.JobDescription;
import com.interview.pdfgenerator.parser.JobDescriptionParser;
import com.interview.pdfgenerator.parser.JobDescriptionParserFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for generating PDF interview guides.
 */
@Service
public class PdfGeneratorService {

    private final JobDescriptionParserFactory parserFactory;
    private final QuestionDatabaseService questionDatabaseService;
    
    // PDF formatting constants
    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 18;
    private static final float FONT_SIZE_HEADING = 14;
    private static final float FONT_SIZE_SUBHEADING = 12;
    private static final float FONT_SIZE_NORMAL = 10;
    private static final float FONT_SIZE_SMALL = 8;
    private static final float LINE_HEIGHT_TITLE = 22;
    private static final float LINE_HEIGHT_HEADING = 18;
    private static final float LINE_HEIGHT_NORMAL = 14;
    private static final float LINE_HEIGHT_SMALL = 10;
    private static final int MAX_LINE_WIDTH = 500;
    
    @Autowired
    public PdfGeneratorService(JobDescriptionParserFactory parserFactory, 
                              QuestionDatabaseService questionDatabaseService) {
        this.parserFactory = parserFactory;
        this.questionDatabaseService = questionDatabaseService;
    }
    
    /**
     * Generate a PDF interview guide based on a job description.
     * 
     * @param jdFilePath Path to the job description file
     * @param additionalSkills Additional skills to include
     * @return Path to the generated PDF file
     * @throws Exception If PDF generation fails
     */
    public String generateInterviewGuidePdf(String jdFilePath, String additionalSkills) throws Exception {
        // Parse the job description
        JobDescriptionParser parser = parserFactory.getParser(jdFilePath);
        JobDescription jobDescription = parser.parse(jdFilePath);
        
        // Get questions for the job description
        Map<String, List<InterviewQuestion>> questionsByCategory = 
                questionDatabaseService.getQuestionsForJobDescription(jobDescription, additionalSkills);
        
        // Create the interview guide
        InterviewGuide guide = new InterviewGuide(jobDescription);
        guide.setQuestionsByCategory(questionsByCategory);
        
        // Generate introduction and final tips
        generateIntroduction(guide);
        generateFinalTips(guide);
        
        // Generate the PDF
        return generatePdf(guide);
    }
    
    /**
     * Generate the introduction section of the interview guide.
     * 
     * @param guide The interview guide to update
     */
    private void generateIntroduction(InterviewGuide guide) {
        JobDescription jd = guide.getJobDescription();
        
        StringBuilder intro = new StringBuilder();
        intro.append("# Technical Interview Guide for ").append(jd.getTitle()).append("\n\n");
        
        intro.append("## About This Guide\n\n");
        intro.append("This comprehensive technical interview guide has been tailored specifically for the ")
             .append(jd.getTitle())
             .append(" position");
        
        if (jd.getCompany() != null && !jd.getCompany().equals("Unknown Company")) {
            intro.append(" at ").append(jd.getCompany());
        }
        
        intro.append(". It contains over 100 pages of interview questions and detailed answers ")
             .append("covering all the technical areas relevant to this role.\n\n");
        
        intro.append("## Key Skills Required\n\n");
        if (!jd.getSkills().isEmpty()) {
            for (String skill : jd.getSkills()) {
                intro.append("- ").append(skill).append("\n");
            }
        } else {
            intro.append("- Java programming\n");
            intro.append("- Object-oriented design\n");
            intro.append("- Problem-solving skills\n");
        }
        
        intro.append("\n## Technologies\n\n");
        if (!jd.getTechnologies().isEmpty()) {
            for (String tech : jd.getTechnologies()) {
                intro.append("- ").append(tech).append("\n");
            }
        } else {
            intro.append("- Java 8/11/17\n");
            intro.append("- Spring Framework\n");
            intro.append("- Databases (SQL/NoSQL)\n");
        }
        
        intro.append("\n## How to Use This Guide\n\n");
        intro.append("This guide is organized into sections covering different technical areas. ")
             .append("Each section contains questions of varying difficulty levels, from basic to advanced. ")
             .append("Review the questions and answers thoroughly, and practice explaining the concepts ")
             .append("in your own words. For coding questions, try to solve them yourself before looking at the solutions.\n\n");
        
        intro.append("Good luck with your interview preparation!\n");
        
        guide.setIntroduction(intro.toString());
    }
    
    /**
     * Generate the final tips section of the interview guide.
     * 
     * @param guide The interview guide to update
     */
    private void generateFinalTips(InterviewGuide guide) {
        StringBuilder tips = new StringBuilder();
        tips.append("# Final Tips & Resources\n\n");
        
        tips.append("## Interview Preparation Tips\n\n");
        tips.append("1. **Review Core Concepts**: Ensure you have a solid understanding of core Java concepts, especially those highlighted in this guide.\n\n");
        tips.append("2. **Practice Coding**: Regularly solve coding problems on platforms like LeetCode, HackerRank, or CodeSignal.\n\n");
        tips.append("3. **Mock Interviews**: Conduct mock interviews with peers or use services like Pramp or interviewing.io.\n\n");
        tips.append("4. **System Design Practice**: Draw out system architectures and practice explaining your design decisions.\n\n");
        tips.append("5. **Behavioral Preparation**: Prepare stories about your past experiences using the STAR method (Situation, Task, Action, Result).\n\n");
        
        tips.append("## Recommended Resources\n\n");
        tips.append("### Books\n");
        tips.append("- \"Effective Java\" by Joshua Bloch\n");
        tips.append("- \"Clean Code\" by Robert C. Martin\n");
        tips.append("- \"Java Concurrency in Practice\" by Brian Goetz\n");
        tips.append("- \"Spring in Action\" by Craig Walls\n");
        tips.append("- \"Designing Data-Intensive Applications\" by Martin Kleppmann\n\n");
        
        tips.append("### Online Courses\n");
        tips.append("- Coursera: \"Java Programming and Software Engineering Fundamentals\"\n");
        tips.append("- Udemy: \"Spring & Hibernate for Beginners\"\n");
        tips.append("- Pluralsight: \"Java Fundamentals\"\n");
        tips.append("- Baeldung: Various Spring tutorials\n\n");
        
        tips.append("### Websites\n");
        tips.append("- Baeldung (https://www.baeldung.com/)\n");
        tips.append("- DZone (https://dzone.com/)\n");
        tips.append("- Stack Overflow (https://stackoverflow.com/)\n");
        tips.append("- GitHub (explore open-source Java projects)\n");
        tips.append("- Spring.io (https://spring.io/guides)\n\n");
        
        tips.append("## Day Before the Interview\n\n");
        tips.append("1. Review this guide one more time, focusing on areas you're less confident about.\n");
        tips.append("2. Get a good night's sleep.\n");
        tips.append("3. Prepare your environment for a virtual interview or plan your route for an in-person interview.\n");
        tips.append("4. Have questions ready to ask the interviewer about the role, team, and company.\n\n");
        
        tips.append("Remember, interviews are also an opportunity for you to evaluate if the company and role are a good fit for you. Good luck!");
        
        guide.setFinalTips(tips.toString());
    }
    
    /**
     * Generate a PDF document from the interview guide.
     * 
     * @param guide The interview guide to convert to PDF
     * @return Path to the generated PDF file
     * @throws IOException If PDF generation fails
     */
    private String generatePdf(InterviewGuide guide) throws IOException {
        // Create a new document
        PDDocument document = new PDDocument();
        
        // Add a title page
        addTitlePage(document, guide);
        
        // Add table of contents
        addTableOfContents(document, guide);
        
        // Add introduction
        addIntroductionSection(document, guide);
        
        // Add question sections
        for (String category : guide.getCategories()) {
            addQuestionSection(document, category, guide.getQuestionsByCategory(category));
        }
        
        // Add final tips
        addFinalTipsSection(document, guide);
        
        // Generate output file name
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String outputFileName = "interview_guide_" + timestamp + ".pdf";
        String outputPath = outputFileName;
        
        // Save the document
        document.save(outputPath);
        document.close();
        
        return outputPath;
    }
    
    /**
     * Add a title page to the document.
     * 
     * @param document The PDF document
     * @param guide The interview guide
     * @throws IOException If adding the title page fails
     */
    private void addTitlePage(PDDocument document, InterviewGuide guide) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        
        // Set fonts
        PDFont titleFont = PDType1Font.HELVETICA_BOLD;
        PDFont normalFont = PDType1Font.HELVETICA;
        
        float pageHeight = page.getMediaBox().getHeight();
        float pageWidth = page.getMediaBox().getWidth();
        float centerX = pageWidth / 2;
        float startY = pageHeight - 200;
        
        // Title
        String title = "Technical Interview Guide";
        float titleWidth = titleFont.getStringWidth(title) / 1000 * 24;
        contentStream.beginText();
        contentStream.setFont(titleFont, 24);
        contentStream.newLineAtOffset(centerX - (titleWidth / 2), startY);
        contentStream.showText(title);
        contentStream.endText();
        
        // Job title
        String jobTitle = guide.getJobDescription().getTitle();
        float jobTitleWidth = titleFont.getStringWidth(jobTitle) / 1000 * 18;
        contentStream.beginText();
        contentStream.setFont(titleFont, 18);
        contentStream.newLineAtOffset(centerX - (jobTitleWidth / 2), startY - 40);
        contentStream.showText(jobTitle);
        contentStream.endText();
        
        // Company
        if (guide.getJobDescription().getCompany() != null && 
            !guide.getJobDescription().getCompany().equals("Unknown Company")) {
            String company = "at " + guide.getJobDescription().getCompany();
            float companyWidth = normalFont.getStringWidth(company) / 1000 * 14;
            contentStream.beginText();
            contentStream.setFont(normalFont, 14);
            contentStream.newLineAtOffset(centerX - (companyWidth / 2), startY - 70);
            contentStream.showText(company);
            contentStream.endText();
        }
        
        // Date
        String date = "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        float dateWidth = normalFont.getStringWidth(date) / 1000 * 12;
        contentStream.beginText();
        contentStream.setFont(normalFont, 12);
        contentStream.newLineAtOffset(centerX - (dateWidth / 2), startY - 120);
        contentStream.showText(date);
        contentStream.endText();
        
        // Question count
        String questionCount = "Contains " + guide.getTotalQuestionCount() + " interview questions and answers";
        float countWidth = normalFont.getStringWidth(questionCount) / 1000 * 12;
        contentStream.beginText();
        contentStream.setFont(normalFont, 12);
        contentStream.newLineAtOffset(centerX - (countWidth / 2), startY - 150);
        contentStream.showText(questionCount);
        contentStream.endText();
        
        contentStream.close();
    }
    
    /**
     * Add a table of contents to the document.
     * 
     * @param document The PDF document
     * @param guide The interview guide
     * @throws IOException If adding the table of contents fails
     */
    private void addTableOfContents(PDDocument document, InterviewGuide guide) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        
        // Set fonts
        PDFont titleFont = PDType1Font.HELVETICA_BOLD;
        PDFont normalFont = PDType1Font.HELVETICA;
        
        float startY = page.getMediaBox().getHeight() - MARGIN;
        float currentY = startY;
        
        // Title
        contentStream.beginText();
        contentStream.setFont(titleFont, FONT_SIZE_TITLE);
        contentStream.newLineAtOffset(MARGIN, currentY);
        contentStream.showText("Table of Contents");
        contentStream.endText();
        
        currentY -= LINE_HEIGHT_TITLE * 2;
        
        // Introduction
        contentStream.beginText();
        contentStream.setFont(normalFont, FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(MARGIN, currentY);
        contentStream.showText("1. Introduction");
        contentStream.endText();
        
        currentY -= LINE_HEIGHT_NORMAL;
        
        // Question sections
        int sectionNumber = 2;
        for (String category : guide.getCategories()) {
            contentStream.beginText();
            contentStream.setFont(normalFont, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, currentY);
            contentStream.showText(sectionNumber + ". " + category + " Questions");
            contentStream.endText();
            
            currentY -= LINE_HEIGHT_NORMAL;
            sectionNumber++;
        }
        
        // Final tips
        contentStream.beginText();
        contentStream.setFont(normalFont, FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(MARGIN, currentY);
        contentStream.showText(sectionNumber + ". Final Tips & Resources");
        contentStream.endText();
        
        contentStream.close();
    }
    
    /**
     * Add the introduction section to the document.
     * 
     * @param document The PDF document
     * @param guide The interview guide
     * @throws IOException If adding the introduction fails
     */
    private void addIntroductionSection(PDDocument document, InterviewGuide guide) throws IOException {
        if (guide.getIntroduction() == null || guide.getIntroduction().isEmpty()) {
            return;
        }
        
        String[] lines = guide.getIntroduction().split("\n");
        addTextSection(document, lines);
    }
    
    /**
     * Add the final tips section to the document.
     * 
     * @param document The PDF document
     * @param guide The interview guide
     * @throws IOException If adding the final tips fails
     */
    private void addFinalTipsSection(PDDocument document, InterviewGuide guide) throws IOException {
        if (guide.getFinalTips() == null || guide.getFinalTips().isEmpty()) {
            return;
        }
        
        String[] lines = guide.getFinalTips().split("\n");
        addTextSection(document, lines);
    }
    
    /**
     * Add a question section to the document.
     * 
     * @param document The PDF document
     * @param category The category of questions
     * @param questions The list of questions
     * @throws IOException If adding the question section fails
     */
    private void addQuestionSection(PDDocument document, String category, List<InterviewQuestion> questions) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        
        // Set fonts
        PDFont titleFont = PDType1Font.HELVETICA_BOLD;
        PDFont normalFont = PDType1Font.HELVETICA;
        
        float startY = page.getMediaBox().getHeight() - MARGIN;
        float currentY = startY;
        
        // Section title
        contentStream.beginText();
        contentStream.setFont(titleFont, FONT_SIZE_TITLE);
        contentStream.newLineAtOffset(MARGIN, currentY);
        contentStream.showText(category + " Questions");
        contentStream.endText();
        
        currentY -= LINE_HEIGHT_TITLE * 2;
        
        contentStream.close();
        
        // Add questions
        for (InterviewQuestion question : questions) {
            addQuestion(document, question, currentY);
            currentY = -1; // Force new page for each question
        }
    }
    
    /**
     * Add a question to the document.
     * 
     * @param document The PDF document
     * @param question The question to add
     * @param startY The Y position to start at, or -1 to start a new page
     * @throws IOException If adding the question fails
     */
    private void addQuestion(PDDocument document, InterviewQuestion question, float startY) throws IOException {
        PDPage page;
        float currentY;
        
        if (startY < 0) {
            // Start a new page
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            currentY = page.getMediaBox().getHeight() - MARGIN;
        } else {
            // Use the current page
            page = document.getPage(document.getNumberOfPages() - 1);
            currentY = startY;
        }
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
        
        // Set fonts
        PDFont headingFont = PDType1Font.HELVETICA_BOLD;
        PDFont normalFont = PDType1Font.HELVETICA;
        PDFont codeFont = PDType1Font.COURIER;
        
        // Subcategory
        if (question.getSubcategory() != null && !question.getSubcategory().isEmpty()) {
            contentStream.beginText();
            contentStream.setFont(headingFont, FONT_SIZE_SUBHEADING);
            contentStream.newLineAtOffset(MARGIN, currentY);
            contentStream.showText(question.getSubcategory());
            contentStream.endText();
            
            currentY -= LINE_HEIGHT_NORMAL;
        }
        
        // Question
        contentStream.beginText();
        contentStream.setFont(headingFont, FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(MARGIN, currentY);
        contentStream.showText("Q: " + question.getQuestion());
        contentStream.endText();
        
        currentY -= LINE_HEIGHT_NORMAL * 1.5f;
        
        // Answer
        String[] answerLines = question.getAnswer().split("\n");
        contentStream.beginText();
        contentStream.setFont(normalFont, FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(MARGIN, currentY);
        contentStream.showText("A:");
        contentStream.endText();
        
        currentY -= LINE_HEIGHT_NORMAL;
        
        // Process answer lines
        for (String line : answerLines) {
            // Check if we need a new page
            if (currentY < MARGIN) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                currentY = page.getMediaBox().getHeight() - MARGIN;
            }
            
            // Check if this is a code block
            if (line.trim().startsWith("```")) {
                // Skip the opening/closing code block markers
                continue;
            }
            
            // Determine if we're in a code block
            boolean isCodeLine = false;
            if (question.getCodeExample() != null && !question.getCodeExample().isEmpty()) {
                isCodeLine = line.trim().startsWith("    ") || line.contains("public ") || 
                             line.contains("private ") || line.contains("class ") || 
                             line.contains("interface ") || line.contains("enum ");
            }
            
            // Write the line
            contentStream.beginText();
            if (isCodeLine) {
                contentStream.setFont(codeFont, FONT_SIZE_SMALL);
                contentStream.newLineAtOffset(MARGIN + 20, currentY);
            } else {
                contentStream.setFont(normalFont, FONT_SIZE_NORMAL);
                contentStream.newLineAtOffset(MARGIN + 10, currentY);
            }
            contentStream.showText(line);
            contentStream.endText();
            
            currentY -= isCodeLine ? LINE_HEIGHT_SMALL : LINE_HEIGHT_NORMAL;
        }
        
        contentStream.close();
    }
    
    /**
     * Add a text section to the document.
     * 
     * @param document The PDF document
     * @param lines The lines of text to add
     * @throws IOException If adding the text section fails
     */
    private void addTextSection(PDDocument document, String[] lines) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        
        // Set fonts
        PDFont titleFont = PDType1Font.HELVETICA_BOLD;
        PDFont headingFont = PDType1Font.HELVETICA_BOLD;
        PDFont normalFont = PDType1Font.HELVETICA;
        
        float startY = page.getMediaBox().getHeight() - MARGIN;
        float currentY = startY;
        
        for (String line : lines) {
            // Check if we need a new page
            if (currentY < MARGIN) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                currentY = startY;
            }
            
            // Skip empty lines
            if (line.trim().isEmpty()) {
                currentY -= LINE_HEIGHT_NORMAL / 2;
                continue;
            }
            
            // Determine line type and formatting
            PDFont font;
            float fontSize;
            float indent = 0;
            float lineHeight;
            
            if (line.startsWith("# ")) {
                // Title
                font = titleFont;
                fontSize = FONT_SIZE_TITLE;
                line = line.substring(2);
                lineHeight = LINE_HEIGHT_TITLE;
            } else if (line.startsWith("## ")) {
                // Heading
                font = headingFont;
                fontSize = FONT_SIZE_HEADING;
                line = line.substring(3);
                lineHeight = LINE_HEIGHT_HEADING;
            } else if (line.startsWith("### ")) {
                // Subheading
                font = headingFont;
                fontSize = FONT_SIZE_SUBHEADING;
                line = line.substring(4);
                lineHeight = LINE_HEIGHT_NORMAL;
            } else if (line.startsWith("- ")) {
                // Bullet point
                font = normalFont;
                fontSize = FONT_SIZE_NORMAL;
                indent = 20;
                line = "• " + line.substring(2);
                lineHeight = LINE_HEIGHT_NORMAL;
            } else {
                // Normal text
                font = normalFont;
                fontSize = FONT_SIZE_NORMAL;
                lineHeight = LINE_HEIGHT_NORMAL;
            }
            
            // Write the line
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(MARGIN + indent, currentY);
            contentStream.showText(line);
            contentStream.endText();
            
            currentY -= lineHeight;
        }
        
        contentStream.close();
    }
} 