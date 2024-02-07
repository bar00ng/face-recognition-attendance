package com.example.absen_pegawai.helpers;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class InputValidation {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static CollectionReference reference= db.collection("Users");
    private static final String TAG = "InputValidation";

    public static boolean validateNik(TextInputEditText textInput) {
        return validateNik(textInput, false);
    }

    public static boolean validateNik(TextInputEditText textInput, boolean isRegister) {
        String nik = textInput.getText().toString().trim();

        if (TextUtils.isEmpty(nik)) {
            textInput.setError("NIK tidak boleh kosong");
            return false;
        } else if (nik.length() < 9 || nik.length() > 9) {
            textInput.setError("NIK harus terdiri dari 9 digit angka");
            return false;
        } else if (isRegister) {  // Hanya lakukan pengecekan jika isRegister true
            checkNikInFirestore(nik, textInput);
        } else {
            textInput.setError(null);
        }

        return true;
    }


    private static void checkNikInFirestore(String nik, TextInputEditText textInput) {
        reference.whereEqualTo("nik", nik)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // NIK sudah ada di Firestore
                            textInput.setError("NIK sudah terdaftar");
                        } else {
                            // NIK belum terdaftar
                            textInput.setError(null);
                        }
                    } else {
                        Log.d(TAG, "Gagal melakukan pencarian NIK");
                    }
                });
    }

    public static boolean validateNama(TextInputEditText textInput) {
        String nama = textInput.getText().toString().trim();

        if (TextUtils.isEmpty(nama)) {
            textInput.setError("Nama tidak boleh kosong");
            return false;
        } else if (nama.length() < 6) {
            textInput.setError("Nama harus lebih dari 6 karakter");
            return false;
        } else {
            textInput.setError(null);
            return true;
        }
    }

    public static boolean validatePassword(TextInputEditText textInput) {
        String password = textInput.getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            textInput.setError("Password tidak boleh kosong");
            return false;
        } else if (password.length() < 6) {
            textInput.setError("Password harus lebih dari 6 karakter");
            return false;
        } else {
            textInput.setError(null);
            return true;
        }
    }
}
