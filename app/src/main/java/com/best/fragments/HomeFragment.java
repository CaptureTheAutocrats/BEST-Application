package com.best.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.best.R;
import com.best.adapters.ProductAdapter;
import com.best.models.Product;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import okhttp3.*;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view       = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView    = view.findViewById(R.id.recyclerProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);

        fetchProducts();

        return view;
    }

    private void fetchProducts() {
        Request request = new Request.Builder()
                .url("https://catchmeifyoucan.xyz/best/api/products.php?page=1&limit=10")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String      responseStr = response.body().string();
                        JSONArray   jsonArray   = new JSONArray(responseStr);
                        productList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject       = jsonArray.getJSONObject(i);
                            Product product             = new Product();
                            product.product_id          = jsonObject.getString("product_id");
                            product.name                = jsonObject.getString("name");
                            product.price               = jsonObject.getInt("price");
                            product.product_condition   = jsonObject.getString("product_condition");
                            product.stock               = jsonObject.getInt("stock");
                            product.image_path          = jsonObject.getString("image_path");
                            productList.add(product);
                            //Log.d("HomeFragment", jsonObject.toString());
                        }

                        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
