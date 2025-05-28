package com.example.myapplication.adapters;

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
import com.example.myapplication.CartActivity;
import com.example.myapplication.ProductDetailActivity;
import com.example.myapplication.R;
import com.example.myapplication.models.Furniture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private ArrayList<Furniture> cartList;
    private Context context;
    private final List<String> documentIds;

    public CartAdapter(ArrayList<Furniture> cartList, Context context, List<String> documentIds) {
        this.cartList = cartList;
        this.context = context;
        this.documentIds = documentIds;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Furniture product = cartList.get(position);
        holder.productName.setText(product.getName());
        holder.productPrice.setText("₹" + product.getPrice());
        holder.itemQuantity.setText("Qty: " + product.getCount());

        Glide.with(context).load(product.getImage()).into(holder.productImage);

        // Remove from Cart
        holder.removeFromCartBtn.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String itemId = documentIds.get(position);

            FirebaseFirestore.getInstance()
                    .collection("carts")
                    .document(userId)
                    .collection("items")
                    .document(itemId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        cartList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, cartList.size());
                        Toast.makeText(context, "Removed from cart", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show();
                    });
        });

        // Increase item quantity
        holder.increaseQuantityBtn.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String itemId = documentIds.get(position);

            FirebaseFirestore.getInstance()
                    .collection("carts")
                    .document(userId)
                    .collection("items")
                    .document(itemId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            long currentCount = documentSnapshot.getLong("count");
                            int updatedCount = (int) currentCount + 1;

                            DocumentReference docRef = documentSnapshot.getReference();
                            docRef.update("count", updatedCount)
                                    .addOnSuccessListener(aVoid -> {
                                        cartList.get(position).setCount(updatedCount);
                                        notifyItemChanged(position);
                                        Toast.makeText(context, "Item quantity increased!", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show()
                    );
        });

        // Decrease item quantity
        holder.decreaseQuantityBtn.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String itemId = documentIds.get(position);

            FirebaseFirestore.getInstance()
                    .collection("carts")
                    .document(userId)
                    .collection("items")
                    .document(itemId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            long currentCount = documentSnapshot.getLong("count");
                            int updatedCount = (int) currentCount - 1;

                            if (updatedCount <= 0) {
                                // Remove the item
                                documentSnapshot.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            cartList.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, cartList.size());
                                            Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                documentSnapshot.getReference().update("count", updatedCount)
                                        .addOnSuccessListener(aVoid -> {
                                            cartList.get(position).setCount(updatedCount);
                                            notifyItemChanged(position);
                                            Toast.makeText(context, "Item quantity decreased!", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show()
                    );
        });

        // Item click opens ProductDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("name", product.getName());
            intent.putExtra("category", product.getCategory());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("description", product.getDescription());
            intent.putExtra("image", product.getImage());
            intent.putExtra("modelUrl", product.getModelUrl());

            if (context instanceof CartActivity) {
                ((CartActivity) context).productDetailLauncher.launch(intent);
            } else {
                context.startActivity(intent); // fallback
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, itemQuantity;
        Button removeFromCartBtn, increaseQuantityBtn, decreaseQuantityBtn;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.cartProductImage);
            productName = itemView.findViewById(R.id.cartProductName);
            productPrice = itemView.findViewById(R.id.cartProductPrice);
            itemQuantity = itemView.findViewById(R.id.cartProductCount);
            removeFromCartBtn = itemView.findViewById(R.id.removeFromCartBtn);
            increaseQuantityBtn = itemView.findViewById(R.id.increaseQuantityBtn);
            decreaseQuantityBtn = itemView.findViewById(R.id.decreaseQuantityBtn);
        }
    }
}
