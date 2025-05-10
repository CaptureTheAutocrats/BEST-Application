package com.best.adapters;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.best.R;
import com.best.SessionManager;
import com.best.models.Cart;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import okhttp3.*;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private static final String API_URL = "https://catchmeifyoucan.xyz/best/api/cart.php";
    private static final MediaType JSON = MediaType.get("application/json");
    private static final OkHttpClient client = new OkHttpClient();
    private static boolean isUpdating   = false;

    private List<Cart> cartList;
    private Context context;
    private SessionManager sessionManager;

    public CartAdapter(List<Cart> list, Context context) {
        this.cartList = list;
        this.context = context;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        sessionManager = new SessionManager(parent.getContext());
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
                increaseCartItem(cartList.get(position).product.product_id);
            }
        });

        // Handle LESS button
        holder.less.setOnClickListener(v -> {
            if ( !isUpdating ){
                if (cart.quantity > 1) {
                    cart.quantity -= 1;
                    notifyItemChanged(position);
                    decreaseCartItem(cartList.get(position).product.product_id);
                }
            }
        });

        // Handle Delete button
        holder.delete.setOnClickListener(v -> {
            if ( !isUpdating ){

                deleteCartItem(cartList.get(position).product.product_id);

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

    public void increaseCartItem(int product_id) {

        try {
            isUpdating = true;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("product_id", product_id);
            jsonObject.put("operation", "+");

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
            Request request = new Request.Builder()
                    .url("https://catchmeifyoucan.xyz/best/api/cart.php")
                    .patch(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    });
                    isUpdating = false;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject resp = new JSONObject(responseBody);
                            String message = resp.getString("message");
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            });
                            isUpdating = false;
                        }
                        catch (Exception e) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(context, "Invalid response", Toast.LENGTH_SHORT).show();
                            });
                            isUpdating = false;
                        }
                    }
                    else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show();
                        });
                        isUpdating = false;
                    }

                }
            });

        } catch (JSONException e) {
            isUpdating = false;
            throw new RuntimeException(e);
        }
    }

    public void decreaseCartItem(int product_id){
        try {
            isUpdating = true;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("product_id", product_id);
            jsonObject.put("operation", "-");

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
            Request request = new Request.Builder()
                    .url("https://catchmeifyoucan.xyz/best/api/cart.php")
                    .patch(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    });
                    isUpdating = false;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject resp = new JSONObject(responseBody);
                            String message  = resp.getString("message");
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            });
                            isUpdating = false;
                        }
                        catch (Exception e) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(context, "Invalid response", Toast.LENGTH_SHORT).show();
                            });
                            isUpdating = false;
                        }
                    }
                    else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show();
                        });
                        isUpdating = false;
                    }

                }
            });

        } catch (JSONException e) {
            isUpdating = false;
            throw new RuntimeException(e);
        }
    }

    public void deleteCartItem(int product_id) {

        try {
            isUpdating = true;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("product_id", product_id);

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
            Request request = new Request.Builder()
                    .url("https://catchmeifyoucan.xyz/best/api/cart.php")
                    .delete(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    });
                    isUpdating = false;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject resp = new JSONObject(responseBody);
                            String message = resp.getString("message");
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            });
                            isUpdating = false;
                        }
                        catch (Exception e) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(context, "Invalid response", Toast.LENGTH_SHORT).show();
                            });
                            isUpdating = false;
                        }
                    }
                    else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show();
                        });
                        isUpdating = false;
                    }

                }
            });

        } catch (JSONException e) {
            isUpdating = false;
            throw new RuntimeException(e);
        }
    }
}