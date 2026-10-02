package com.karmcharikatta.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> implements Filterable {

    private List<DocumentModel> documentList;
    private List<DocumentModel> documentListFull;
    private Context context;

    public DocumentAdapter(List<DocumentModel> documentList, Context context) {
        this.documentList = documentList;
        this.documentListFull = new ArrayList<>(documentList);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentModel document = documentList.get(position);
        
        holder.textTitle.setText(document.getTitle() != null ? document.getTitle() : "Untitled Document");
        holder.textCategory.setText(document.getCategory() != null ? document.getCategory() : "");

        // Safely handle optional department field
        if (document.getDepartment() != null && !document.getDepartment().trim().isEmpty()) {
            holder.textDepartment.setText(document.getDepartment());
            holder.textDepartment.setVisibility(View.VISIBLE);
        } else {
            holder.textDepartment.setVisibility(View.GONE);
        }

        // Safely handle optional date field
        if (document.getDate() != null && !document.getDate().trim().isEmpty()) {
            holder.textDate.setText(document.getDate());
            holder.textDate.setVisibility(View.VISIBLE);
        } else {
            holder.textDate.setVisibility(View.GONE);
        }

        // Safe PDF launch logic (handles web URLs, Google Docs viewer fallback, and null safety)
        holder.btnViewPdf.setOnClickListener(v -> {
            String pdfUrl = document.getPdfUrl();
            if (pdfUrl == null || pdfUrl.trim().isEmpty()) {
                Toast.makeText(context, "PDF link not available", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Try opening directly in Browser or PDF Viewer app
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl));
                context.startActivity(intent);
            } catch (Exception e) {
                // Fallback to Google Docs PDF Viewer
                try {
                    String googleDocsUrl = "https://docs.google.com/viewer?url=" + Uri.encode(pdfUrl);
                    Intent docsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleDocsUrl));
                    context.startActivity(docsIntent);
                } catch (Exception ex) {
                    Toast.makeText(context, "Unable to open PDF link", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Delete button click
        holder.btnDeleteDoc.setOnClickListener(v -> showDeleteConfirmationDialog(document));
    }

    private void showDeleteConfirmationDialog(DocumentModel document) {
        if (document.getDocumentId() == null || document.getDocumentId().isEmpty()) {
            Toast.makeText(context, "Cannot delete: Document ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText inputPin = new EditText(context);
        inputPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputPin.setHint("Enter Admin Security PIN");
        inputPin.setPadding(50, 30, 50, 30);

        new AlertDialog.Builder(context)
                .setTitle("Delete Document")
                .setMessage("Are you sure you want to delete '" + (document.getTitle() != null ? document.getTitle() : "this document") + "'?\n\nEnter Admin Security PIN to confirm:")
                .setView(inputPin)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String pin = inputPin.getText().toString().trim();
                    if (pin.equals(AdminConfig.ADMIN_PIN)) {
                        deleteDocumentFromFirebase(document);
                    } else {
                        Toast.makeText(context, "Incorrect Security PIN!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteDocumentFromFirebase(DocumentModel document) {
        String docId = document.getDocumentId();

        // 1. Delete from Firestore "documents" collection
        FirebaseFirestore.getInstance().collection("documents").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Document Deleted Successfully!", Toast.LENGTH_SHORT).show();

                    // 2. Delete from Firebase Storage if it's a hosted storage file
                    if (document.getPdfUrl() != null && document.getPdfUrl().contains("firebasestorage")) {
                        try {
                            FirebaseStorage.getInstance().getReferenceFromUrl(document.getPdfUrl()).delete();
                        } catch (Exception ignored) {}
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public void updateList(List<DocumentModel> newList) {
        this.documentList = newList;
        this.documentListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return documentFilter;
    }

    private Filter documentFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<DocumentModel> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(documentListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (DocumentModel item : documentListFull) {
                    boolean matchesTitle = item.getTitle() != null && item.getTitle().toLowerCase().contains(filterPattern);
                    boolean matchesCategory = item.getCategory() != null && item.getCategory().toLowerCase().contains(filterPattern);
                    boolean matchesDept = item.getDepartment() != null && item.getDepartment().toLowerCase().contains(filterPattern);

                    if (matchesTitle || matchesCategory || matchesDept) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            documentList.clear();
            if (results.values != null) {
                documentList.addAll((List<DocumentModel>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textCategory, textDepartment, textDate;
        MaterialButton btnViewPdf;
        ImageButton btnDeleteDoc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textCategory = itemView.findViewById(R.id.text_category);
            textDepartment = itemView.findViewById(R.id.text_department);
            textDate = itemView.findViewById(R.id.text_date);
            btnViewPdf = itemView.findViewById(R.id.btn_view_pdf);
            btnDeleteDoc = itemView.findViewById(R.id.btn_delete_doc);
        }
    }
}
