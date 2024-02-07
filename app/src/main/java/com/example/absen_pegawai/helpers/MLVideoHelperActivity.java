package com.example.absen_pegawai.helpers;

import static androidx.camera.view.PreviewView.StreamState;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.example.absen_pegawai.R;
import com.example.absen_pegawai.helpers.vision.GraphicOverlay;
import com.example.absen_pegawai.helpers.vision.VisionBaseProcessor;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class MLVideoHelperActivity extends AppCompatActivity{
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_LOCATION = 1002;

    protected PreviewView previewView;
    protected GraphicOverlay graphicOverlay;
    private TextView outputTextView;
    private ExtendedFloatingActionButton addFaceButton;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Executor executor = Executors.newSingleThreadExecutor();

    private VisionBaseProcessor processor;
    private ImageAnalysis imageAnalysis;
    FusedLocationProviderClient fusedLocationProviderClient;
    public List<Address> addresses;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);

        previewView = findViewById(R.id.camera_source_preview);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        addFaceButton = findViewById(R.id.button_add_face);

        cameraProviderFuture = ProcessCameraProvider.getInstance(getApplicationContext());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        processor = setProcessor();

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
            initSource();
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            getLastLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (processor != null) {
            processor.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin kamera diberikan, lanjutkan dengan logika aplikasi
                initSource();
            } else {
                // Izin kamera tidak diberikan, tampilkan pesan informasi
                showPermissionDeniedDialog("Aplikasi memerlukan izin kamera untuk fitur tertentu.");
            }
        } else if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin lokasi diberikan, lanjutkan dengan logika aplikasi
                getLastLocation();
            } else {
                // Izin lokasi tidak diberikan, tampilkan pesan informasi
                showPermissionDeniedDialog("Aplikasi memerlukan izin lokasi untuk fitur tertentu.");
            }
        }

    }

    private void showPermissionDeniedDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Pengaturan", (dialog, which) -> openAppSettings())
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    protected void setOutputText(String text) {
        outputTextView.setText(text);
    }

    private void initSource() {

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getApplicationContext()));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        int lensFacing = getLensFacing();
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();

        setFaceDetector(lensFacing);
        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    /**
     * The face detector provides face bounds whose coordinates, width and height depend on the
     * preview's width and height, which is guaranteed to be available after the preview starts
     * streaming.
     */
    private void setFaceDetector(int lensFacing) {
        previewView.getPreviewStreamState().observe(this, new Observer<StreamState>() {
            @Override
            public void onChanged(StreamState streamState) {
                if (streamState != StreamState.STREAMING) {
                    return;
                }

                View preview = previewView.getChildAt(0);
                float width = preview.getWidth() * preview.getScaleX();
                float height = preview.getHeight() * preview.getScaleY();
                float rotation = preview.getDisplay().getRotation();
                if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                    float temp = width;
                    width = height;
                    height = temp;
                }

                imageAnalysis.setAnalyzer(
                        executor,
                        createFaceDetector((int) width, (int) height, lensFacing)
                );
                previewView.getPreviewStreamState().removeObserver(this);
            }
        });
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private ImageAnalysis.Analyzer createFaceDetector(int width, int height, int lensFacing) {
        graphicOverlay.setPreviewProperties(width, height, lensFacing);
        return imageProxy -> {
            if (imageProxy.getImage() == null) {
                imageProxy.close();
                return;
            }
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            // converting from YUV format
            processor.detectInImage(imageProxy, toBitmap(imageProxy.getImage()), rotationDegrees);
            // after done, release the ImageProxy object
            imageProxy.close();
        };
    }

    private Bitmap toBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    protected int getLensFacing() {
        return CameraSelector.LENS_FACING_FRONT;
    }

    protected abstract VisionBaseProcessor setProcessor();

    public void makeAddFaceVisible() {
        addFaceButton.setVisibility(View.VISIBLE);
    }

    public void onAddFaceClicked(View view) {

    }

    public void getLastLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Geocoder geocoder = new Geocoder(
                                MLVideoHelperActivity.this,
                                        Locale.getDefault()
                                );

                                try {
                                    addresses = geocoder.getFromLocation(
                                            location.getLatitude(),
                                            location.getLongitude(),
                                            1
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });

        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }
}
