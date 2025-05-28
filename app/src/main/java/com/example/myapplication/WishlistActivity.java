package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.WishlistAdapter;
import com.example.myapplication.models.Furniture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends BaseActivity {

    private RecyclerView wishlistRecyclerView;
    private WishlistAdapter adapter;
    private List<Furniture> wishlistItems;
    private List<String> documentIds;
    private ProgressBar progressBar;
    private TextView emptyText;
    public ActivityResultLauncher<Intent> productDetailLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentWithDrawer(R.layout.activity_wishlist);

        wishlistRecyclerView = findViewById(R.id.wishlistRecyclerView);
        progressBar = findViewById(R.id.wishlistProgressBar);
        emptyText = findViewById(R.id.emptyWishlistText);

        wishlistItems = new ArrayList<>();
        documentIds = new ArrayList<>();

        // Set up the adapter with initial empty data
        adapter = new WishlistAdapter(this, wishlistItems, documentIds);
        wishlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        wishlistRecyclerView.setAdapter(adapter);

        // Register the result launcher for ProductDetailActivity to refresh the wishlist
        productDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadWishlist(); // Refresh the wishlist when returning from ProductDetailActivity
                    }
                });

        loadWishlist(); // Load wishlist data on initial launch
    }

    @SuppressLint("SetTextI18n")
    private void loadWishlist() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch the user's wishlist from Firestore
        FirebaseFirestore.getInstance()
                .collection("wishlists")
                .document(userId)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Clear existing data to avoid duplication
                    wishlistItems.clear();
                    documentIds.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Create Furniture objects from the Firestore documents
                        Furniture item = doc.toObject(Furniture.class);
                        if (item != null) {
                            wishlistItems.add(item);
                            documentIds.add(doc.getId()); // Save document ID to remove item if needed
                        }
                    }

                    // Notify adapter to refresh the RecyclerView
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (wishlistItems.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE); // Show empty text if wishlist is empty
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    emptyText.setText("Failed to load wishlist.");
                    emptyText.setVisibility(View.VISIBLE);
                });
    }
}
