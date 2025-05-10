package com.best.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.best.LoginActivity;
import com.best.MainActivity;
import com.best.R;
import com.best.SessionManager;
import com.best.adapters.ProductAdapter;
import com.best.models.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.*;
import okhttp3.*;

public class ProductsFragment extends Fragment implements ProductAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;


    private final OkHttpClient client       = new OkHttpClient();
    private static final String CART_URL   = "https://catchmeifyoucan.xyz/best/api/cart.php";
    private static final MediaType JSON     = MediaType.get("application/json; charset=utf-8");
    private SessionManager sessionManager;

    private boolean isLoading = false;
    private int currentPage = 1;
    private final int limit = 10; // per page


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view       = inflater.inflate(R.layout.fragment_products, container, false);
        recyclerView    = view.findViewById(R.id.recyclerProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        adapter = new ProductAdapter(productList, this); // Pass this as the listener
        recyclerView.setAdapter(adapter);

        // Initialize the SwipeRefreshLayout and RecyclerView
        swipeRefreshLayout = view.findViewById(R.id.ProductsSwipeRefreshLayout);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastVisibleItemPosition() >= productList.size() - 1) {
                    isLoading = true;
                    currentPage++;
                    fetchProducts(currentPage);
                }
            }
        });

        sessionManager  = new SessionManager(requireActivity().getApplicationContext());

        // Set SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reset the currentPage to 1 to start fetching from the first page again when refreshing
            currentPage = 1;
            productList.clear();
            fetchProducts(currentPage);
        });

        fetchProducts(currentPage);

        return view;
    }

    private void fetchProducts(int page) {

        // Show the refresh spinner
        swipeRefreshLayout.setRefreshing(true);

        isLoading = true;
        Request request = new Request.Builder()
                .url("https://catchmeifyoucan.xyz/best/api/products.php?page=" + page + "&limit=" + limit)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner on failure
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseStr = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseStr);

                        List<Product> newProducts = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject productJsonObject = jsonArray.getJSONObject(i);
                            Product product = new Product();
                            product.product_id = productJsonObject.getString("product_id");
                            product.name = productJsonObject.getString("name");
                            product.description = productJsonObject.getString("description");
                            product.price = productJsonObject.getInt("price");
                            product.product_condition = productJsonObject.getString("product_condition");
                            product.stock = productJsonObject.getInt("stock");
                            product.image_path = productJsonObject.getString("image_path");
                            newProducts.add(product);
                        }

                        requireActivity().runOnUiThread(() -> {
                            productList.addAll(newProducts);
                            adapter.notifyDataSetChanged();
                            isLoading = false;
                            swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner on failure
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        isLoading = false;
                        swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner on failure
                    }
                } else {
                    isLoading = false;
                    swipeRefreshLayout.setRefreshing(false); // Hide the refresh spinner on failure
                }
            }
        });
    }


    @Override
    public void onItemClick(View view, Product product) {

        if ( view.getId() == R.id.btnAddToCart ){
            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("product_id", product.product_id);
                jsonObject.put("quantity", 1);

                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                Request request = new Request.Builder()
                        .url("https://catchmeifyoucan.xyz/best/api/cart.php")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                        .build();

                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e("ProductFragment", "Network error", e);
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        String responseBody = response.body().string();
                        if (response.isSuccessful()) {
                            try {
                                JSONObject resp = new JSONObject(responseBody);
                                String message = resp.getString("message");
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                });
                            }
                            catch (Exception e) {
                                requireActivity().runOnUiThread(() ->{
                                    Toast.makeText(getContext(), "Invalid response", Toast.LENGTH_SHORT).show();
                                    Log.e("ProductFragment", "Parse error", e);
                                });

                            }
                        }
                        else if (response.code() == 401) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                            });
                        }
                        else {
                            requireActivity().runOnUiThread(() ->{
                                Toast.makeText(getContext(), "Server error", Toast.LENGTH_SHORT).show();
                            });
                        }

                    }
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(()->{
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }
        else{
            requireActivity().runOnUiThread(()-> Toast.makeText(getContext(), product.name, Toast.LENGTH_SHORT).show());
            ProductDetailsFragment  productDetailsFragment  = new ProductDetailsFragment(product);
            FragmentTransaction     transaction             = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, productDetailsFragment);
            transaction.addToBackStack(null); /// allow back navigation
            transaction.commit();
        }
    }

}


