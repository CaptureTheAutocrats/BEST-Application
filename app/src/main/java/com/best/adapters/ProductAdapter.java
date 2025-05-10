package com.best.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.best.R;
import com.best.models.Product;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;

    private OnItemClickListener listener; // Interface to handle clicks

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {

        Product product = productList.get(position);
        holder.name.setText(product.name);
        holder.price.setText("\uD83D\uDCB0 Price: " + product.price + " ৳");
        holder.condition.setText("\uD83E\uDDFE Condition: " + product.product_condition);
        holder.stock.setText("\uD83D\uDCE6 Stock: " + product.stock);

        // Fix: prepend full base URL to relative path
        String imageUrl = "https://catchmeifyoucan.xyz/best/" + product.image_path;

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(holder.imageView);

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
        TextView  stock;
        ImageView imageView;
        Button    addToCartBtn;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name         = itemView.findViewById(R.id.txtName);
            price        = itemView.findViewById(R.id.txtPrice);
            condition    = itemView.findViewById(R.id.txtCondition);
            imageView    = itemView.findViewById(R.id.imageProduct);
            stock        = itemView.findViewById(R.id.txtStock);
            addToCartBtn = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
