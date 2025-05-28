package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.FurnitureAdapter;
import com.example.myapplication.models.Furniture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private FurnitureAdapter adapter;
    private List<Furniture> furnitureList;
    private ProgressBar progressBar;
    private Spinner spinnerCategory;
    private FirebaseFirestore db;
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentWithDrawer(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        db = FirebaseFirestore.getInstance();
        furnitureList = new ArrayList<>();
        adapter = new FurnitureAdapter(this, furnitureList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupCategorySpinner();
    }

    private void loadFurnitureData() {
        progressBar.setVisibility(View.VISIBLE);
        furnitureList.clear();

        db.collection("furniture")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Furniture furniture = doc.toObject(Furniture.class);
                            if (selectedCategory.equals("All") ||
                                    furniture.getCategory().equalsIgnoreCase(selectedCategory)) {
                                furnitureList.add(furniture);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Exception e = task.getException();
                        Log.e("FirestoreError", "Error loading data", e);
                        Toast.makeText(HomeActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupCategorySpinner() {
        String[] categories = {"All", "Sofa", "Table", "Chair", "Bed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                loadFurnitureData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
