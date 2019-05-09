package com.example.nminhanh.spacesharing.Model;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Space implements Serializable {
    private String id = "0";
    private String idChu = "0";
    private String tieuDe;
    private String diaChiPho;
    private String phuongId;
    private String quanId;
    private String thanhPhoId;
    private String diaChiDayDu;
    private double dienTich;
    private double gia;
    private String firstImagePath;
    private int soPhongNgu;
    private int soPhongVeSinh;
    private String loai;
    private String huongCua;
    private String moTa;
    private int thangCoc;
    private double giaDien;
    private double giaNuoc;
    private boolean khaDung = false;
    private ArrayList<Double> l = new ArrayList<>();
    private String g;

    @ServerTimestamp
    private Date timeAdded;

    public Space() {
    }


    public Space(String id,
                 String idChu,
                 String tieuDe,
                 String diaChiPho,
                 String phuong,
                 String quan,
                 String diaChiDayDu,
                 String g,
                 ArrayList<Double> l,
                 String thanhPho,
                 double dienTich,
                 double gia,
                 String firstImagePath,
                 int soPhongNgu,
                 int soPhongVeSinh,
                 String loai,
                 String huongCua,
                 String moTa,
                 int thangCoc,
                 double giaDien,
                 double giaNuoc,
                 boolean khaDung
    ) {
        this.id = id;
        this.idChu = idChu;
        this.tieuDe = tieuDe;
        this.diaChiPho = diaChiPho;
        this.phuongId = phuong;
        this.quanId = quan;
        this.thanhPhoId = thanhPho;
        this.diaChiDayDu = diaChiDayDu;
        this.g = g;
        this.l = new ArrayList<>(l);
        this.dienTich = dienTich;
        this.gia = gia;
        this.firstImagePath = firstImagePath;
        this.soPhongNgu = soPhongNgu;
        this.soPhongVeSinh = soPhongVeSinh;
        this.loai = loai;
        this.huongCua = huongCua;
        this.moTa = moTa;
        this.thangCoc = thangCoc;
        this.giaDien = giaDien;
        this.giaNuoc = giaNuoc;
        this.khaDung = khaDung;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdChu() {
        return idChu;
    }

    public void setIdChu(String idChu) {
        this.idChu = idChu;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getDiaChiPho() {
        return diaChiPho;
    }

    public void setDiaChiPho(String diaChiPho) {
        this.diaChiPho = diaChiPho;
    }

    public String getPhuongId() {
        return phuongId;
    }

    public void setPhuongId(String phuong) {
        this.phuongId = phuong;
    }

    public String getQuanId() {
        return quanId;
    }

    public void setQuanId(String quan) {
        this.quanId = quan;
    }

    public String getThanhPhoId() {
        return thanhPhoId;
    }

    public void setThanhPhoId(String thanhPho) {
        this.thanhPhoId = thanhPho;
    }

    public double getDienTich() {
        return dienTich;
    }

    public void setDienTich(double dienTich) {
        this.dienTich = dienTich;
    }

    public double getGia() {
        return gia;
    }

    public void setGia(double gia) {
        this.gia = gia;
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public int getSoPhongNgu() {
        return soPhongNgu;
    }

    public void setSoPhongNgu(int soPhongNgu) {
        this.soPhongNgu = soPhongNgu;
    }

    public int getSoPhongVeSinh() {
        return soPhongVeSinh;
    }

    public void setSoPhongVeSinh(int soPhongVeSinh) {
        this.soPhongVeSinh = soPhongVeSinh;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public String getHuongCua() {
        return huongCua;
    }

    public void setHuongCua(String huongCua) {
        this.huongCua = huongCua;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public boolean isKhaDung() {
        return khaDung;
    }

    public void setKhaDung(boolean khaDung) {
        this.khaDung = khaDung;
    }

    public String getDiaChiDayDu() {
        return diaChiDayDu;
    }

    public void setDiaChiDayDu(String diaChiDayDu) {
        this.diaChiDayDu = diaChiDayDu;
    }

    public ArrayList<Double> getL() {
        return l;
    }

    public void setL(ArrayList<Double> l) {
        this.l = l;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public int getThangCoc() {
        return thangCoc;
    }

    public void setThangCoc(int thangCoc) {
        this.thangCoc = thangCoc;
    }

    public double getGiaDien() {
        return giaDien;
    }

    public void setGiaDien(double giaDien) {
        this.giaDien = giaDien;
    }

    public double getGiaNuoc() {
        return giaNuoc;
    }

    public void setGiaNuoc(double giaNuoc) {
        this.giaNuoc = giaNuoc;
    }

    public Date getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Date timeAdded) {
        this.timeAdded = timeAdded;
    }

}
