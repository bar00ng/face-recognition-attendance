package com.example.absen_pegawai.model;

public class Attendance {
    public String jam_masuk;
    public String jam_keluar;
    public int tgl_masuk;
    public int bulan_masuk;
    public int tahun_masuk;

    public Attendance() {}

    public Attendance(String jam_masuk, String jam_keluar, int tgl_masuk, int bulan_masuk, int tahun_masuk) {
        this.jam_masuk = jam_masuk;
        this.jam_keluar = jam_keluar;
        this.tgl_masuk = tgl_masuk;
        this.bulan_masuk = bulan_masuk;
        this.tahun_masuk = tahun_masuk;
    }

    public String getJam_masuk() {
        return jam_masuk;
    }

    public String getJam_keluar() {
        return jam_keluar;
    }

    public int getTgl_masuk() {
        return tgl_masuk;
    }

    public int getBulan_masuk() {
        return bulan_masuk;
    }

    public int getTahun_masuk() {
        return tahun_masuk;
    }

    public void setJam_masuk(String jam_masuk) {
        this.jam_masuk = jam_masuk;
    }

    public void setJam_keluar(String jam_keluar) {
        this.jam_keluar = jam_keluar;
    }

    public void setTgl_masuk(int tgl_masuk) {
        this.tgl_masuk = tgl_masuk;
    }

    public void setBulan_masuk(int bulan_masuk) {
        this.bulan_masuk = bulan_masuk;
    }

    public void setTahun_masuk(int tahun_masuk) {
        this.tahun_masuk = tahun_masuk;
    }
}
