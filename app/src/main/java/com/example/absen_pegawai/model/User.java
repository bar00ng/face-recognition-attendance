package com.example.absen_pegawai.model;

public class User {
    private String nama, nik, password, role;

    public User() {}

    public User(String nama, String nik, String password, String role) {
        this.nama = nama;
        this.nik = nik;
        this.password = password;
        this.role = role;
    }

    public User(String password) {
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getNama() {
        return nama;
    }

    public String getNik() {
        return nik;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }
}
