package com.example.absen_pegawai;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.absen_pegawai.helpers.CreateUserHelperActivity;
import com.example.absen_pegawai.helpers.vision.VisionBaseProcessor;
import com.example.absen_pegawai.helpers.vision.recogniser.ScanUserFaceProcessor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.face.Face;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;

public class ScanUserFaceActivity extends CreateUserHelperActivity implements ScanUserFaceProcessor.ScanUserFaceCallback {
    private static final String TAG = "ScanUserFaceActivity";
    private Interpreter faceNetInterpreter;
    private ScanUserFaceProcessor scanUserFaceProcessor;

    private Face face;
    private Bitmap faceBitmap;
    private float[] faceVector;
    private String userId;

    @Override
    protected void onStart() {
        super.onStart();

        userId = getIntent().getStringExtra("userId");
    }
    @Override
    protected VisionBaseProcessor setProcessor() {
        try {
            faceNetInterpreter = new Interpreter(FileUtil.loadMappedFile(this, "mobile_face_net.tflite"), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }

        scanUserFaceProcessor = new ScanUserFaceProcessor(
                faceNetInterpreter,
                graphicOverlay,
                this
        );
        scanUserFaceProcessor.activity = this;
        return scanUserFaceProcessor;
    }

    @Override
    public void onFaceDetected(Face face, Bitmap faceBitmap, float[] faceVector) {
        this.face = face;
        this.faceBitmap = faceBitmap;
        this.faceVector = faceVector;
    }

    @Override
    public void onFaceRecognised(Face face, float probability, String name) {
        // Do Nothing
    }

    public void onCancelBtnClick(View v) {
        deleteUser();
    }

    private void openListUserActivity() {
        Intent intent = new Intent(this, ListUserActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        deleteUser();
    }

    private void deleteUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Berhasil membatalkan create user, dengan ID : " + userId);
                        openListUserActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Terjadi kesalahan saat membatalkan proses membuat user");
                    }
                });
    }

    @Override
    public void saveData(View view) {
        super.saveData(view);

        if (face == null || faceBitmap == null) {
            showToast("Tidak ada wajah yang terdeteksi");
            return;
        }

        Face tempFace = face;
        Bitmap tempBitmap = faceBitmap;
        float[] tempVector = faceVector;
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.add_face_dialog, null);
        ((ImageView) dialogView.findViewById(R.id.dlg_image)).setImageBitmap(tempBitmap);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                scanUserFaceProcessor.registerFace(userId, tempVector);

                showToast("User Berhasil di simpan");

                openListUserActivity();
            }
        });
        builder.show();
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}