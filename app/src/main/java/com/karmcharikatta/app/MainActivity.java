package com.karmcharikatta.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Dashboard Cards
        MaterialCardView cardGr = findViewById(R.id.card_gr);
        MaterialCardView cardPension = findViewById(R.id.card_pension);
        MaterialCardView cardAllEmployees = findViewById(R.id.card_all_employees);
        MaterialCardView cardMsrtc = findViewById(R.id.card_msrtc);
        MaterialCardView cardNotice = findViewById(R.id.card_notice);

        // Notice TextViews
        TextView textNoticeTitle = findViewById(R.id.text_notice_title);
        TextView textNoticeMessage = findViewById(R.id.text_notice_message);

        // Toolbar Buttons
        ImageButton btnMenu = findViewById(R.id.btn_menu);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnNotification = findViewById(R.id.btn_notification);

        // Fetch Dynamic Notice from Firestore
        db.collection("notices")
          .limit(1)
          .addSnapshotListener((value, error) -> {
              if (value != null && !value.isEmpty()) {
                  DocumentSnapshot doc = value.getDocuments().get(0);
                  String title = doc.getString("title");
                  String message = doc.getString("message");
                  String blogUrl = doc.getString("blogUrl");

                  if (title != null && !title.trim().isEmpty()) {
                      textNoticeTitle.setText(title);
                  }
                  if (message != null && !message.trim().isEmpty()) {
                      textNoticeMessage.setText(message);
                  }

                  // Open Blog Link on Notice Card Click
                  cardNotice.setOnClickListener(v -> {
                      if (blogUrl != null && !blogUrl.trim().isEmpty()) {
                          try {
                              Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(blogUrl));
                              startActivity(intent);
                          } catch (Exception e) {
                              Toast.makeText(MainActivity.this, "Unable to open link", Toast.LENGTH_SHORT).show();
                          }
                      } else {
                          Toast.makeText(MainActivity.this, "No link attached to this notice", Toast.LENGTH_SHORT).show();
                      }
                  });
              } else {
                  cardNotice.setOnClickListener(v -> 
                      Toast.makeText(MainActivity.this, "No new notifications", Toast.LENGTH_SHORT).show()
                  );
              }
          });

        // Set Click Listeners - Passing exact UI text as the category
        cardGr.setOnClickListener(v -> openDocumentList("कर्मचारी शासन निर्णय"));
        cardPension.setOnClickListener(v -> openDocumentList("PF / NPS / Pension / EPS"));
        cardAllEmployees.setOnClickListener(v -> openDocumentList("सर्व शासकीय कर्मचारी"));
        cardMsrtc.setOnClickListener(v -> openDocumentList("MSRTC कर्मचारी"));

        btnMenu.setOnClickListener(v -> Toast.makeText(this, "Menu Clicked", Toast.LENGTH_SHORT).show());
        btnSearch.setOnClickListener(v -> openDocumentList("सर्व शासकीय कर्मचारी"));
        btnNotification.setOnClickListener(v -> Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show());
    }

    private void openDocumentList(String category) {
        Intent intent = new Intent(this, DocumentListActivity.class);
        intent.putExtra("CATEGORY", category);
        startActivity(intent);
    }
}
