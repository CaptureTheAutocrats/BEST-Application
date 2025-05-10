package com.best.adapters;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.best.R;
import com.best.models.Cart;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import okhttp3.*;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private static final String API_URL = "https://catchmeifyoucan.xyz/best/api/cart.php";
    private static final MediaType JSON = MediaType.get("application/json");
    private static final OkHttpClient client = new OkHttpClient();
    private static boolean isUpdating   = false;

    private List<Cart> cartList;

    public CartAdapter(List<Cart> list) {
        this.cartList = list;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {

        Cart cart = cartList.get(position);
        holder.name.setText(cart.product.name);
        holder.unitPrice.setText("\uD83D\uDCB0 Price: " + cart.product.price + " ৳");
        holder.condition.setText("\uD83E\uDDFE Condition: " + cart.product.product_condition);
        holder.quantity.setText("\uD83D\uDCE6 Quantity: " + cart.quantity);
        holder.totalPrice.setText("\uD83D\uDCB0 Total: " + cart.product.price * cart.quantity + " ৳");

        // Fix: prepend full base URL to relative path
        String imageUrl = "https://catchmeifyoucan.xyz/best/" + cart.product.image_path;

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(holder.imageView);

        // Handle MORE button
        holder.more.setOnClickListener(v -> {
            if ( !isUpdating ){
                cart.quantity += 1;
                notifyItemChanged(position);
            }
        });

        // Handle LESS button
        holder.less.setOnClickListener(v -> {
            if ( !isUpdating ){
                if (cart.quantity > 1) {
                    cart.quantity -= 1;
                    notifyItemChanged(position);
                }
            }
        });

        holder.delete.setOnClickListener(v -> {
            if ( !isUpdating ){
                cartList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartList.size());
            }
        });


    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView  name;
        TextView  unitPrice;
        TextView  condition;
        TextView  quantity;
        TextView  totalPrice;
        ImageView imageView;

        ImageButton more;
        ImageButton less;
        ImageButton delete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            name        = itemView.findViewById(R.id.CartTxtName);
            unitPrice   = itemView.findViewById(R.id.CartTxtUnitPrice);
            condition   = itemView.findViewById(R.id.CartTxtCondition);
            imageView   = itemView.findViewById(R.id.CartImageProduct);
            quantity    = itemView.findViewById(R.id.CartTxtQuantity);
            totalPrice  = itemView.findViewById(R.id.CartTxtTotalPrice);

            more        = itemView.findViewById(R.id.CartBtnMore);
            less        = itemView.findViewById(R.id.CartBtnLess);
            delete      = itemView.findViewById(R.id.CartBtnDelete);
        }
    }


    public String patchRequest(String url, String jsonBody) throws Exception {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .method("PATCH", body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }

    public String deleteRequest(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }
}