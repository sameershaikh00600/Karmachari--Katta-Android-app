package com.karmcharikatta.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Dashboard Cards
        MaterialCardView cardGr = findViewById(R.id.card_gr);
        MaterialCardView cardPension = findViewById(R.id.card_pension);
        MaterialCardView cardAllEmployees = findViewById(R.id.card_all_employees);
        MaterialCardView cardMsrtc = findViewById(R.id.card_msrtc);
        MaterialCardView cardNotice = findViewById(R.id.card_notice);

        // Toolbar Buttons
        ImageButton btnMenu = findViewById(R.id.btn_menu);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnNotification = findViewById(R.id.btn_notification);

        // Set Click Listeners
        cardGr.setOnClickListener(v -> openDocumentList("Government Resolutions (GR)"));
        cardPension.setOnClickListener(v -> openDocumentList("Pension"));
        cardAllEmployees.setOnClickListener(v -> openDocumentList("All"));
        cardMsrtc.setOnClickListener(v -> openDocumentList("MSRTC"));
        
        cardNotice.setOnClickListener(v -> 
            Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show()
        );

        btnMenu.setOnClickListener(v -> Toast.makeText(this, "Menu Clicked", Toast.LENGTH_SHORT).show());
        btnSearch.setOnClickListener(v -> openDocumentList("All"));
        btnNotification.setOnClickListener(v -> Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show());
    }

    private void openDocumentList(String category) {
        Intent intent = new Intent(this, DocumentListActivity.class);
        intent.putExtra("CATEGORY", category);
        startActivity(intent);
    }
}
