package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.Furniture;
import com.example.myapplication.adapters.FurnitureAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FurnitureAdapter adapter;
    List<Furniture> furnitureList;
    ProgressBar progressBar;
    FirebaseFirestore db;
    Spinner spinnerCategory;
    String selectedCategory = "All"; // Default
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnLogout = findViewById(R.id.btnLogout);
        db = FirebaseFirestore.getInstance();
        furnitureList = new ArrayList<>();
        adapter = new FurnitureAdapter(this, furnitureList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupCategorySpinner();

        btnLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut(); // Sign out user
            Intent intent = new Intent(HomeActivity.this, Login.class);
            startActivity(intent);
            finish(); // Close HomeActivity so user cannot return using the back button
        });
    }

    private void setupCategorySpinner() {
        String[] categories = {"All", "Sofa", "Table", "Chair", "Bed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                loadFurnitureData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Load all items by default
        loadFurnitureData();
    }

    private void loadFurnitureData() {
        progressBar.setVisibility(View.VISIBLE);
        furnitureList.clear();

        db.collection("furniture")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Furniture furniture = document.toObject(Furniture.class);
                            // If category is "All" or matches selected category, add to list
                            if (selectedCategory.equals("All") || furniture.getCategory().equals(selectedCategory)) {
                                furnitureList.add(furniture);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(HomeActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
