package com.example.absen_pegawai.helpers;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.absen_pegawai.enums.Role;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public class Report {
    private static final String TAG = "ReportUtils";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static CollectionReference userRef = db.collection("Users");

    private Context context;

    public Report(Context context) {
        this.context = context;
    }

    public void generateAbsensiReport() {
        LocalDate today = LocalDate.now();
        int daysInMonth = today.lengthOfMonth();

        int year = today.getYear();
        int month = today.getMonthValue();
        String monthInString = Month.of(month).name();
        String sheetName = monthInString + "-" + year;

        HashMap<String, HashMap<String, Integer>> dataAbsensiPerUser = new HashMap<>();

        userRef.whereNotEqualTo("role", Role.ADMIN)
                .get().addOnCompleteListener(userTask -> {
           if (userTask.isSuccessful()) {
               for (QueryDocumentSnapshot userDoc : userTask.getResult()) {
                   String namaPegawai = userDoc.getString("nama");
                   final int[] totalAbsensi = {0};

                   CollectionReference absensiCollection = userDoc.getReference()
                           .collection("data_absensi");

                   absensiCollection.get().addOnCompleteListener(absensiTask -> {
                       if (absensiTask.isSuccessful()) {
                           // Iterasi melalui setiap dokumen absensi
                           for (QueryDocumentSnapshot absensiDoc : absensiTask.getResult()) {
                               int bulanMasuk = absensiDoc.getLong("bulan_masuk").intValue();
                               int tahunMasuk = absensiDoc.getLong("tahun_masuk").intValue();

                               // Periksa apakah bulan dan tahun absensi sama dengan bulan dan tahun saat ini
                               if (bulanMasuk == month && tahunMasuk == year) {
                                   totalAbsensi[0]++;
                               }
                           }
                           HashMap<String, Integer> absensiPengguna = dataAbsensiPerUser.getOrDefault(namaPegawai, new HashMap<>());
                           absensiPengguna.put(monthInString, totalAbsensi[0]);
                           dataAbsensiPerUser.put(namaPegawai, absensiPengguna);
                       } else {
                           Log.e(TAG, "Error getting absensi documents: \"" + absensiTask.getException());
                       }

                       // Jika semua dokumen pengguna sudah diiterasi, buat file Excel dan tulis data ke dalamnya
                       if (dataAbsensiPerUser.size() == userTask.getResult().size()) {
                           createExcelFile(dataAbsensiPerUser, monthInString, year);
                       }
                   });
               }
           }
        });
    }

    private void createExcelFile(HashMap<String, HashMap<String, Integer>> dataAbsensiPerUser, String monthInString, int year) {
        Workbook workbook = new XSSFWorkbook();

        // Header Cell Style
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);

        CellStyle normalCellStyle = workbook.createCellStyle();
        normalCellStyle.setAlignment(HorizontalAlignment.CENTER);
        normalCellStyle.setBorderTop(BorderStyle.MEDIUM);
        normalCellStyle.setBorderBottom(BorderStyle.MEDIUM);
        normalCellStyle.setBorderLeft(BorderStyle.MEDIUM);
        normalCellStyle.setBorderRight(BorderStyle.MEDIUM);

        Sheet sheet = workbook.createSheet(monthInString + " " + year);

        // Create header row
        Row headerRow = sheet.createRow(0);
        Cell namaPegawaiHeaderCell = headerRow.createCell(0);
        namaPegawaiHeaderCell.setCellValue("Nama Pegawai");
        namaPegawaiHeaderCell.setCellStyle(headerStyle);

        Cell totalHeaderCell = headerRow.createCell(1);
        totalHeaderCell.setCellValue("Total");
        totalHeaderCell.setCellStyle(headerStyle);

        int rowNum = 1; // Start from row 1 for data

        // Iterate through data and write to Excel
        for (Map.Entry<String, HashMap<String, Integer>> entry : dataAbsensiPerUser.entrySet()) {
            String namaPegawai = entry.getKey();
            HashMap<String, Integer> absensiMap = entry.getValue();

            Row row = sheet.createRow(rowNum++);
            Cell namaCell = row.createCell(0);
            namaCell.setCellValue(namaPegawai);
            namaCell.setCellStyle(normalCellStyle);

            // Check if absensi data exists for current month, if not, set 0
            int totalAbsensi = absensiMap.containsKey(monthInString) ? absensiMap.get(monthInString) : 0;
            Cell totalAbsenCell = row.createCell(1);
            totalAbsenCell.setCellValue(totalAbsensi);
            totalAbsenCell.setCellStyle(normalCellStyle);
        }

        // Save workbook to file
        try {
            String fileName = "REPORT_ABSENSI_" + monthInString + "_" + year + "_" + System.currentTimeMillis() + ".xls";
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(dir, fileName);
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            Log.d(TAG, "Berhasil di simpan di\t: " + file.getAbsolutePath());

            showToast("Report berhasil dibuat. Cek folder Downloads!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
