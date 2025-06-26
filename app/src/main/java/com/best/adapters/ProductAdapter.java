package com.best.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.best.R;
import com.best.SessionManager;
import com.best.models.Product;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;

    private OnItemClickListener listener; // Interface to handle clicks

    private final String API_PATH = "https://catchmeifyoucan.xyz/distributed-best/";

    SessionManager sessionManager;

    public interface OnItemClickListener {
        void onItemClick(View view, Product product);
    }

    public ProductAdapter(List<Product> list, OnItemClickListener listener) {
        this.productList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        sessionManager = new SessionManager(parent.getContext());
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

        Product product = productList.get(position);
        holder.name.setText(product.name);
        holder.price.setText("\uD83D\uDCB0 Price: " + product.price + " ৳");
        holder.condition.setText("\uD83E\uDDFE Condition: " + product.product_condition);


        // Fix: prepend full base URL to relative path
        String imageUrl = API_PATH + product.image_path;

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(holder.imageView);


        if (product.user_id == sessionManager.getUserId() ) {
            holder.cardRoot.setBackgroundResource(R.drawable.red_border);
        } else {
            holder.cardRoot.setBackgroundResource(0); // Remove background
        }

        // Set item click listener
        holder.itemView.setOnClickListener(v -> listener.onItemClick(v,product));
        holder.addToCartBtn.setOnClickListener(v->listener.onItemClick(v,product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView  name;
        TextView  price;
        TextView  condition;
        ImageView imageView;
        Button    addToCartBtn;
        View      cardRoot;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name         = itemView.findViewById(R.id.txtName);
            price        = itemView.findViewById(R.id.txtPrice);
            condition    = itemView.findViewById(R.id.txtCondition);
            imageView    = itemView.findViewById(R.id.imageProduct);
            addToCartBtn = itemView.findViewById(R.id.btnAddToCart);
            cardRoot     = itemView.findViewById(R.id.cardRoot); // Reference to CardView

        }
    }
}
