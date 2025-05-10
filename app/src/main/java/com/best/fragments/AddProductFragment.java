package com.best.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.best.R;
import com.best.SessionManager;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class AddProductFragment extends Fragment {

    private static final int REQUEST_IMAGE_PICK = 1001;

    EditText    editName;
    EditText    editDescription;
    EditText    editPrice;
    EditText    editStock;
    RadioGroup  radioGroupCond;
    Button      btnUpload;
    ImageView   imagePreview;
    Bitmap      selectedBitmap;
    CircularProgressIndicator progressIndicator;
    View        view;

    SessionManager sessionManager;
    public AddProductFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_product, container, false);

        editName            = view.findViewById(R.id.editName);
        editDescription     = view.findViewById(R.id.editDescription);
        editPrice           = view.findViewById(R.id.editPrice);
        radioGroupCond      = view.findViewById(R.id.radioGroupCondition);
        editStock           = view.findViewById(R.id.editStock);
        btnUpload           = view.findViewById(R.id.btnUpload);
        imagePreview        = view.findViewById(R.id.imagePreview);
        progressIndicator   = view.findViewById(R.id.progressIndicatorUpload);

        sessionManager  = new SessionManager(requireContext());

        btnUpload.setOnClickListener(v -> uploadProduct());

        imagePreview.setImageResource(R.drawable.image_placeholder);
        imagePreview.setOnClickListener(v->selectImage());

        return view;
    }

    private void selectImage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {

                // Get image size in bytes
                long imageSizeInBytes = requireActivity().getContentResolver()
                        .openAssetFileDescriptor(uri, "r")
                        .getLength();

                if (imageSizeInBytes > 1024 * 1024) { // 1 MB
                    Toast.makeText(getContext(), "Image must be under 1 MB", Toast.LENGTH_SHORT).show();
                    return;
                }

                selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                imagePreview.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                Log.d("AddProductFragment","Image Selection: " + e.toString());
            }
        }
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Connection timeout
                .readTimeout(60, TimeUnit.SECONDS)     // Read timeout
                .writeTimeout(60, TimeUnit.SECONDS)    // Write timeout
                .build();
    }

    private void uploadProduct() {

        String name         = editName.getText().toString().trim();
        String description  = editDescription.getText().toString().trim();
        String price        = editPrice.getText().toString().trim();
        String stock        = editStock.getText().toString().trim();

        RadioButton selectedRadioButton = view.findViewById(radioGroupCond.getCheckedRadioButtonId());
        String      condition           = selectedRadioButton.getText().toString(); // "New" or "Used"

        if (selectedBitmap == null) {
            Toast.makeText(getContext(), "Select an image first", Toast.LENGTH_SHORT).show();
            return;
        }



        requireActivity().runOnUiThread(()-> progressIndicator.setVisibility(View.VISIBLE));

        // Convert image to base64
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        String base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
        String imageString = base64Image;

        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("description", description);
            json.put("price", Double.parseDouble(price));
            json.put("product_condition", condition);
            json.put("stock", Integer.parseInt(stock));
            json.put("image", imageString);
            json.put("image_ext","png");
        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpClient client =  getOkHttpClient();
        RequestBody  body   = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://catchmeifyoucan.xyz/best/api/add-product.php")
                .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->{
                    progressIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Upload failed " + e, Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                requireActivity().runOnUiThread(() -> {
                    progressIndicator.setVisibility(View.INVISIBLE);
                    if (response.code() == 201) {
                        Toast.makeText(getContext(), "Product added", Toast.LENGTH_SHORT).show();
                        // Clear all fields after successful upload
                        clearFields();
                    }
                    else if (response.code() == 401) {
                        Toast.makeText(getContext(), "Session expired", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Helper method to clear fields
    private void clearFields() {
        editName.setText("");
        editDescription.setText("");
        editPrice.setText("");
        editStock.setText("");
        radioGroupCond.clearCheck(); // Deselect any radio buttons

        // Clear the selected image and reset image preview
        selectedBitmap = null;
        imagePreview.setImageResource(R.drawable.image_placeholder); // Use a placeholder or clear it
    }
}
