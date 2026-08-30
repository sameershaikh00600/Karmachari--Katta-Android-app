package com.karmcharikatta.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DocumentListActivity extends AppCompatActivity {

    private static final String TAG = "DocumentListActivity";
    private RecyclerView recyclerView;
    private DocumentAdapter adapter;
    private List<DocumentModel> documentList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private SearchView searchView;
    private ChipGroup chipGroupFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_list);

        // Initialize UI components
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        textEmpty = findViewById(R.id.text_empty);
        searchView = findViewById(R.id.search_view);
        chipGroupFilter = findViewById(R.id.chip_group_filter);

        // Setup RecyclerView
        documentList = new ArrayList<>();
        adapter = new DocumentAdapter(documentList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get Category from Intent
        String initialCategory = getIntent().getStringExtra("CATEGORY");
        if (initialCategory == null) initialCategory = "All";

        // Fetch Documents
        fetchDocuments(initialCategory);

        // Update Chip Selection based on Intent
        updateChipSelection(initialCategory);

        // Setup Search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        // Setup Filter Chips
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                fetchDocuments("All");
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chip_all) fetchDocuments("All");
                else if (id == R.id.chip_gr) fetchDocuments("Government Resolutions (GR)");
                else if (id == R.id.chip_rules) fetchDocuments("Rules");
                else if (id == R.id.chip_forms) fetchDocuments("Forms");
            }
        });
    }

    private void updateChipSelection(String category) {
        if (category.equals("All")) chipGroupFilter.check(R.id.chip_all);
        else if (category.equals("Government Resolutions (GR)")) chipGroupFilter.check(R.id.chip_gr);
        else if (category.equals("Rules")) chipGroupFilter.check(R.id.chip_rules);
        else if (category.equals("Forms")) chipGroupFilter.check(R.id.chip_forms);
    }

    private void fetchDocuments(String category) {
        progressBar.setVisibility(View.VISIBLE);
        Query query = db.collection("documents");

        if (!category.equals("All")) {
            query = query.whereEqualTo("category", category);
        }

        query.addSnapshotListener((value, error) -> {
            progressBar.setVisibility(View.GONE);
            if (error != null) {
                Log.e(TAG, "Listen failed.", error);
                Toast.makeText(DocumentListActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                List<DocumentModel> fetchedDocuments = value.toObjects(DocumentModel.class);
                documentList.clear();
                documentList.addAll(fetchedDocuments);
                adapter.updateList(documentList);

                if (documentList.isEmpty()) {
                    textEmpty.setVisibility(View.VISIBLE);
                } else {
                    textEmpty.setVisibility(View.GONE);
                }
            }
        });
    }
}
