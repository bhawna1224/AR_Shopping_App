package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class ProductDetailActivity extends BaseActivity {

    ImageView productImage;
    TextView productName, productCategory, productPrice, productDescription, quantityText, cartCountText;
    Button addToWishlistBtn, viewInARBtn, addToCartBtn, increaseBtn, decreaseBtn;
    LinearLayout quantityLayout;

    FirebaseFirestore db;
    FirebaseUser currentUser;
    String userId;
    boolean isInWishlist = false;
    DocumentReference existingWishlistDoc;
    String name, category, imageUrl, description, modelUrl;
    double price;
    int cartCount = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentWithDrawer(R.layout.activity_product_detail);

        // Initialize UI components
        productImage = findViewById(R.id.productImage);
        productName = findViewById(R.id.productName);
        productCategory = findViewById(R.id.productCategory);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        addToWishlistBtn = findViewById(R.id.addToWishlistBtn);
        viewInARBtn = findViewById(R.id.viewInARBtn);
        addToCartBtn = findViewById(R.id.addToCartBtn);
        cartCountText = findViewById(R.id.cartCountText);
        quantityLayout = findViewById(R.id.quantityLayout);
        increaseBtn = findViewById(R.id.increaseBtn);
        decreaseBtn = findViewById(R.id.decreaseBtn);
        quantityText = findViewById(R.id.quantityText);

        // Get data from intent
        name = getIntent().getStringExtra("name");
        category = getIntent().getStringExtra("category");
        price = getIntent().getDoubleExtra("price", 0.0);
        description = getIntent().getStringExtra("description");
        imageUrl = getIntent().getStringExtra("image");
        modelUrl = getIntent().getStringExtra("modelUrl");

        productName.setText(name);
        productCategory.setText(category);
        productPrice.setText("₹" + price);
        productDescription.setText(description);
        Glide.with(this).load(imageUrl).into(productImage);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (isUserAuthenticated()) {
            userId = currentUser.getUid();
            checkIfInWishlist();
            fetchCartCount();
        } else {
            addToWishlistBtn.setText("Login to use Wishlist");
            addToCartBtn.setText("Login to add to Cart");
        }

        addToCartBtn.setOnClickListener(v -> {
            if (!isUserAuthenticated()) {
                showLoginDialog();
            } else {
                addOrUpdateCartItem(1);
            }
        });

        increaseBtn.setOnClickListener(v -> {
            if (isUserAuthenticated()) {
                addOrUpdateCartItem(1);
            }
        });

        decreaseBtn.setOnClickListener(v -> {
            if (isUserAuthenticated()) {
                addOrUpdateCartItem(-1);
            }
        });

        addToWishlistBtn.setOnClickListener(v -> {
            if (!isUserAuthenticated()) {
                showLoginDialog();
            } else {
                toggleWishlist();
            }
        });

        viewInARBtn.setOnClickListener(v -> {
            if (modelUrl != null && !modelUrl.isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://arvr.google.com/scene-viewer/1.0?file="
                            + modelUrl + "&mode=ar_preferred&title=" + Uri.encode(name)));
                    intent.setPackage("com.google.android.googlequicksearchbox");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "Google Play Services for AR not installed", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "3D Model not available for this product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isUserAuthenticated()) {
            fetchCartCount();
            checkIfInWishlist();
        }
    }

    private boolean isUserAuthenticated() {
        return currentUser != null && !currentUser.isAnonymous();
    }

    private Query getProductQuery(String collection) {
        return db.collection(collection).document(userId).collection("items")
                .whereEqualTo("name", name)
                .whereEqualTo("price", price);
    }

    private void addOrUpdateCartItem(int increment) {
        getProductQuery("carts").get().addOnSuccessListener(snapshot -> {
            if (!snapshot.isEmpty()) {
                DocumentReference docRef = snapshot.getDocuments().get(0).getReference();
                long current = snapshot.getDocuments().get(0).getLong("count");
                int newCount = (int) current + increment;
                if (newCount <= 0) {
                    docRef.delete().addOnSuccessListener(unused -> {
                        cartCount = 0;
                        updateCartCountUI();
                        Toast.makeText(this, "Removed from cart", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK); // Set result for refreshing CartActivity
                    });
                } else {
                    docRef.update("count", newCount).addOnSuccessListener(unused -> {
                        cartCount = newCount;
                        updateCartCountUI();
                        Toast.makeText(this, "Cart updated!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK); // Set result for refreshing CartActivity
                    });
                }
            } else if (increment > 0) {
                Map<String, Object> cartItem = new HashMap<>();
                cartItem.put("name", name);
                cartItem.put("category", category);
                cartItem.put("price", price);
                cartItem.put("description", description);
                cartItem.put("image", imageUrl);
                cartItem.put("modelUrl", modelUrl);
                cartItem.put("count", 1);

                db.collection("carts").document(userId).collection("items").add(cartItem)
                        .addOnSuccessListener(ref -> {
                            cartCount = 1;
                            updateCartCountUI();
                            Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK); // Set result for refreshing CartActivity
                        });
            }
        });
    }

    private void fetchCartCount() {
        getProductQuery("carts").get().addOnSuccessListener(snapshot -> {
            if (!snapshot.isEmpty()) {
                Long count = snapshot.getDocuments().get(0).getLong("count");
                cartCount = (count != null) ? count.intValue() : 0;
            } else {
                cartCount = 0;
            }
            updateCartCountUI();
        }).addOnFailureListener(e -> {
            cartCount = 0;
            updateCartCountUI();
        });
    }

    private void updateCartCountUI() {
        if (cartCount > 0) {
            addToCartBtn.setVisibility(View.GONE);
            quantityLayout.setVisibility(View.VISIBLE);
            quantityText.setText(String.valueOf(cartCount));
        } else {
            addToCartBtn.setVisibility(View.VISIBLE);
            quantityLayout.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void checkIfInWishlist() {
        getProductQuery("wishlists").get().addOnSuccessListener(snapshot -> {
            if (!snapshot.isEmpty()) {
                isInWishlist = true;
                existingWishlistDoc = snapshot.getDocuments().get(0).getReference();
                addToWishlistBtn.setText("Remove from Wishlist");
            } else {
                isInWishlist = false;
                addToWishlistBtn.setText("Add to Wishlist");
            }
        });
    }

    private void toggleWishlist() {
        if (isInWishlist && existingWishlistDoc != null) {
            existingWishlistDoc.delete().addOnSuccessListener(unused -> {
                isInWishlist = false;
                addToWishlistBtn.setText("Add to Wishlist");
                Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Set result for refreshing WishlistActivity
            });
        } else {
            Map<String, Object> wishlistItem = new HashMap<>();
            wishlistItem.put("name", name);
            wishlistItem.put("category", category);
            wishlistItem.put("price", price);
            wishlistItem.put("description", description);
            wishlistItem.put("image", imageUrl);
            wishlistItem.put("modelUrl", modelUrl);

            db.collection("wishlists").document(userId).collection("items").add(wishlistItem)
                    .addOnSuccessListener(ref -> {
                        isInWishlist = true;
                        existingWishlistDoc = ref;
                        addToWishlistBtn.setText("Remove from Wishlist");
                        Toast.makeText(this, "Added to wishlist!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK); // Set result for refreshing WishlistActivity
                    });
        }
    }

    private void showLoginDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Login Required")
                .setMessage("To use this feature, please log in or register.")
                .setPositiveButton("Login", (dialog, which) -> {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() != null && auth.getCurrentUser().isAnonymous()) {
                        auth.getCurrentUser().delete();
                        auth.signOut();
                    }
                    startActivity(new Intent(ProductDetailActivity.this, Login.class));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
