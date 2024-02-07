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

import com.example.absen_pegawai.enums.Role;
import com.example.absen_pegawai.helpers.InputValidation;
import com.example.absen_pegawai.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private TextInputEditText nikInput, passwordInput;
    private Button signUpBtn;
    private ProgressBar signUpProgress;
    private TextView signInLink;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    InputValidation validation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nikInput = findViewById(R.id.nikInput);
        passwordInput = findViewById(R.id.passwordInput);
        signUpBtn = findViewById(R.id.signUpButton);
        signUpProgress = findViewById(R.id.signUpProgress);
        signInLink = findViewById(R.id.signInLink);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpProcess();
            }
        });

        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSignIn();
            }
        });
    }

    void signUpProcess() {
        signUpProgress.setVisibility(View.VISIBLE);
        signUpBtn.setVisibility(View.GONE);

        String nik = nikInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        validation = new InputValidation();

        if(InputValidation.validateNik(nikInput) &&
                InputValidation.validatePassword(passwordInput)
        ) {
            User newUser = new User("Admin", nik, password, Role.ADMIN);

            Log.d(TAG, "Start Membuat User Baru");
            db.collection("Users")
                    .add(newUser)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "User baru berhasil ditambahkan, dengan ID : " + documentReference.getId());

                            showToast("Berhasil membuat User baru");

                            signUpProgress.setVisibility(View.GONE);
                            signUpBtn.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Terjadi kesalahan saat menambahkan user. Error : " + e.getMessage());

                            signUpProgress.setVisibility(View.GONE);
                            signUpBtn.setVisibility(View.VISIBLE);
                        }
                    });
            Log.d(TAG, "Selesai membuat user baru");
        }


    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
    }

    // Fungsi untuk memeriksa apakah sebuah string terdiri dari angka
    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    private void openSignIn() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}