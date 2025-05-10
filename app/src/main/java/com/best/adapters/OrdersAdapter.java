package com.best.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.best.R;
import com.best.SessionManager;
import com.best.models.Order;
import com.squareup.picasso.Picasso;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private static final String API_URL = "https://catchmeifyoucan.xyz/best/api/orders.php";
    private static final MediaType JSON = MediaType.get("application/json");
    private static final OkHttpClient client = new OkHttpClient();

    private List<Order> ordertList;
    private Context context;
    private SessionManager sessionManager;
    private boolean isUpdating = false;

    public OrdersAdapter(List<Order> list, Context context) {
        this.ordertList = list;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        sessionManager = new SessionManager(parent.getContext());
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_orders, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

        Order order = ordertList.get(position);
        holder.name.setText(order.product.name);
        holder.quantity.setText("\uD83D\uDCE6 Quantity: " + order.quantity);
        holder.totalPrice.setText("\uD83D\uDCB0 Total: " + order.product.price * order.quantity + " ৳");

        String statusIcon = "\u2753";
        switch (order.status.toLowerCase()) {
            case "pending":
                statusIcon = "\u23F3"; // ⏳
                break;
            case "completed":
                statusIcon = "\u2705"; // ✅
                break;
            case "cancelled":
                statusIcon = "\u274C"; // ❌
                break;
        }
        holder.status.setText(statusIcon + " Status: " + order.status);

        // Fix: prepend full base URL to relative path
        String imageUrl = "https://catchmeifyoucan.xyz/best/" + order.product.image_path;

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return ordertList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView  unitPrice;
        TextView  condition;
        TextView  quantity;
        TextView  totalPrice;
        TextView  status;
        ImageView imageView;

        ImageButton delete;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            name        = itemView.findViewById(R.id.OrderTxtName);
            imageView   = itemView.findViewById(R.id.OrderImageProduct);
            quantity    = itemView.findViewById(R.id.OrderTxtQuantity);
            totalPrice  = itemView.findViewById(R.id.OrderTxtTotalPrice);
            status      = itemView.findViewById(R.id.OrderTxtStatus);
            delete      = itemView.findViewById(R.id.OrderBtnDelete);
        }
    }


}