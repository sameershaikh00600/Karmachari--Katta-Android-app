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
import com.google.firebase.firestore.ListenerRegistration;
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
    private ListenerRegistration firestoreListener;

    private String mainCategory = "All";       // From Dashboard (GR, Pension, MSRTC, All)
    private String currentSubCategory = "All"; // From Chips (All, GRs, Rules, Forms)

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

        // Get Main Category from Intent (Dashboard click)
        if (getIntent().hasExtra("CATEGORY")) {
            mainCategory = getIntent().getStringExtra("CATEGORY");
        }
        if (mainCategory == null) mainCategory = "All";

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

        // Setup Filter Chips (All, GRs, Rules, Forms)
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                if (id == R.id.chip_all) currentSubCategory = "All";
                else if (id == R.id.chip_gr) currentSubCategory = "GRs";
                else if (id == R.id.chip_rules) currentSubCategory = "Rules";
                else if (id == R.id.chip_forms) currentSubCategory = "Forms";

                fetchDocuments();
            }
        });

        // Initial fetch with default subCategory ("All" within the mainCategory)
        chipGroupFilter.check(R.id.chip_all);
        fetchDocuments();
    }

    private void fetchDocuments() {
        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        progressBar.setVisibility(View.VISIBLE);
        Query query = db.collection("documents");

        // 1. Filter by Main Dashboard Section (e.g. GR, Pension, MSRTC)
        if (!mainCategory.equals("All")) {
            query = query.whereEqualTo("category", mainCategory);
        }

        // 2. Filter by Chip Sub-Category (e.g. GRs, Rules, Forms)
        if (!currentSubCategory.equals("All")) {
            query = query.whereEqualTo("subCategory", currentSubCategory);
        }

        firestoreListener = query.addSnapshotListener((value, error) -> {
            if (!isDestroyed()) {
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
                    adapter.updateList(new ArrayList<>(documentList));

                    if (documentList.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textEmpty.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }
}
