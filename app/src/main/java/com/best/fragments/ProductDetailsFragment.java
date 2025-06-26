package com.best.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.best.R;
import com.best.SessionManager;
import com.best.models.Product;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProductDetailsFragment extends Fragment {


    private ImageView productImage;
    private TextView txtProductName;
    private TextView txtProductDescription;
    private TextView txtProductPrice;
    private TextView txtProductCondition;
    private TextView txtProductStock;
    private TextView txtProductSellerName;
    private TextView txtProductSellerId;
    private TextView txtProductSellerPhoneNumber;
    private TextView btnRemoveProduct;

    private Product product;

    private final OkHttpClient client      = new OkHttpClient();
    private static final String API_URL    = "https://catchmeifyoucan.xyz/distributed-best/api/get-info-seller.php";
    private SessionManager sessionManager;


    public ProductDetailsFragment(Product product) {
        this.product = product;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productImage                = view.findViewById(R.id.productImage);
        txtProductName              = view.findViewById(R.id.txtProductName);
        txtProductDescription       = view.findViewById(R.id.txtProductDescription);
        txtProductPrice             = view.findViewById(R.id.txtProductPrice);
        txtProductCondition         = view.findViewById(R.id.txtProductCondition);
        txtProductStock             = view.findViewById(R.id.txtProductStock);
        txtProductSellerName        = view.findViewById(R.id.txtProductSellerName);
        txtProductSellerId          = view.findViewById(R.id.txtProductSellerId);
        txtProductSellerPhoneNumber = view.findViewById(R.id.txtProductSellerPhoneNumber);
        btnRemoveProduct            = view.findViewById(R.id.remove_product_button_id);

        if ( product.user_id != sessionManager.getUserId() ){
            btnRemoveProduct.setVisibility(View.GONE);
        }

        if (product != null) {

            txtProductName.setText(product.name);
            txtProductDescription.setText(product.description);
            txtProductPrice.setText("Price: " + product.price + " BDT");
            txtProductCondition.setText("Condition: " + product.product_condition);
            txtProductStock.setText("Stock: " + product.stock);

            try {

                Request request = new Request.Builder()
                        .url(API_URL+"?user_id="+product.user_id)
                        .get()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                        .build();

                request.url();

                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {
                        requireActivity().runOnUiThread(()->{
                            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        String responseBody = response.body().string();
                        if (response.isSuccessful()) {
                            try {
                                JSONObject resp = new JSONObject(responseBody);
                                String name = resp.getString("name");
                                String student_id = resp.getString("student_id");
                                String phone_number = resp.getString("phone_number");
                                requireActivity().runOnUiThread(()->{
                                    txtProductSellerName.setText( name);
                                    txtProductSellerName.setVisibility(View.VISIBLE);
                                    txtProductSellerId.setText( student_id);
                                    txtProductSellerId.setVisibility(View.VISIBLE);
                                    txtProductSellerPhoneNumber.setText(phone_number);
                                    txtProductSellerPhoneNumber.setVisibility(View.VISIBLE);
                                    Log.d("Product Details Fragment", responseBody.toString());
                                });
                            }
                            catch (Exception e) {
//                                requireActivity().runOnUiThread(()->{
//                                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
//                                });
                            }
                        }
                        else {
                            requireActivity().runOnUiThread(()->{
                                Toast.makeText(getContext(), "Server error!", Toast.LENGTH_SHORT).show();
                            });
                        }

                    }
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(()->{
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                });
            }

            // Fix: prepend full base URL to relative path
            String imageUrl = "https://catchmeifyoucan.xyz/distributed-best/" + product.image_path;

            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(productImage);

        }

        btnRemoveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(requireContext())
                    .setTitle("Remove Product")
                    .setMessage("Are you sure you want to remove this product?")

                    .setPositiveButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setNeutralButton("Yes", (dialog, which) -> {

                        try {

                            JSONObject json = new JSONObject();
                            json.put("product_id", product.product_id);
                            json.put("product_condition", product.product_condition);

                            RequestBody  body   = RequestBody.create(json.toString(), MediaType.parse("application/json"));
                            Request request = new Request.Builder()
                                    .url("https://catchmeifyoucan.xyz/distributed-best/api/remove-product.php")
                                    .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                                    .post(body)
                                    .build();

                            client.newCall(request).enqueue(new Callback() {

                                @Override
                                public void onFailure(Call call, IOException e) {
                                    requireActivity().runOnUiThread(()->{
                                        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {

                                    try {
                                        String responseBody = response.body().string();
                                        JSONObject jsonObject = new JSONObject(responseBody);
                                        String message = jsonObject.getString("message");

                                        if (response.isSuccessful()) {
                                            requireActivity().runOnUiThread(()->{
                                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                                requireActivity().getSupportFragmentManager().popBackStack();
                                            });
                                        }
                                        else {
                                            requireActivity().runOnUiThread(()->{
                                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                        catch (Exception e) {
                            requireActivity().runOnUiThread(()->{
                                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            });
                        }


                    })
                    .show();

            }
        });
    }
}