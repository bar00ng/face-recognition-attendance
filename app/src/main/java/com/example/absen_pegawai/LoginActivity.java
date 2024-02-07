package com.example.absen_pegawai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.absen_pegawai.enums.Role;
import com.example.absen_pegawai.helpers.InputValidation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.Month;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextInputEditText nikInput, passwordInput;
    private Button loginBtn;
    private ProgressBar loginProgress;
//    private TextView signUpLink;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nikInput = findViewById(R.id.nikInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.signInButton);
        loginProgress = findViewById(R.id.loginProgress);
//        signUpLink = findViewById(R.id.signUpLink);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginProcess();
            }
        });

//        signUpLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                openSignUp();
//            }
//        });
    }

    private void loginProcess() {
        String nik = nikInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if(InputValidation.validateNik(nikInput) &&
                InputValidation.validatePassword(passwordInput)
        ) {
            toggleProgressBar();

            Log.d(TAG, "Start proses login");

            db.collection("Users")
                    .whereEqualTo("nik", nik)
                    .whereEqualTo("password", password)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();

                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                    String roleName = document.getString("role");
                                    String userId = document.getId();

                                    Log.d(TAG, "Proses login berhasil. Role : " + roleName);

                                    if (roleName.equals(Role.ADMIN)) {
                                        openListUserActivity(userId);
                                    } else if (roleName.equals(Role.USER)) {
                                        checkAttendanceStatus(userId);
                                    }
                                } else {
                                    Log.w(TAG, "Dokumen tidak ditemukan");

                                    showToast("Username atau Password salah.");
                                }

                                toggleProgressBar();
                            } else {
                                Log.w(TAG, "Terjadi kesalah, Error: " + task.getException().getMessage());

                                showToast("Proses login gagal");

                                toggleProgressBar();
                            }
                        }
                    });

            Log.d(TAG, "Selesai proses login");
        }
    }

    private void openListUserActivity(String userId) {
        Intent intent = new Intent(this, ListUserActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    private void openFaceRecognitionActivity(String userId) {
        Intent intent = new Intent(this, FaceRecognitionActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
    }

//    private void openSignUp() {
//        Intent intent = new Intent(this, SignUpActivity.class);
//        startActivity(intent);
//    }

    private void checkAttendanceStatus(String userId) {
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        int dayOfMonth = currentDate.getDayOfMonth();

        String monthInString = Month.of(month).name();

        String absenId = dayOfMonth + " " + monthInString + " " + year;

        // Mengecek apakah dokumen absensi sudah ada untuk hari ini
        db.collection("Users")
                .document(userId)
                .collection("data_absensi")
                .document(absenId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();

                            if (documentSnapshot.exists()) {
                                showToast("Kamu sudah absen hari ini");
                            } else {
                                openFaceRecognitionActivity(userId);
                            }
                        } else {
                            Log.w(TAG, "Terjadi kesalahan saat memeriksa absensi: " + task.getException().getMessage());
                            showToast("Gagal memeriksa absensi.");
                        }
                    }
                });
    }

    private void toggleProgressBar() {
        if (loginProgress.getVisibility() == View.VISIBLE) {
            loginProgress.setVisibility(View.GONE);
            loginBtn.setVisibility(View.VISIBLE);
        } else {
            loginProgress.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.GONE);
        }
    }
}