package com.best.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.best.R;
import com.best.models.Product;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

public class ProductDetailsFragment extends Fragment {


    private ImageView productImage;
    private TextView txtProductName;
    private TextView txtProductDescription;
    private TextView txtProductPrice;
    private TextView txtProductCondition;
    private TextView txtProductStock;
    private MaterialButton btnAddToCart;

    private Product product;

    public ProductDetailsFragment(Product product) {
        this.product = product;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productImage            = view.findViewById(R.id.productImage);
        txtProductName          = view.findViewById(R.id.txtProductName);
        txtProductDescription   = view.findViewById(R.id.txtProductDescription);
        txtProductPrice         = view.findViewById(R.id.txtProductPrice);
        txtProductCondition     = view.findViewById(R.id.txtProductCondition);
        txtProductStock         = view.findViewById(R.id.txtProductStock);
        btnAddToCart            = view.findViewById(R.id.btnAddToCart);

        if (product != null) {
            txtProductName.setText(product.name);
            txtProductDescription.setText(product.description);
            txtProductPrice.setText("Price: " + product.price + " BDT");
            txtProductCondition.setText("Condition: " + product.product_condition);
            txtProductStock.setText("Stock: " + product.stock);

            // Fix: prepend full base URL to relative path
            String imageUrl = "https://catchmeifyoucan.xyz/best/" + product.image_path;

            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(productImage);

        }
    }
}