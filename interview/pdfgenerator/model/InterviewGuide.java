package com.interview.pdfgenerator.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Model class representing the complete interview guide with all sections and questions.
 */
public class InterviewGuide {
    private JobDescription jobDescription;
    private Map<String, List<InterviewQuestion>> questionsByCategory;
    private String introduction;
    private String finalTips;

    public InterviewGuide() {
        this.questionsByCategory = new HashMap<>();
    }

    public InterviewGuide(JobDescription jobDescription) {
        this.jobDescription = jobDescription;
        this.questionsByCategory = new HashMap<>();
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(JobDescription jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Map<String, List<InterviewQuestion>> getQuestionsByCategory() {
        return questionsByCategory;
    }

    public void setQuestionsByCategory(Map<String, List<InterviewQuestion>> questionsByCategory) {
        this.questionsByCategory = questionsByCategory;
    }

    public void addQuestion(InterviewQuestion question) {
        String category = question.getCategory();
        if (!questionsByCategory.containsKey(category)) {
            questionsByCategory.put(category, new ArrayList<>());
        }
        questionsByCategory.get(category).add(question);
    }

    public List<InterviewQuestion> getQuestionsByCategory(String category) {
        return questionsByCategory.getOrDefault(category, new ArrayList<>());
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getFinalTips() {
        return finalTips;
    }

    public void setFinalTips(String finalTips) {
        this.finalTips = finalTips;
    }

    /**
     * Get all categories in the guide
     * @return List of category names
     */
    public List<String> getCategories() {
        return new ArrayList<>(questionsByCategory.keySet());
    }

    /**
     * Get total number of questions in the guide
     * @return Total question count
     */
    public int getTotalQuestionCount() {
        int count = 0;
        for (List<InterviewQuestion> questions : questionsByCategory.values()) {
            count += questions.size();
        }
        return count;
    }
} 