package com.best;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword;
    private TextView tvSignUp;
    private CircularProgressIndicator progressIndicator;

    private SessionManager sessionManager;

    private final OkHttpClient client       = new OkHttpClient();
    private static final String LOGIN_URL   = "https://catchmeifyoucan.xyz/distributed-best/api/login.php";
    private static final MediaType JSON     = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        sessionManager = new SessionManager(this);
        if ( sessionManager.getToken() != null ){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        initViews();
        setClickListeners();
    }

    private void initViews() {
        tilEmail            = findViewById(R.id.tilEmail);
        tilPassword         = findViewById(R.id.tilPassword);
        etEmail             = findViewById(R.id.etEmail);
        etPassword          = findViewById(R.id.etPassword);
        btnLogin            = findViewById(R.id.btnLogin);
        tvForgotPassword    = findViewById(R.id.tvForgotPassword);
        tvSignUp            = findViewById(R.id.tvSignUp);
        progressIndicator   = findViewById(R.id.progressIndicator);
    }

    private void setClickListeners() {

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndLogin();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement forgot password functionality
                Toast.makeText(LoginActivity.this, "Forgot password clicked", Toast.LENGTH_SHORT).show();
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to sign up screen
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return Patterns.PHONE.matcher(phoneNumber).matches();
    }

    private void validateAndLogin() {

        // Reset errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        String phone_number    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phone_number)) {
            tilEmail.setError("Phone number is required");
            return;
        } else if (!isValidPhoneNumber(phone_number)) {
            tilEmail.setError("Enter a valid phone number");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            return;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            return;
        }

        progressIndicator.setVisibility(View.VISIBLE);

        try {

            JSONObject json = new JSONObject();
            json.put("phone_number", phone_number);
            json.put("password", password);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(LOGIN_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();


            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressIndicator.setVisibility(View.INVISIBLE);
                        Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                        Log.e("LoginActivity", "Network error", e);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    runOnUiThread(()->progressIndicator.setVisibility(View.INVISIBLE));
                    String responseBody = response.body().string();

                    if (response.isSuccessful()) {
                        try {

                            JSONObject resJson          = new JSONObject(responseBody);
                            int         user_id         = resJson.getInt("user_id");
                            String      token           = resJson.getString("token");
                            long        tokenExpiresAt  = resJson.getLong("tokenExpiresAt");
                            sessionManager.saveToken(user_id, token, tokenExpiresAt);

                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            });
                        }
                        catch (Exception e) {
                            runOnUiThread(() ->{
                                Toast.makeText(LoginActivity.this, "Invalid response", Toast.LENGTH_SHORT).show();
                                Log.e("LoginActivity", "Parse error", e);
                            });

                        }
                    }
                    else if (response.code() == 401) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        });
                    }
                    else {
                        runOnUiThread(() ->{
                            Toast.makeText(LoginActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        });
                    }

                }
            });

        } catch (Exception e) {
            runOnUiThread(()->progressIndicator.setVisibility(View.INVISIBLE));
            Toast.makeText(this, "Error building request", Toast.LENGTH_SHORT).show();
            Log.e("LoginActivity", "JSON error", e);
        }

    }


}