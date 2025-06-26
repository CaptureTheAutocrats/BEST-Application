package com.best.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.best.R;
import com.best.SessionManager;
import com.best.models.Order;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrdersAsSellerAdapter extends RecyclerView.Adapter<OrdersAsSellerAdapter.OrderViewHolder> {


    private static final MediaType JSON = MediaType.get("application/json");
    private static final OkHttpClient client = new OkHttpClient();

    private List<Order>     ordertList;
    private Context         context;
    private SessionManager  sessionManager;
    private boolean         isUpdating = false;

    public OrdersAsSellerAdapter(List<Order> list, Context context) {
        this.ordertList = list;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        sessionManager  = new SessionManager(parent.getContext());
        View view       = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_orders_as_seller, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

        Order order = ordertList.get(position);
        holder.name.setText(order.product.name);
        holder.totalPrice.setText("\uD83D\uDCB0 Total: " + order.product.price * order.quantity + " ৳");

        String statusIcon = "\u2753";
        switch (order.status.toLowerCase()) {
            case "pending":
                statusIcon = "\u23F3"; // ⏳
                break;
            case "confirmed":
                statusIcon = "\uD83D\uDCE6"; // 📦
                break;
            case "completed":
                statusIcon = "\u2705"; // ✅
                break;
            case "cancelled":
                statusIcon = "\u274C"; // ❌
                break;
            case "in Box":
                statusIcon = "\uD83D\uDCE5"; // 📥
                break;
        }
        holder.status.setText(statusIcon + " Status: " + order.status);

        holder.buyer.setText("\uD83D\uDC68 Buyer: " + order.buyer.name);
        holder.buyerId.setText("\uD83D\uDD22 Buyer ID: " + order.buyer.studentId);

        // Fix: prepend full base URL to relative path
        String imageUrl = "https://catchmeifyoucan.xyz/distributed-best/" + order.product.image_path;

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(holder.imageView);

        // Set item click listener
        holder.updateOrderStatus.setOnClickListener(v->{
            
            PopupMenu popupMenu = new PopupMenu(context,v);
            popupMenu.getMenu().add("Cancelled");
            popupMenu.getMenu().add("Confirmed");

            if ( !order.status.equals("In Box") ){
                popupMenu.getMenu().add("Open Box");
                popupMenu.getMenu().add("Close Box");
                popupMenu.getMenu().add("In Box");
            }


            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    String status = menuItem.getTitle().toString();
                    //Toast.makeText(context, title + " selected", Toast.LENGTH_SHORT).show();

                    if ( status.equals("Open Box") ){

                        try {
                            JSONObject json = new JSONObject();
                            json.put("value", "lock"); // or "lock"

                            RequestBody body = RequestBody.create(json.toString(), JSON);
                            Request request = new Request.Builder()
                                    .url("https://io.adafruit.com/api/v2/mrhbijoy/feeds/lock-command/data")
                                    .post(body)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("X-AIO-Key", "aio_LfVs259yP2mxEtlA0QmksC1zwYbD")
                                    .build();

                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String responseBody = response.body().string();
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        if (response.isSuccessful()) {
                                            Toast.makeText(context, "Command sent successfully!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "Failed: " + responseBody, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });

                        }catch (Exception e){
                            e.printStackTrace();
                            Log.d("Orders ", e.toString() );
                        }

                    }

                    else if ( status.equals("Close Box") ){

                        try {
                            JSONObject json = new JSONObject();
                            json.put("value", "unlock"); // or "unlock"

                            RequestBody body = RequestBody.create(json.toString(), JSON);
                            Request request = new Request.Builder()
                                    .url("https://io.adafruit.com/api/v2/mrhbijoy/feeds/lock-command/data")
                                    .post(body)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("X-AIO-Key", "aio_LfVs259yP2mxEtlA0QmksC1zwYbD")
                                    .build();

                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    new Handler(Looper.getMainLooper()).post(() ->
                                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String responseBody = response.body().string();
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        if (response.isSuccessful()) {
                                            Toast.makeText(context, "Command sent successfully!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "Failed: " + responseBody, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });

                        }catch (Exception e){
                            e.printStackTrace();
                            Log.d("Orders ", e.toString() );
                        }

                    }

                    else {
                        try {
                            isUpdating = true;
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("order_id", order.order_id);
                            jsonObject.put("status", status);

                            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                            Request request = new Request.Builder()
                                    .url("https://catchmeifyoucan.xyz/distributed-best/api/orders.php")
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
                                        } catch (Exception e) {
                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                Toast.makeText(context, "Invalid response" + e.toString(), Toast.LENGTH_SHORT).show();
                                                Log.d("OrderAsSeller Adapter", responseBody);
                                            });
                                            isUpdating = false;
                                        }
                                    } else {
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            Toast.makeText(context, "Server error" + responseBody, Toast.LENGTH_SHORT).show();
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


                    return true;
                }
            });
            popupMenu.show();
        });

    }

    @Override
    public int getItemCount() {
        return ordertList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView    name;
        TextView    unitPrice;
        TextView    condition;
        TextView    totalPrice;
        TextView    status;
        TextView    buyer;
        TextView    buyerId;
        ImageView   imageView;
        ImageButton updateOrderStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            name                = itemView.findViewById(R.id.OrderAsSellerTxtName);
            imageView           = itemView.findViewById(R.id.OrderAsSellerImageProduct);
            totalPrice          = itemView.findViewById(R.id.OrderAsSellerTxtTotalPrice);
            status              = itemView.findViewById(R.id.OrderAsSellerTxtStatus);
            buyer               = itemView.findViewById(R.id.OrderTxtBuyer);
            buyerId             = itemView.findViewById(R.id.OrderTxtBuyerId);
            updateOrderStatus   = itemView.findViewById(R.id.OrderAsSellerButtonUpdateOrderStatus);
        }
    }

}