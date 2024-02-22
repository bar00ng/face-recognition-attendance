package com.example.absen_pegawai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.absen_pegawai.enums.Office;
import com.example.absen_pegawai.helpers.MLVideoHelperActivity;
import com.example.absen_pegawai.helpers.vision.VisionBaseProcessor;
import com.example.absen_pegawai.helpers.vision.recogniser.FaceRecognitionProcessor;
import com.example.absen_pegawai.model.Attendance;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.face.Face;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class FaceRecognitionActivity extends MLVideoHelperActivity implements FaceRecognitionProcessor.FaceRecognitionCallback {
    private static final String TAG = "FaceRecognitionActivity";
    private Interpreter faceNetInterpreter;
    private FaceRecognitionProcessor faceRecognitionProcessor;

    private Face face;
    private Bitmap faceBitmap;
    private float[] faceVector;

    private String userId;
    private boolean isMasuk;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onStart() {
        super.onStart();

        userId = getIntent().getStringExtra("userId");
        isMasuk = getIntent().getBooleanExtra("isMasuk", false);
    }

    @Override
    protected VisionBaseProcessor setProcessor() {
        try {
            faceNetInterpreter = new Interpreter(FileUtil.loadMappedFile(this, "mobile_face_net.tflite"), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }

        faceRecognitionProcessor = new FaceRecognitionProcessor(
                faceNetInterpreter,
                graphicOverlay,
                this
        );
        faceRecognitionProcessor.activity = this;
        return faceRecognitionProcessor;
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

    @Override
    public void onFaceDetected(Face face, Bitmap faceBitmap, float[] faceVector) {
        this.face = face;
        this.faceBitmap = faceBitmap;
        this.faceVector = faceVector;
    }

    @Override
    public void onFaceRecognised(Face face, float probability, String name) {
        if(Objects.equals(name, userId)){
            getLastLocation();

            double longitudeNow = addresses.get(0).getLongitude();
            double latitudeNow = addresses.get(0).getLatitude();
            String addresNow = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String country = addresses.get(0).getCountryName();

            double officeLongitude = Office.LONGITUDE;
            double officeLatitude = Office.LATITUDE;

            Log.d(TAG, "ALAMAT - " + addresNow);

            double distanceThreshold = Office.DISTANCE_TRESHOLD;

            double distanceToOffice = calculateDistance(latitudeNow, longitudeNow, officeLatitude, officeLongitude);

            if (distanceToOffice <= distanceThreshold) {
                checkAttendance(longitudeNow, latitudeNow, addresNow, city, country);
            } else {
                showToast("Kamu berada di luar kantor");
            }

        } else {
            showToast("Kamu bukan orang yang sama");
        }
    }

    public void setTestImage(Bitmap cropToBBox) {
        if (cropToBBox == null) {
            return;
        }
//        runOnUiThread(() -> ((ImageView) findViewById(R.id.testImageView)).setImageBitmap(cropToBBox));
    }

    void checkAttendance(
            double longitude, double latitude,
            String addressNow, String city,
            String country
    ){
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        int dayOfMonth = currentDate.getDayOfMonth();

        String monthInString = Month.of(month).name();

        String absenId = dayOfMonth + " " + monthInString + " " + year;
        String timeNow = getCurrentTimeIn24HourFormat();

        String collectionName = isMasuk ? "data_absensi_masuk" : "data_absensi_keluar";

        // Mengecek apakah dokumen absensi sudah ada untuk hari ini
        db.collection("Users")
                .document(userId)
                .collection(collectionName)
                .document(absenId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();

                            if (!documentSnapshot.exists()) {
                                insertAttendance(
                                        dayOfMonth, month,
                                        year, longitude,
                                        latitude,
                                        timeNow, absenId
                                );
                            }
                        } else {
                            Log.w(TAG, "Terjadi kesalahan saat memeriksa absensi: " + task.getException().getMessage());
                            showToast("Gagal memeriksa absensi.");
                        }
                    }
                });
    };

    void insertAttendance(
            int tgl, int bulan,
            int tahun, double longitude,
            double latitude,
            String jam, String absenId
    ) {
        String fieldPrefix = isMasuk ? "_masuk" : "_keluar";

        Map<String, Object> absensi = new HashMap<>();
        absensi.put("longitude" + fieldPrefix, longitude);
        absensi.put("latitude" + fieldPrefix, latitude);
        absensi.put("jam" + fieldPrefix, jam);
        absensi.put("tgl" + fieldPrefix, tgl);
        absensi.put("bulan" + fieldPrefix, bulan);
        absensi.put("tahun" + fieldPrefix, tahun);

        // Perform the Firestore operation
        DocumentReference absensiRef = db.collection("Users").document(userId)
                .collection("data_absensi").document(absenId);

        absensiRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, update its data
                        absensiRef.update(absensi)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            openLoginActivity();
                                            showToast("Data absen berhasil disimpan");
                                        } else {
                                            showToast("Gagal menyimpan data absen");
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle failure
                                        System.err.println("Error saat mencatat waktu masuk ke Firestore: " + e.getMessage());
                                    }
                                });
                    } else {
                        // Document doesn't exist, set its data
                        absensiRef.set(absensi)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            openLoginActivity();
                                            showToast("Data absen berhasil disimpan");
                                        } else {
                                            showToast("Gagal menyimpan data absen");
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle failure
                                        System.err.println("Error saat mencatat waktu masuk ke Firestore: " + e.getMessage());
                                    }
                                });
                    }
                } else {
                    // Handle failure
                    showToast("Gagal memeriksa dokumen absensi");
                    Log.e(TAG, "Error getting document", task.getException());
                }
            }
        });

    }

    void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private String getCurrentTimeIn24HourFormat() {
        // Create a Calendar instance to get the current time
        Calendar calendar = Calendar.getInstance();

        // Create a SimpleDateFormat with the desired 24-hour format
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Format the current time using the SimpleDateFormat
        return dateFormat.format(calendar.getTime());
    }

    // Metode untuk menghitung jarak antara dua titik koordinat menggunakan Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Radius bumi dalam meter
        final double R = 6371000;

        // Konversi dari derajat ke radian
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Selisih koordinat
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Jarak dalam meter
        return R * c;
    }

    void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}