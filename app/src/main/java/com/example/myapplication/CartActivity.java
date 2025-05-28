package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.CartAdapter;
import com.example.myapplication.models.Furniture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends BaseActivity {

    RecyclerView cartRecyclerView;
    CartAdapter cartAdapter;
    ArrayList<Furniture> cartItems;
    private List<String> documentIds;
    FirebaseFirestore db;
    ProgressBar progressBar;
    private TextView emptyText;

    public ActivityResultLauncher<Intent> productDetailLauncher; // 👈 Made public

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentWithDrawer(R.layout.activity_cart);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        progressBar = findViewById(R.id.cartProgressBar);
        emptyText = findViewById(R.id.emptyCartText);

        cartItems = new ArrayList<>();
        documentIds = new ArrayList<>();
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(cartItems, this, documentIds);
        cartRecyclerView.setAdapter(cartAdapter);

        db = FirebaseFirestore.getInstance();

        productDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        fetchCartItems(); // 👈 refresh cart when returning from ProductDetailActivity
                    }
                });

        fetchCartItems(); // ⬅ Initial cart fetch
    }

    @SuppressLint("SetTextI18n")
    public void fetchCartItems() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        db.collection("carts").document(userId).collection("items")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        cartItems.clear();
                        documentIds.clear();

                        for (DocumentSnapshot document : task.getResult()) {
                            Furniture product = document.toObject(Furniture.class);
                            if (product != null) {
                                cartItems.add(product);
                                documentIds.add(document.getId());
                            }
                        }

                        cartAdapter.notifyDataSetChanged();

                        if (cartItems.isEmpty()) {
                            emptyText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this, "Failed to load cart", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyText.setText("Failed to load cart.");
                    emptyText.setVisibility(View.VISIBLE);
                });
    }
}
