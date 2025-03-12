package com.interview.pdfgenerator.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Model class representing a Job Description with extracted information.
 */
public class JobDescription {
    private String title;
    private String company;
    private List<String> skills;
    private List<String> responsibilities;
    private List<String> technologies;
    private String description;

    public JobDescription() {
        this.skills = new ArrayList<>();
        this.responsibilities = new ArrayList<>();
        this.technologies = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public void addSkill(String skill) {
        this.skills.add(skill);
    }

    public List<String> getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(List<String> responsibilities) {
        this.responsibilities = responsibilities;
    }

    public void addResponsibility(String responsibility) {
        this.responsibilities.add(responsibility);
    }

    public List<String> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<String> technologies) {
        this.technologies = technologies;
    }

    public void addTechnology(String technology) {
        this.technologies.add(technology);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "JobDescription{" +
                "title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", skills=" + skills +
                ", responsibilities=" + responsibilities +
                ", technologies=" + technologies +
                '}';
    }
} 