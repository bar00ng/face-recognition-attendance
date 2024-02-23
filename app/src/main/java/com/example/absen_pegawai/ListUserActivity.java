package com.example.absen_pegawai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.absen_pegawai.adapter.UserAdapter;
import com.example.absen_pegawai.enums.Role;
import com.example.absen_pegawai.helpers.Report;
import com.example.absen_pegawai.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ListUserActivity extends AppCompatActivity implements UserAdapter.OnItemClickListener {
    private static final String TAG = "ListUserActivity";
    private static final int STORAGE_PERMISSION_CODE = 23;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersCollection = db.collection("Users");
    private UserAdapter adapter;

    FloatingActionButton createUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);

        createUser = findViewById(R.id.createUser);

        createUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open Add User Activity
                openCreateUserActivity();
            }
        });

        setUpRecyclerView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestForStoragePermissions();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestForStoragePermissions();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu:
                logout();
                return true;
            case R.id.downloadReport:
                downloadReport();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
        openShowUserActivity(documentSnapshot.getId());
    }

    @Override
    public void onDeleteClick(DocumentSnapshot documentSnapshot, int position) {
        adapter.deleteItem(position);

        Toast.makeText(this, "Berhasil menghapus user.", Toast.LENGTH_LONG).show();

        Log.d(TAG, "Berhasil menghapus user");
    }

    @Override
    public void onUpdatePasswordClick(DocumentSnapshot documentSnapshot, int position) {
        openUpdateUserPasswordActivity(documentSnapshot.getId());
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0){
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if(read && write){
                    Toast.makeText(ListUserActivity.this, "Storage Permissions Granted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ListUserActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void downloadReport() {
        Report report = new Report(this);
        report.generateAbsensiReport();
    }

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }else{
            //Below android 11
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>(){

                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                                //Android is 11 (R) or above
                                if(Environment.isExternalStorageManager()){
                                    //Manage External Storage Permissions Granted
                                    Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted");
                                }else{
                                    Toast.makeText(ListUserActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                //Below android 11

                            }
                        }
                    });

    void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void openCreateUserActivity() {
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivity(intent);
    }

    private void setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: Start");
        try{
            Query query = usersCollection.whereEqualTo("role", Role.USER)
                    .orderBy("nama", Query.Direction.ASCENDING);

            FirestoreRecyclerOptions<User> opt = new FirestoreRecyclerOptions.Builder<User>()
                    .setQuery(query, User.class)
                    .build();

            adapter = new UserAdapter(opt);

            RecyclerView recyclerView = findViewById(R.id.userRecylerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            adapter.setOnItemClickListener(ListUserActivity.this);
        } catch (Exception e) {
            Log.w(TAG, "Error setup recyler view. Error : " + e.getMessage());
        }
        Log.d(TAG, "setUpRecyclerView: End");
    }

    private void openShowUserActivity(String id) {
        Intent intent = new Intent(this, ShowUserActivity.class);
        intent.putExtra("documentId", id);
        startActivity(intent);
    }

    private void openUpdateUserPasswordActivity(String id) {
        Intent intent = new Intent(this, UpdateUserPasswordActivity.class);
        intent.putExtra("documentId", id);
        startActivity(intent);
    }
}