package com.karmcharikatta.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textCategory = itemView.findViewById(R.id.text_category);
            textDepartment = itemView.findViewById(R.id.text_department);
            textDate = itemView.findViewById(R.id.text_date);
            btnViewPdf = itemView.findViewById(R.id.btn_view_pdf);
        }
    }
}
