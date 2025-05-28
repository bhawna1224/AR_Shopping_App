package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.ProductDetailActivity;
import com.example.myapplication.R;
import com.example.myapplication.models.Furniture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private final Context context;
    private final List<Furniture> wishlistItems;
    private final List<String> documentIds; // List of Firestore document IDs (to delete specific entries)

    public WishlistAdapter(Context context, List<Furniture> wishlistItems, List<String> documentIds) {
        this.context = context;
        this.wishlistItems = wishlistItems;
        this.documentIds = documentIds;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Furniture item = wishlistItems.get(position);

        holder.name.setText(item.getName());
        holder.category.setText(item.getCategory());
        holder.price.setText("₹" + item.getPrice());
        Glide.with(context).load(item.getImage()).into(holder.image);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String itemId = documentIds.get(position);

        // Handle Remove from Wishlist
        holder.removeBtn.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("wishlists")
                    .document(userId)
                    .collection("items")
                    .document(itemId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        // Update the wishlist UI by removing the item
                        wishlistItems.remove(position);
                        documentIds.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, wishlistItems.size());
                        Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
                    );
        });

        // Handle Add to Cart
        holder.addToCartBtn.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("carts")
                    .document(userId)
                    .collection("items")
                    .whereEqualTo("name", item.getName())
                    .whereEqualTo("price", item.getPrice())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Item already in cart: update count
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            Long currentCount = doc.getLong("count");
                            int updatedCount = (currentCount != null ? currentCount.intValue() : 0) + 1;
                            doc.getReference().update("count", updatedCount)
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show());
                        } else {
                            // Item not in cart: add with count = 1
                            Furniture cartItem = new Furniture(
                                    item.getName(),
                                    item.getPrice(),
                                    item.getImage(),
                                    item.getDescription(),
                                    item.getCategory(),
                                    item.getModelUrl(),
                                    1  // Initialize count to 1
                            );

                            db.collection("carts")
                                    .document(userId)
                                    .collection("items")
                                    .add(cartItem)
                                    .addOnSuccessListener(documentReference ->
                                            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show());
                        }
                    });
        });

        // Handle product click to open ProductDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("name", item.getName());
            intent.putExtra("category", item.getCategory());
            intent.putExtra("price", item.getPrice());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("image", item.getImage());
            intent.putExtra("modelUrl", item.getModelUrl());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return wishlistItems.size();
    }

    public static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, category, price;
        Button removeBtn, addToCartBtn;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.wishlistImage);
            name = itemView.findViewById(R.id.wishlistName);
            category = itemView.findViewById(R.id.wishlistCategory);
            price = itemView.findViewById(R.id.wishlistPrice);
            removeBtn = itemView.findViewById(R.id.removeFromWishlistBtn);
            addToCartBtn = itemView.findViewById(R.id.addToCartBtn);
        }
    }
}
