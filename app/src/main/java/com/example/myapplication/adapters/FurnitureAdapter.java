package com.example.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.ProductDetailActivity;
import com.example.myapplication.R; // Add this import for R
import com.example.myapplication.models.Furniture;

import java.util.List;

public class FurnitureAdapter extends RecyclerView.Adapter<FurnitureAdapter.ViewHolder> {
    private Context context;
    private List<Furniture> furnitureList;

    public FurnitureAdapter(Context context, List<Furniture> furnitureList) {
        this.context = context;
        this.furnitureList = furnitureList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_furniture, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Furniture furniture = furnitureList.get(position);
        holder.textViewName.setText(furniture.getName());
        holder.textViewPrice.setText("$" + furniture.getPrice());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("name", furniture.getName());
            intent.putExtra("category", furniture.getCategory());
            intent.putExtra("price", furniture.getPrice());
            intent.putExtra("description", furniture.getDescription());
            intent.putExtra("image", furniture.getImage());
            intent.putExtra("modelUrl", furniture.getModelUrl());
            context.startActivity(intent);
        });

        Glide.with(context).load(furniture.getImage()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return furnitureList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewPrice;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Fix the IDs to match the XML file
            textViewName = itemView.findViewById(R.id.furniture_name);
            textViewPrice = itemView.findViewById(R.id.furniture_price);
            imageView = itemView.findViewById(R.id.furniture_image);
        }
    }
}

