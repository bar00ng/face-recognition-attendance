package com.example.absen_pegawai;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.absen_pegawai.adapter.AttendanceAdapter;
import com.example.absen_pegawai.model.Attendance;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
public class UserHistoryFragment extends Fragment {
    private static final String TAG = "UserHistoryFragment";
    private AttendanceAdapter adapter;
    public UserHistoryFragment() {
    }

    public static UserHistoryFragment newInstance(String userId) {
        UserHistoryFragment fragment = new UserHistoryFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_history, container, false);

        String userId = getArguments().getString("userId");
        RecyclerView recyclerView = rootView.findViewById(R.id.userAttendanceHistoryRecylerView);
        setupRecyclerView(userId, recyclerView);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    void setupRecyclerView(String userId, RecyclerView recyclerView) {
        Log.d(TAG, "setUpRecyclerView: Start");
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Query query = db.collection("Users")
                    .document(userId)
                    .collection("data_absensi")
                    .orderBy("tgl_masuk", Query.Direction.ASCENDING)
                    .orderBy("bulan_masuk", Query.Direction.ASCENDING)
                    .orderBy("tahun_masuk", Query.Direction.ASCENDING);

            FirestoreRecyclerOptions<Attendance> opt = new FirestoreRecyclerOptions.Builder<Attendance>()
                    .setQuery(query, Attendance.class)
                    .build();

            adapter = new AttendanceAdapter(opt);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage(), e);
            // Optionally, handle the error gracefully, e.g., show a toast message or display an error view
            // For logging, it's better to use Log.e for error level logs.
        } finally {
            Log.d(TAG, "setUpRecyclerView: End");
        }
    }
}