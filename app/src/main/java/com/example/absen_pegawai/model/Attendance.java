package com.example.absen_pegawai.model;

public class Attendance {
//    public String tgl_masuk;
    public double longitude;
    public double latitude;
    public String jam_masuk;
    public String address_masuk;
    public String country_masuk;
    public String city_masuk;
    public int tgl_masuk, bulan_masuk, tahun_masuk;

    public Attendance() {}

    public Attendance(int tgl_masuk, double longitude, double latitude) {
        this.tgl_masuk = tgl_masuk;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Attendance(double longitude, double latitude, String jam_masuk, String address_masuk, String country_masuk, String city_masuk, int tgl_masuk, int bulan_masuk, int tahun_masuk) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.jam_masuk = jam_masuk;
        this.address_masuk = address_masuk;
        this.country_masuk = country_masuk;
        this.city_masuk = city_masuk;
        this.tgl_masuk = tgl_masuk;
        this.bulan_masuk = bulan_masuk;
        this.tahun_masuk = tahun_masuk;
    }

    public String getJam_masuk() {
        return jam_masuk;
    }

    public String getAddress_masuk() {
        return address_masuk;
    }

    public String getCountry_masuk() {
        return country_masuk;
    }

    public String getCity_masuk() {
        return city_masuk;
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

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
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

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public void setJam_masuk(String jam_masuk) {
        this.jam_masuk = jam_masuk;
    }

    public void setAddress_masuk(String address_masuk) {
        this.address_masuk = address_masuk;
    }

    public void setCountry_masuk(String country_masuk) {
        this.country_masuk = country_masuk;
    }

    public void setCity_masuk(String city_masuk) {
        this.city_masuk = city_masuk;
    }
}
