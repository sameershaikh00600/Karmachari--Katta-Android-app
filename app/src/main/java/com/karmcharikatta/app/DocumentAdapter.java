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
        holder.textTitle.setText(document.getTitle());
        holder.textCategory.setText(document.getCategory());
        holder.textDepartment.setText(document.getDepartment());
        holder.textDate.setText(document.getDate());

        holder.btnViewPdf.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(document.getPdfUrl()), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            
            Intent chooser = Intent.createChooser(intent, "Open PDF");
            context.startActivity(chooser);
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
                    if (item.getTitle().toLowerCase().contains(filterPattern) ||
                        item.getCategory().toLowerCase().contains(filterPattern) ||
                        item.getDepartment().toLowerCase().contains(filterPattern)) {
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
            documentList.addAll((List) results.values);
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
