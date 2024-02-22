package com.example.absen_pegawai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.absen_pegawai.enums.Role;
import com.example.absen_pegawai.helpers.InputValidation;
import com.example.absen_pegawai.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateUserActivity extends AppCompatActivity{
    private static final String TAG = "CreateUserActivity";
    TextInputEditText nameInput, nikInput, passwordInput;
    ProgressBar progressBar;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        nameInput = findViewById(R.id.nameInput);
        nikInput = findViewById(R.id.nikInput);
        passwordInput = findViewById(R.id.passwordInput);
        progressBar = findViewById(R.id.progressBar);
        submitBtn = findViewById(R.id.createBtn);


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nik = nikInput.getText().toString().trim();
                String nama = nameInput.getText().toString().trim();
                String pwd = passwordInput.getText().toString().trim();

                // Validasi Input
                if(InputValidation.validateNama(nameInput) &&
                        InputValidation.validateNik(nikInput, true) &&
                        InputValidation.validatePassword(passwordInput)
                ) {
                    User newUser = new User(nama, nik, pwd, Role.USER);

                    addNewUser(newUser);
                }
            }
        });
    }

    // Function tambah user baru
    private void addNewUser(User user) {
        toggleProgressBar();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Berhasil menambahkan pegawai baru");
                        String userId = documentReference.getId();

                        toggleProgressBar();

                        redirectToScanFaceActivity(userId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error menambahkan user baru. Error Msg : " + e.getMessage());
                    }
                });
    }

    private void redirectToScanFaceActivity(String userId) {
        Intent intent = new Intent(this, ScanUserFaceActivity.class);
        intent.putExtra("userId", userId);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, ListUserActivity.class);
        startActivity(intent);
        finish();
    }
}