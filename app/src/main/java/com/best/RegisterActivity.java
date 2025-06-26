package com.best;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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

public class RegisterActivity extends AppCompatActivity {



    private TextInputLayout tilUsername;
    private TextInputLayout tilEmail;
    private TextInputLayout tilStudentId;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etUsername;
    private TextInputEditText etPhoneNumber;
    private TextInputEditText etStudentId;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnSignup;
    private TextView tvLogin;
    private CircularProgressIndicator progressIndicator;

    private final OkHttpClient client        = new OkHttpClient();
    private static final String REGISTER_URL = "https://catchmeifyoucan.xyz/distributed-best/api/register.php";
    private static final MediaType JSON      = MediaType.get("application/json; charset=utf-8");

    private SessionManager sessionManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        sessionManager = new SessionManager(this);

        initViews();
        setClickListeners();
    }

    private void initViews() {
        tilUsername         = findViewById(R.id.tilUsername);
        tilEmail            = findViewById(R.id.tilEmail);
        tilStudentId        = findViewById(R.id.tilStudentId);
        tilPassword         = findViewById(R.id.tilPassword);
        tilConfirmPassword  = findViewById(R.id.tilConfirmPassword);

        etUsername          = findViewById(R.id.etUsername);
        etPhoneNumber = findViewById(R.id.etEmail);
        etStudentId         = findViewById(R.id.etStudentId);
        etPassword          = findViewById(R.id.etPassword);
        etConfirmPassword   = findViewById(R.id.etConfirmPassword);

        btnSignup           = findViewById(R.id.btnSignup);
        tvLogin             = findViewById(R.id.tvLogin);
        progressIndicator   = findViewById(R.id.progressIndicatorSignUp);
    }

    private void setClickListeners() {
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSignup();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to login
                finish();
            }
        });
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return Patterns.PHONE.matcher(phoneNumber).matches();
    }

    private void validateAndSignup() {

        // Reset errors
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilStudentId.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String username         = etUsername.getText().toString().trim();
        String phoneNumber      = etPhoneNumber.getText().toString().trim();
        String studentId        = etStudentId.getText().toString().trim();
        String password         = etPassword.getText().toString().trim();
        String confirmPassword  = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            return;
        }
        else if (username.length() < 3) {
            tilUsername.setError("Username must be at least 3 characters");
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            tilEmail.setError("Phone number is required");
            return;
        }
        else if (!isValidPhoneNumber(phoneNumber)) {
            tilEmail.setError("Enter a valid phone number");
            return;
        }

        if (TextUtils.isEmpty(studentId)) {
            tilStudentId.setError("Student ID is required");
            return;
        }
        else if (studentId.length() != 11) {
            tilStudentId.setError("Student ID must be 11 digits");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            return;
        }
        else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Confirm password is required");
            return;
        }
        else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            return;
        }

        progressIndicator.setVisibility(View.VISIBLE);
        try {
            JSONObject json = new JSONObject();
            json.put("name", username);
            json.put("phone_number", phoneNumber);
            json.put("password", password);
            json.put("student_id", studentId);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(REGISTER_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->{
                        progressIndicator.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    runOnUiThread(()->progressIndicator.setVisibility(View.INVISIBLE));
                    String res = response.body().string();

                    if (response.isSuccessful()) {
                        try {
                            JSONObject resJson      = new JSONObject(res);
                            int user_id  = resJson.getInt("user_id");
                            String token            = resJson.getString("token");
                            long   tokenExpiresAt   = resJson.getLong("tokenExpiresAt");
                            sessionManager.saveToken(user_id,token, tokenExpiresAt);

                            runOnUiThread(() -> {
                                Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() ->{
                                Toast.makeText(RegisterActivity.this, "Unexpected response", Toast.LENGTH_SHORT).show();
                                Log.e("RegisterActivity", "Unexpected response "+ e.getMessage());
                            });
                        }
                    } else if (response.code() == 409) {
                        runOnUiThread(() ->{
                            Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (Exception e) {
            runOnUiThread(()->progressIndicator.setVisibility(View.INVISIBLE));
            Toast.makeText(this, "Error building request", Toast.LENGTH_SHORT).show();
            Log.e("RegisterActivity", "Error building request " + e.getMessage());
        }
    }




}
