package com.best.fragments;

import android.annotation.SuppressLint;
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

import com.best.MainActivity;
import com.best.R;
import com.best.SessionManager;
import com.best.adapters.CartAdapter;
import com.best.models.Cart;
import com.best.models.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<Cart> cartList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();
    private SessionManager sessionManager;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sessionManager  = new SessionManager(requireActivity().getApplicationContext());
        View view       = inflater.inflate(R.layout.fragment_cart, container, false);

        // Initialize the SwipeRefreshLayout and RecyclerView
        swipeRefreshLayout = view.findViewById(R.id.CartSwipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(cartList);
        recyclerView.setAdapter(adapter);

        Button btnOrder = view.findViewById(R.id.CartBtnOrder);
        btnOrder.setOnClickListener(v -> openOrderFragment());

        // Set SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(this::fetchCarts);

        fetchCarts(); // Fetch the cart items initially

        return view;
    }

    private void fetchCarts() {

        Log.d("Cart", "Fetching cart");

        // Show the refresh spinner
        swipeRefreshLayout.setRefreshing(true);

        Request request = new Request.Builder()
                .url("https://catchmeifyoucan.xyz/best/api/cart.php")
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("Cart", e.toString());
                swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner on failure
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {

                        String responseStr  = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseStr);

                        List<Cart> newCarts = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject cartJsonObject = jsonArray.getJSONObject(i);

                            Cart cart = new Cart();
                            cart.product = new Product();

                            cart.cart_item_id = cartJsonObject.getInt("cart_item_id");
                            cart.user_id      = cartJsonObject.getInt("user_id");
                            cart.product_id   = cartJsonObject.getInt("product_id");
                            cart.quantity     = cartJsonObject.getInt("quantity");
                            cart.updated_at   = cartJsonObject.getString("updated_at");

                            JSONObject productJsonObject = cartJsonObject.getJSONObject("product");
                            cart.product.product_id = productJsonObject.getString("product_id");
                            cart.product.name = productJsonObject.getString("name");
                            cart.product.description = productJsonObject.getString("description");
                            cart.product.price = productJsonObject.getInt("price");
                            cart.product.product_condition = productJsonObject.getString("product_condition");
                            cart.product.stock = productJsonObject.getInt("stock");
                            cart.product.image_path = productJsonObject.getString("image_path");

                            newCarts.add(cart);
                        }

                        requireActivity().runOnUiThread(() -> {
                            cartList.clear();  // Clear old data before updating
                            cartList.addAll(newCarts); // Add new data
                            adapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner after data is loaded
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("Cart", e.toString());
                        swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner on error
                    }
                } else {
                    Log.e("Cart", response.toString());
                    swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner on error
                }
            }
        });
    }

    private void openOrderFragment() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).switchFragment(R.id.nav_orders);
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav);
            bottomNav.setSelectedItemId(R.id.nav_orders);
        }
    }
}
