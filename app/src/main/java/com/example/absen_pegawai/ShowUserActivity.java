package com.example.absen_pegawai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.absen_pegawai.adapter.AttendanceAdapter;
import com.example.absen_pegawai.model.Attendance;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ShowUserActivity extends AppCompatActivity implements AttendanceAdapter.onAttendanceItemClickListener{
    private static final String TAG = "ShowUserActivity";
    private String documentId;
    TextView nikText, namaText;
    AttendanceAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);

        nikText = findViewById(R.id.nikTextView);
        namaText = findViewById(R.id.namaTextView);

        documentId = getIntent().getStringExtra("documentId");

        getUserInfo();
    }

    @Override
    public void onDeleteClick(DocumentSnapshot documentSnapshot, int position) {
        adapter.deleteAttendanceItem(position);

        Toast.makeText(this, "Berhasil menghapus data absensi.", Toast.LENGTH_LONG).show();

        Log.d(TAG, "Berhasil menghapus data absensi");
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
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, ListUserActivity.class);
        startActivity(intent);
        finish();
    }

    private void setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: Start");
        try{
            Query query = db.collection("Users")
                    .document(documentId)
                    .collection("data_absensi")
                    .orderBy("tgl_masuk", Query.Direction.ASCENDING)
                    .orderBy("bulan_masuk", Query.Direction.ASCENDING)
                    .orderBy("tahun_masuk", Query.Direction.ASCENDING);

            FirestoreRecyclerOptions<Attendance> opt = new FirestoreRecyclerOptions.Builder<Attendance>()
                    .setQuery(query, Attendance.class)
                    .build();

            adapter = new AttendanceAdapter(opt);

            RecyclerView recyclerView = findViewById(R.id.userAttendanceRecylerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            adapter.setOnAttendanceItemClickListener(ShowUserActivity.this);
        } catch (Exception e) {
            Log.w(TAG, "Error setup recyler view. Error : " + e.getMessage());
        }
        Log.d(TAG, "setUpRecyclerView: End");
    }



    private void getUserInfo() {
        db.collection("Users")
                .document(documentId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "Berhasil menemukan user");
                            String nik = documentSnapshot.getString("nik");
                            String nama = documentSnapshot.getString("nama");

                            nikText.setText(nik);
                            namaText.setText(nama);
                        } else {
                            Log.w(TAG, "User tidak ditemukan");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Gagal mencari user dengan ID : " + documentId);
                        Log.w(TAG, "Error Message : " + e.getMessage());
                    }
                });

        setUpRecyclerView();
    }
}