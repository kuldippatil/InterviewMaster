package com.interview.pdfgenerator.model;

/**
 * Model class representing an interview question with its answer.
 */
public class InterviewQuestion {
    private String category;
    private String subcategory;
    private String question;
    private String answer;
    private String codeExample;
    private int difficulty; // 1-5 scale

    public InterviewQuestion() {
    }

    public InterviewQuestion(String category, String subcategory, String question, String answer) {
        this.category = category;
        this.subcategory = subcategory;
        this.question = question;
        this.answer = answer;
        this.difficulty = 3; // Default medium difficulty
    }

    public InterviewQuestion(String category, String subcategory, String question, String answer, String codeExample, int difficulty) {
        this.category = category;
        this.subcategory = subcategory;
        this.question = question;
        this.answer = answer;
        this.codeExample = codeExample;
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCodeExample() {
        return codeExample;
    }

    public void setCodeExample(String codeExample) {
        this.codeExample = codeExample;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return "InterviewQuestion{" +
                "category='" + category + '\'' +
                ", subcategory='" + subcategory + '\'' +
                ", question='" + question + '\'' +
                ", difficulty=" + difficulty +
                '}';
    }
} 