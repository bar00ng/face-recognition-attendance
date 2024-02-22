package com.example.absen_pegawai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;

public class UserHomeFragment extends Fragment {
    private static final String TAG = "UserHomeFragment";

    public UserHomeFragment() {
        // Required empty public constructor
    }

    public static UserHomeFragment newInstance(String userId) {
        UserHomeFragment fragment = new UserHomeFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_user_home, container, false);

        TextView todayDateTextView = rootView.findViewById(R.id.todayDateTextView);
        generateTodayDate(todayDateTextView);

        TextView jamMasuk = rootView.findViewById(R.id.jamMasukTextView);
        TextView jamKeluar = rootView.findViewById(R.id.jamKeluarTextView);
        generateJamTextView(jamMasuk, jamKeluar);

        String userId = getArguments().getString("userId");

        Button checkOutButton = rootView.findViewById(R.id.checkOutButton);
        Button checkInButton = rootView.findViewById(R.id.checkInButton);
        checkAttendanceStatus(checkInButton, checkOutButton, userId);

        return rootView;
    }

    private void generateJamTextView(TextView jamMasukTextView, TextView jamKeluarTextView) {
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        int dayOfMonth = currentDate.getDayOfMonth();

        String monthInString = Month.of(month).name();

        String absenId = dayOfMonth + " " + monthInString + " " + year;

        String userId = getArguments().getString("userId");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference collectionReference = db.collection("Users")
                .document(userId).collection("data_absensi").document(absenId);

        collectionReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();

                        String jamMasuk = documentSnapshot.getString("jam_masuk");
                        String jamKeluar = documentSnapshot.getString("jam_keluar");

                        jamMasukTextView.setText(jamMasuk != null ? jamMasuk : "-");
                        jamKeluarTextView.setText(jamKeluar != null ? jamKeluar : "-");
                    }
                });
    }

    void openFaceRecognitionActivity(String userId, boolean isMasuk) {
        Intent intent = new Intent(getActivity(), FaceRecognitionActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("isMasuk", isMasuk);
        startActivity(intent);
    }

    void generateTodayDate(TextView textView) {
        LocalDate currentDate = LocalDate.now();

        int year = currentDate.getYear();
        int dayOfMonth = currentDate.getDayOfMonth();

        String monthInString = currentDate.getMonth().toString();

        monthInString = monthInString.substring(0, 1).toUpperCase() + monthInString.substring(1).toLowerCase();

        String formattedDate = String.format(Locale.getDefault(), "%02d %s %d", dayOfMonth, monthInString, year);

        textView.setText(formattedDate);
    }

    private void checkAttendanceStatus(Button checkInButton, Button checkOutButton, String userId) {
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        int dayOfMonth = currentDate.getDayOfMonth();

        String monthInString = Month.of(month).name();

        String absenId = dayOfMonth + " " + monthInString + " " + year;


        FirebaseFirestore db = FirebaseFirestore.getInstance();

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

                            String jamMasuk = documentSnapshot.getString("jam_masuk");
                            String jamKeluar = documentSnapshot.getString("jam_keluar");
                            if (jamMasuk == null && jamKeluar == null) {
                                checkInButton.setVisibility(View.VISIBLE);
                                checkInButton.setOnClickListener(listener -> {openFaceRecognitionActivity(userId, true);});
                            } else {
                                checkInButton.setVisibility(View.GONE);
                            }

                            if (jamKeluar == null && jamMasuk != null) {
                                checkOutButton.setVisibility(View.VISIBLE);
                                checkOutButton.setOnClickListener(listener -> {openFaceRecognitionActivity(userId, false);});
                            } else {
                                checkOutButton.setVisibility(View.GONE);
                            }
                        } else {
                            Log.w(TAG, "Terjadi kesalahan saat memeriksa absensi: " + task.getException().getMessage());
                            showToast("Gagal memeriksa absensi.");
                        }
                    }
                });
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
        toast.show();
    }
}