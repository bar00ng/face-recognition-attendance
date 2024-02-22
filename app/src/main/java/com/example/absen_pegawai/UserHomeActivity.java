package com.example.absen_pegawai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.absen_pegawai.databinding.ActivityUserHomeBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserHomeActivity extends AppCompatActivity {
    ActivityUserHomeBinding binding;

    private static final String TAG = "UserHomeActivity";
    private String userId;
    TextView nameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getIntent().getStringExtra("userId");
        nameTextView = findViewById(R.id.userName);

        generateUserName();

        replaceFragment(UserHomeFragment.newInstance(userId));
        setupBottomNav();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu:
                openLoginActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setupBottomNav() {
        binding.bottomNavigationMenu.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.homeMenu:
                    replaceFragment(UserHomeFragment.newInstance(userId));
                    break;
                case R.id.historyMenu:
                    replaceFragment(UserHistoryFragment.newInstance(userId));
                    break;
            }

            return true;
        });
    }

    void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    void generateUserName() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "Document found!");
                            String nama = document.getString("nama");

                            nameTextView.setText("Hello, " + nama + "!");
                        } else {
                            Log.w(TAG, "No such document");
                        }
                    } else {
                        Log.w(TAG, "get failed with ", task.getException());
                    }
                })
                .addOnFailureListener(e ->{
                    Log.e(TAG, "Error getting document", e);
                });
    }
}