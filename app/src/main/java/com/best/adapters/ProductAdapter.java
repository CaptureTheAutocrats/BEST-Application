package com.best.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public ProductAdapter(List<Product> list) {
        this.productList = list;
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
        holder.price.setText("Price: " + product.price + " BDT");
        holder.condition.setText("Condition: " + product.product_condition);
        holder.stock.setText("Stock: " + product.stock);

        // Fix: prepend full base URL to relative path
        String imageUrl = "https://catchmeifyoucan.xyz/best/" + product.image_path;

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imageView);
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

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name        = itemView.findViewById(R.id.txtName);
            price       = itemView.findViewById(R.id.txtPrice);
            condition   = itemView.findViewById(R.id.txtCondition);
            imageView   = itemView.findViewById(R.id.imageProduct);
            stock       = itemView.findViewById(R.id.txtStock);
        }
    }
}
