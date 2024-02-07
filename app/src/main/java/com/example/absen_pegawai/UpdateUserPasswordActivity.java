package com.example.absen_pegawai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.absen_pegawai.helpers.InputValidation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateUserPasswordActivity extends AppCompatActivity {
    public static final String TAG = "UpdateUserPasswordActivity";
    private String userId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView currentUser;
    TextInputEditText passwordInput;
    ProgressBar progressBar;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_password);

        userId = getIntent().getStringExtra("documentId");
        currentUser = findViewById(R.id.currentUser);
        passwordInput = findViewById(R.id.passwordInput);
        progressBar = findViewById(R.id.progressBar);
        submitBtn = findViewById(R.id.createBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (InputValidation.validatePassword(passwordInput)) {
                    updatePassword();
                }
            }
        });

        getUserInfo();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, ListUserActivity.class);
        startActivity(intent);
        finish();
    }

    private void getUserInfo() {
        db.collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "Berhasil menemukan user");
                            String nama = documentSnapshot.getString("nama");

                            currentUser.setText("Current User\t: " + nama);
                        } else {
                            Log.w(TAG, "User tidak ditemukan");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Gagal mencari user dengan ID : " + userId);
                        Log.w(TAG, "Error Message : " + e.getMessage());
                    }
                });
    }

    private void updatePassword() {
        if (userId != null) {
            toggleProgressBar();

            String newPassword = passwordInput.getText().toString().trim();

            Map<String, Object> user = new HashMap<>();
            user.put("password", newPassword);

            db.collection("Users")
                    .document(userId)
                    .update(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Berhasil mengupdate password");

                                openListUserActivity();

                                showToast("Password berhasil diperbaharui");
                            } else {
                                Log.w(TAG, "Gagal mengupdate password");
                                showToast("Gagal memperbaharui password");
                            }

                            toggleProgressBar();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error : " + e.getMessage());
                        }
                    });
        } else {
            showToast("Terjadi kesalahan saat mengupdate password");

            Log.d(TAG, "userId kosoong");
        }
    }

    private void openListUserActivity() {
        Intent intent = new Intent(this, ListUserActivity.class);
        startActivity(intent);
        finish();
    }

    private void toggleProgressBar() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
            submitBtn.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            submitBtn.setVisibility(View.GONE);
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}