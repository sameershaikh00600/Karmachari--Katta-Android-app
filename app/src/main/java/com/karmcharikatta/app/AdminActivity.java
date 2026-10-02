package com.karmcharikatta.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    // Document Form Views
    private TextInputEditText editDocTitle, editDocDepartment, editDocDate, editPdfUrl;
    private Spinner spinnerMainCategory, spinnerSubCategory;
    private TextView textSelectedFile;
    private MaterialButton btnSelectPdf, btnUploadDoc;
    private ProgressBar progressUploadDoc;

    // Notice Form Views
    private TextInputEditText editNoticeTitle, editNoticeMessage, editNoticeBlogUrl;
    private MaterialButton btnPublishNotice;
    private ProgressBar progressNotice;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri selectedPdfUri = null;

    // ActivityResultLauncher for PDF file selection
    private final ActivityResultLauncher<String> pdfPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedPdfUri = uri;
                    textSelectedFile.setText("PDF Selected: " + uri.getLastPathSegment());
                } else {
                    Toast.makeText(AdminActivity.this, "No file chosen", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Bind Document Form Views
        editDocTitle = findViewById(R.id.edit_doc_title);
        editDocDepartment = findViewById(R.id.edit_doc_department);
        editDocDate = findViewById(R.id.edit_doc_date);
        editPdfUrl = findViewById(R.id.edit_pdf_url);
        spinnerMainCategory = findViewById(R.id.spinner_main_category);
        spinnerSubCategory = findViewById(R.id.spinner_sub_category);
        textSelectedFile = findViewById(R.id.text_selected_file);
        btnSelectPdf = findViewById(R.id.btn_select_pdf);
        btnUploadDoc = findViewById(R.id.btn_upload_doc);
        progressUploadDoc = findViewById(R.id.progress_upload_doc);

        // Bind Notice Form Views
        editNoticeTitle = findViewById(R.id.edit_notice_title);
        editNoticeMessage = findViewById(R.id.edit_notice_message);
        editNoticeBlogUrl = findViewById(R.id.edit_notice_blog_url);
        btnPublishNotice = findViewById(R.id.btn_publish_notice);
        progressNotice = findViewById(R.id.progress_notice);

        // Setup Main Category Spinner
        String[] mainCategories = {
                "कर्मचारी शासन निर्णय",
                "PF / NPS / Pension / EPS",
                "सर्व शासकीय कर्मचारी",
                "MSRTC कर्मचारी"
        };
        ArrayAdapter<String> mainAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mainCategories);
        spinnerMainCategory.setAdapter(mainAdapter);

        // Setup Sub Category Spinner
        String[] subCategories = {"GRs", "Rules", "Forms", "All"};
        ArrayAdapter<String> subAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subCategories);
        spinnerSubCategory.setAdapter(subAdapter);

        // Choose PDF Button Click
        btnSelectPdf.setOnClickListener(v -> pdfPickerLauncher.launch("application/pdf"));

        // Upload Document Button Click
        btnUploadDoc.setOnClickListener(v -> uploadDocumentProcess());

        // Publish Notice Button Click
        btnPublishNotice.setOnClickListener(v -> publishNoticeProcess());
    }

    private void uploadDocumentProcess() {
        String title = editDocTitle.getText() != null ? editDocTitle.getText().toString().trim() : "";
        String mainCategory = spinnerMainCategory.getSelectedItem().toString();
        String subCategory = spinnerSubCategory.getSelectedItem().toString();
        String department = editDocDepartment.getText() != null ? editDocDepartment.getText().toString().trim() : "";
        String dateStr = editDocDate.getText() != null ? editDocDate.getText().toString().trim() : "";
        String pdfUrlInput = editPdfUrl.getText() != null ? editPdfUrl.getText().toString().trim() : "";

        if (title.isEmpty()) {
            editDocTitle.setError("Title is required");
            editDocTitle.requestFocus();
            return;
        }

        if (pdfUrlInput.isEmpty() && selectedPdfUri == null) {
            Toast.makeText(this, "Please enter a PDF URL or choose a PDF file!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show Progress
        progressUploadDoc.setVisibility(View.VISIBLE);
        btnUploadDoc.setEnabled(false);

        // Auto-generate date if left blank
        String finalDate = dateStr.isEmpty() ? new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()) : dateStr;

        if (!pdfUrlInput.isEmpty()) {
            // Save directly using provided PDF URL
            saveDocumentToFirestore(title, mainCategory, subCategory, department, finalDate, pdfUrlInput);
        } else {
            // 1. Upload PDF File to Firebase Storage
            String fileName = "pdf_" + System.currentTimeMillis() + ".pdf";
            StorageReference storageRef = storage.getReference().child("pdfs/" + fileName);

            storageRef.putFile(selectedPdfUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String pdfUrl = downloadUri.toString();
                        saveDocumentToFirestore(title, mainCategory, subCategory, department, finalDate, pdfUrl);
                    }))
                    .addOnFailureListener(e -> {
                        progressUploadDoc.setVisibility(View.GONE);
                        btnUploadDoc.setEnabled(true);
                        Toast.makeText(AdminActivity.this, "Storage Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveDocumentToFirestore(String title, String mainCategory, String subCategory, String department, String date, String pdfUrl) {
        DocumentModel document = new DocumentModel(title, mainCategory, subCategory, department, date, pdfUrl);

        db.collection("documents")
                .add(document)
                .addOnSuccessListener(documentReference -> {
                    progressUploadDoc.setVisibility(View.GONE);
                    btnUploadDoc.setEnabled(true);
                    Toast.makeText(AdminActivity.this, "Document Added Successfully!", Toast.LENGTH_LONG).show();

                    // Reset Document Form
                    editDocTitle.setText("");
                    editDocDepartment.setText("");
                    editDocDate.setText("");
                    editPdfUrl.setText("");
                    selectedPdfUri = null;
                    textSelectedFile.setText("No PDF file selected");
                })
                .addOnFailureListener(e -> {
                    progressUploadDoc.setVisibility(View.GONE);
                    btnUploadDoc.setEnabled(true);
                    Toast.makeText(AdminActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void publishNoticeProcess() {
        String noticeTitle = editNoticeTitle.getText() != null ? editNoticeTitle.getText().toString().trim() : "";
        String message = editNoticeMessage.getText() != null ? editNoticeMessage.getText().toString().trim() : "";
        String blogUrl = editNoticeBlogUrl.getText() != null ? editNoticeBlogUrl.getText().toString().trim() : "";

        if (noticeTitle.isEmpty()) {
            editNoticeTitle.setError("Notice title required");
            return;
        }

        if (message.isEmpty()) {
            editNoticeMessage.setError("Message required");
            return;
        }

        progressNotice.setVisibility(View.VISIBLE);
        btnPublishNotice.setEnabled(false);

        Map<String, Object> noticeMap = new HashMap<>();
        noticeMap.put("title", noticeTitle);
        noticeMap.put("message", message);
        noticeMap.put("blogUrl", blogUrl);
        noticeMap.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));

        // Save entry in Firestore "notices" collection
        db.collection("notices")
                .add(noticeMap)
                .addOnSuccessListener(documentReference -> {
                    progressNotice.setVisibility(View.GONE);
                    btnPublishNotice.setEnabled(true);
                    Toast.makeText(AdminActivity.this, "Notice Published Successfully!", Toast.LENGTH_LONG).show();

                    // Reset Notice Form
                    editNoticeTitle.setText("");
                    editNoticeMessage.setText("");
                    editNoticeBlogUrl.setText("");
                })
                .addOnFailureListener(e -> {
                    progressNotice.setVisibility(View.GONE);
                    btnPublishNotice.setEnabled(true);
                    Toast.makeText(AdminActivity.this, "Failed to Publish Notice: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
