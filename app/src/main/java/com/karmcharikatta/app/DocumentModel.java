package com.karmcharikatta.app;

import com.google.firebase.Timestamp;

public class DocumentModel {
    private String title;
    private String category;
    private String department;
    private String date;
    private String pdfUrl;

    // Required no-arg constructor for Firestore
    public DocumentModel() {}

    public DocumentModel(String title, String category, String department, String date, String pdfUrl) {
        this.title = title;
        this.category = category;
        this.department = department;
        this.date = date;
        this.pdfUrl = pdfUrl;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
}
