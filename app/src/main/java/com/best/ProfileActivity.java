package com.best;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.best.databinding.ActivityProfileBinding;

import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private final OkHttpClient client       = new OkHttpClient();
    private static final String API_URL_PROFILE         = "https://catchmeifyoucan.xyz/distributed-best/api/get-info-user.php";
    private static final String API_URL_CHANGE_PASSWORD = "https://catchmeifyoucan.xyz/distributed-best/api/set-info.php";
    private static final String API_URL_BALANCE         = "https://catchmeifyoucan.xyz/distributed-best/api/balance.php";
    private static final MediaType JSON                 = MediaType.get("application/json; charset=utf-8");

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        sessionManager = new SessionManager(getApplicationContext());

        // Initialize binding
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnChangePassword.setOnClickListener(v -> {

            // Create layout programmatically
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            // New Password input
            final EditText newPasswordInput = new EditText(this);
            newPasswordInput.setHint("New Password");
            newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(newPasswordInput);

            // Confirm Password input
            final EditText confirmPasswordInput = new EditText(this);
            confirmPasswordInput.setHint("Confirm Password");
            confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(confirmPasswordInput);

            // Show AlertDialog
            new AlertDialog.Builder(this)
                    .setTitle("Change Password")
                    .setView(layout)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newPass = newPasswordInput.getText().toString();
                        String confirmPass = confirmPasswordInput.getText().toString();

                        if (newPass.isEmpty() || confirmPass.isEmpty()) {
                            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                        } else if (!newPass.equals(confirmPass)) {
                            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        }
                        else if ( newPass.length() < 6 ){
                            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show();
                        }
                        else {

                            // TODO: Save the new password securely

                            try {

                                JSONObject json = new JSONObject();
                                json.put("password", newPass);

                                RequestBody body = RequestBody.create(json.toString(), JSON);
                                Request request = new Request.Builder()
                                        .url(API_URL_CHANGE_PASSWORD)
                                        .patch(body)
                                        .addHeader("Content-Type", "application/json")
                                        .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                                        .build();


                                client.newCall(request).enqueue(new Callback() {

                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_SHORT).show();
                                        });
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {

                                        String responseBody = response.body().string();

                                        if (response.isSuccessful()) {
                                            try {

                                                JSONObject resJson          = new JSONObject(responseBody);
                                                String message = resJson.getString("message");
                                                runOnUiThread(() -> {
                                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                                });
                                            }
                                            catch (Exception e) {
                                                runOnUiThread(() ->{
                                                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                                });
                                                Log.e("Profile Activity", responseBody + " " + e.toString());
                                            }
                                        }
                                        else if (response.code() == 401) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                        else {
                                            runOnUiThread(() ->{
                                                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
                                            });
                                        }

                                    }
                                });

                            } catch (Exception e) {
                                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                                Log.e("Profile Activity", e.toString());
                            }

                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Handle Recharge click
        binding.btnRecharge.setOnClickListener(v -> {
            // Create layout
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            // Amount input
            final EditText amountInput = new EditText(this);
            amountInput.setHint("Amount");
            amountInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(amountInput);

            // Reference input
            final EditText referenceInput = new EditText(this);
            referenceInput.setHint("bKash Number");
            referenceInput.setInputType(InputType.TYPE_CLASS_TEXT);
            layout.addView(referenceInput);

            // Show dialog
            new AlertDialog.Builder(this)
                    .setTitle("Recharge Wallet")
                    .setView(layout)
                    .setPositiveButton("Recharge", (dialog, which) -> {
                        String amountStr = amountInput.getText().toString().trim();
                        String bkashStr = referenceInput.getText().toString().trim();

                        if (amountStr.isEmpty() || bkashStr.isEmpty()) {
                            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        }else if ( bkashStr.length() != 11) {
                            Toast.makeText(this, "Enter a valid bKash account number", Toast.LENGTH_SHORT).show();
                        } else {
                            // TODO: Process recharge with amount and reference
                            //Toast.makeText(this, "Recharge: $" + amountStr + "\nRef: " + refStr, Toast.LENGTH_SHORT).show();

                            try {

                                JSONObject json = new JSONObject();
                                json.put("amount", amountStr);

                                RequestBody body = RequestBody.create(json.toString(), JSON);
                                Request request = new Request.Builder()
                                        .url(API_URL_BALANCE)
                                        .patch(body)
                                        .addHeader("Content-Type", "application/json")
                                        .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                                        .build();


                                client.newCall(request).enqueue(new Callback() {

                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_SHORT).show();
                                        });
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {

                                        String responseBody = response.body().string();

                                        if (response.isSuccessful()) {
                                            try {

                                                JSONObject resJson          = new JSONObject(responseBody);
                                                String  message = resJson.getString("message");
                                                int     balance = resJson.getInt("balance");
                                                runOnUiThread(() -> {
                                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                                    binding.tvBalance.setText(balance + " ৳");
                                                });


                                            }
                                            catch (Exception e) {
                                                runOnUiThread(() ->{
                                                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                                });
                                                Log.e("Profile Activity", responseBody + " " + e.toString());
                                            }
                                        }
                                        else if (response.code() == 401) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                        else {
                                            runOnUiThread(() ->{
                                                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
                                            });
                                        }

                                    }
                                });

                            } catch (Exception e) {
                                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                                Log.e("Profile Activity", e.toString());
                            }

                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Handle Withdraw click
        binding.btnWithDraw.setOnClickListener(v -> {
            // Create layout
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            // Amount input
            final EditText amountInput = new EditText(this);
            amountInput.setHint("Amount");
            amountInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(amountInput);

            // bKash Number input
            final EditText bkashInput = new EditText(this);
            bkashInput.setHint("bKash Number");
            bkashInput.setInputType(InputType.TYPE_CLASS_PHONE);
            layout.addView(bkashInput);

            // Show dialog
            new AlertDialog.Builder(this)
                    .setTitle("Withdraw Funds")
                    .setView(layout)
                    .setPositiveButton("Withdraw", (dialog, which) -> {
                        String amountStr = amountInput.getText().toString().trim();
                        String bkashStr = bkashInput.getText().toString().trim();

                        if (amountStr.isEmpty() || bkashStr.isEmpty()) {
                            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        } else if ( bkashStr.length() != 11) {
                            Toast.makeText(this, "Enter a valid bKash account number", Toast.LENGTH_SHORT).show();
                        } else {
                            // TODO: Process withdrawal
                            //Toast.makeText(this, "Withdraw $" + amountStr + " to " + bkashStr, Toast.LENGTH_SHORT).show();

                            try {

                                JSONObject json = new JSONObject();
                                json.put("amount", "-" + amountStr);

                                RequestBody body = RequestBody.create(json.toString(), JSON);
                                Request request = new Request.Builder()
                                        .url(API_URL_BALANCE)
                                        .patch(body)
                                        .addHeader("Content-Type", "application/json")
                                        .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                                        .build();


                                client.newCall(request).enqueue(new Callback() {

                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_SHORT).show();
                                        });
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {

                                        String responseBody = response.body().string();

                                        if (response.isSuccessful()) {
                                            try {

                                                JSONObject resJson          = new JSONObject(responseBody);
                                                String  message = resJson.getString("message");
                                                int     balance = resJson.getInt("balance");
                                                runOnUiThread(() -> {
                                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                                    binding.tvBalance.setText(balance + " ৳");
                                                });
                                            }
                                            catch (Exception e) {
                                                runOnUiThread(() ->{
                                                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                                });
                                                Log.e("Profile Activity", responseBody + " " + e.toString());
                                            }
                                        }
                                        else if (response.code() == 401) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                        else {
                                            runOnUiThread(() ->{
                                                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
                                            });
                                        }

                                    }
                                });

                            } catch (Exception e) {
                                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                                Log.e("Profile Activity", e.toString());
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        getUserInfo();
    }

    private void getUserInfo(){
        try {

            Request request = new Request.Builder()
                    .url(API_URL_PROFILE)
                    .get()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + sessionManager.getToken())
                    .build();


            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responseBody = response.body().string();

                    if (response.isSuccessful()) {
                        try {

                            JSONObject resJson  = new JSONObject(responseBody);
                            String name         = resJson.getString("name");
                            String student_id   = resJson.getString("student_id");
                            String phone_number = resJson.getString("phone_number");
                            String balance      = resJson.getString("balance");
                            runOnUiThread(() -> {
                                binding.tvUserName.setText(name);
                                binding.tvStudentId.setText(student_id);
                                binding.tvEmail.setText(phone_number);
                                binding.tvBalance.setText(balance + " ৳");
                            });
                        }
                        catch (Exception e) {
                            runOnUiThread(() ->{
                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            });
                            Log.e("Profile Activity", responseBody + " " + e.toString());
                        }
                    }
                    else if (response.code() == 401) {
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
                        });
                    }
                    else {
                        runOnUiThread(() ->{
                            Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
                        });
                    }

                }
            });

        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            Log.e("Profile Activity", e.toString());
        }
    }


}