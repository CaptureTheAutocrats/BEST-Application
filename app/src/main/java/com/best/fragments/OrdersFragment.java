package com.best.fragments;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.best.R;
import com.best.SessionManager;
import com.best.adapters.OrdersAdapter;
import com.best.models.Order;
import com.best.models.Product;
import com.google.android.material.materialswitch.MaterialSwitch;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OrdersFragment extends Fragment {


    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();
    private SessionManager sessionManager;
    private boolean globalIsAsSeller = false;

    private TextView ordersAsBuyerTxtView;
    private TextView ordersAsSellerTxtView;

    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    private static final MediaType JSON = MediaType.get("application/json");

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sessionManager  = new SessionManager(requireActivity().getApplicationContext());
        View view       = inflater.inflate(R.layout.fragment_orders, container, false);

        // Initialize the SwipeRefreshLayout and RecyclerView
        swipeRefreshLayout  = view.findViewById(R.id.OrdersSwipeRefreshLayout);
        recyclerView        = view.findViewById(R.id.recyclerOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter             = new OrdersAdapter(orderList, requireActivity().getApplicationContext());
        recyclerView.setAdapter(adapter);

        MaterialSwitch roleSwitch = view.findViewById(R.id.OrdersRoleSwitch);
        roleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isAsSeller) {
                changeOrderView(isAsSeller);
            }
        });

        ordersAsBuyerTxtView  = view.findViewById(R.id.OrdersAsBuyerTxtView);
        ordersAsSellerTxtView = view.findViewById(R.id.OrdersAsSellerTextView);

        // Set SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(()->{
            orderList.clear();
            fetchOrders();
        });

        fetchOrders(); // Fetch the Order items initially

        return view;
    }

    private void fetchOrders() {

        Log.d("Order", "Fetching orders");

        // Show the refresh spinner
        swipeRefreshLayout.setRefreshing(true);

        Request request;
        if ( globalIsAsSeller ){
            request = new Request.Builder()
                    .url("https://catchmeifyoucan.xyz/best/api/orders.php")
                    .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                    .addHeader("IsAsSeller", "true")
                    .build();
        }
        else {
             request = new Request.Builder()
                    .url("https://catchmeifyoucan.xyz/best/api/orders.php")
                    .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                    .build();
        }

        client.newCall(request).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("Order", e.toString());
                swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner on failure
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {

                        String responseStr   = response.body().string();
                        JSONArray jsonArray  = new JSONArray(responseStr);
                        List<Order> newOrders = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject orderJsonObject = jsonArray.getJSONObject(i);

                            Order order = new Order();
                            order.product = new Product();

                            order.order_id     = orderJsonObject.getInt("order_id");
                            order.buyer_id     = orderJsonObject.getInt("buyer_id");
                            order.seller_id    = orderJsonObject.getInt("seller_id");
                            order.product_id   = orderJsonObject.getInt("product_id");
                            order.quantity     = orderJsonObject.getInt("quantity");
                            order.total_amount = orderJsonObject.getInt("total_amount");
                            order.status       = orderJsonObject.getString("status");
                            order.box_id       = orderJsonObject.getString("box_id");
                            order.pickup_code  = orderJsonObject.getString("pickup_code");
                            order.created_at   = orderJsonObject.getString("created_at");
                            order.updated_at   = orderJsonObject.getString("updated_at");

                            JSONObject productJsonObject = orderJsonObject.getJSONObject("product");
                            order.product.product_id         = productJsonObject.getInt("product_id");
                            order.product.name               = productJsonObject.getString("name");
                            order.product.description        = productJsonObject.getString("description");
                            order.product.price              = productJsonObject.getInt("price");
                            order.product.product_condition  = productJsonObject.getString("product_condition");
                            order.product.stock              = productJsonObject.getInt("stock");
                            order.product.image_path         = productJsonObject.getString("image_path");

                            newOrders.add(order);
                        }

                        requireActivity().runOnUiThread(() -> {
                            orderList.clear();  // Clear old data before updating
                            orderList.addAll(newOrders); // Add new data
                            adapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner after data is loaded
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("Order", e.toString());
                        swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner on error
                    }
                } else {
                    Log.e("Order", response.toString());
                    swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner on error
                }
            }
        });
    }

    private void changeOrderView(boolean isAsSeller){
        globalIsAsSeller = isAsSeller;
        orderList.clear();
        fetchOrders();

        if ( isAsSeller ){
            ordersAsBuyerTxtView.setTypeface(null, Typeface.NORMAL);
            ordersAsSellerTxtView.setTypeface(null, Typeface.BOLD);
        }else{
            ordersAsBuyerTxtView.setTypeface(null, Typeface.BOLD);
            ordersAsSellerTxtView.setTypeface(null, Typeface.NORMAL);
        }

    }

}