package com.bvnd115.tracuuthuoc;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "thuoc", uniqueConstraints = @UniqueConstraint(name = "uk_thuoc_code", columnNames = "code"))
public class Thuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 500)
    private String tenThuoc;

    @Column(nullable = false, length = 500)
    private String hoatChat;

    @Column(nullable = false, length = 500)
    private String dangBaoChe;

    @Column(nullable = false, length = 500)
    private String hangSanXuat;

    @Column(nullable = false, length = 255)
    private String hoiChuan;

    @Column(nullable = false, length = 255)
    private String kiemSoatDacBiet;

    @Column(nullable = false)
    private Integer bhytTyLe;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal donGia;

    @Column(nullable = false, length = 128)
    private String soGiayPhep;

    @Column(nullable = false, length = 255)
    private String hamLuong;

    @Column(nullable = false, length = 500)
    private String quyCach;

    @Column(nullable = false, length = 64)
    private String donViTinh;

    @Column(nullable = false, length = 500)
    private String congTy;

    @Column(nullable = false, length = 128)
    private String nuocSanXuat;

    @Column(nullable = false, length = 4000)
    private String chiDinh;

    @Column(nullable = false, length = 4000)
    private String chongChiDinh;

    @Column(nullable = false, length = 4000)
    private String lieuDung;

    @Column(nullable = false, length = 2000)
    private String dieuKienBhyt;

    @Column(length = 128)
    private String soQuyetDinh;

    @Column(length = 255)
    private String nhom;

    @Column(length = 255)
    private String nhomThuoc;

    @Column(length = 64)
    private String bhyt;

    @Column(length = 500)
    private String bhytThanhToan;

    @Column(length = 255)
    private String nguoiThucHien;

    @Column(length = 64)
    private String ngaySanXuat;

    @Column
    private LocalDateTime hanDung;

    @Column
    private LocalDateTime ngayHetHan;

    @Column(length = 500)
    private String khongDung;

    @Column
    private Boolean ngungSuDung;

    @Column(nullable = false, length = 500)
    private String tenThuocNorm;

    @Column(nullable = false, length = 500)
    private String hoatChatNorm;

    @Column(nullable = false, length = 500)
    private String hangSanXuatNorm;

    @PrePersist
    @PreUpdate
    void normalize() {
        tenThuocNorm = TextNormalizer.fold(tenThuoc);
        hoatChatNorm = TextNormalizer.fold(hoatChat);
        hangSanXuatNorm = TextNormalizer.fold(hangSanXuat);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public void setTenThuoc(String tenThuoc) {
        this.tenThuoc = tenThuoc;
    }

    public String getHoatChat() {
        return hoatChat;
    }

    public void setHoatChat(String hoatChat) {
        this.hoatChat = hoatChat;
    }

    public String getDangBaoChe() {
        return dangBaoChe;
    }

    public void setDangBaoChe(String dangBaoChe) {
        this.dangBaoChe = dangBaoChe;
    }

    public String getHangSanXuat() {
        return hangSanXuat;
    }

    public void setHangSanXuat(String hangSanXuat) {
        this.hangSanXuat = hangSanXuat;
    }

    public String getHoiChuan() {
        return hoiChuan;
    }

    public void setHoiChuan(String hoiChuan) {
        this.hoiChuan = hoiChuan;
    }

    public String getKiemSoatDacBiet() {
        return kiemSoatDacBiet;
    }

    public void setKiemSoatDacBiet(String kiemSoatDacBiet) {
        this.kiemSoatDacBiet = kiemSoatDacBiet;
    }

    public Integer getBhytTyLe() {
        return bhytTyLe;
    }

    public void setBhytTyLe(Integer bhytTyLe) {
        this.bhytTyLe = bhytTyLe;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getSoGiayPhep() {
        return soGiayPhep;
    }

    public void setSoGiayPhep(String soGiayPhep) {
        this.soGiayPhep = soGiayPhep;
    }

    public String getHamLuong() {
        return hamLuong;
    }

    public void setHamLuong(String hamLuong) {
        this.hamLuong = hamLuong;
    }

    public String getQuyCach() {
        return quyCach;
    }

    public void setQuyCach(String quyCach) {
        this.quyCach = quyCach;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getCongTy() {
        return congTy;
    }

    public void setCongTy(String congTy) {
        this.congTy = congTy;
    }

    public String getNuocSanXuat() {
        return nuocSanXuat;
    }

    public void setNuocSanXuat(String nuocSanXuat) {
        this.nuocSanXuat = nuocSanXuat;
    }

    public String getChiDinh() {
        return chiDinh;
    }

    public void setChiDinh(String chiDinh) {
        this.chiDinh = chiDinh;
    }

    public String getChongChiDinh() {
        return chongChiDinh;
    }

    public void setChongChiDinh(String chongChiDinh) {
        this.chongChiDinh = chongChiDinh;
    }

    public String getLieuDung() {
        return lieuDung;
    }

    public void setLieuDung(String lieuDung) {
        this.lieuDung = lieuDung;
    }

    public String getDieuKienBhyt() {
        return dieuKienBhyt;
    }

    public void setDieuKienBhyt(String dieuKienBhyt) {
        this.dieuKienBhyt = dieuKienBhyt;
    }

    public String getSoQuyetDinh() {
        return soQuyetDinh;
    }

    public void setSoQuyetDinh(String soQuyetDinh) {
        this.soQuyetDinh = soQuyetDinh;
    }

    public String getNhom() {
        return nhom;
    }

    public void setNhom(String nhom) {
        this.nhom = nhom;
    }

    public String getNhomThuoc() {
        return nhomThuoc;
    }

    public void setNhomThuoc(String nhomThuoc) {
        this.nhomThuoc = nhomThuoc;
    }

    public String getBhyt() {
        return bhyt;
    }

    public void setBhyt(String bhyt) {
        this.bhyt = bhyt;
    }

    public String getBhytThanhToan() {
        return bhytThanhToan;
    }

    public void setBhytThanhToan(String bhytThanhToan) {
        this.bhytThanhToan = bhytThanhToan;
    }

    public String getNguoiThucHien() {
        return nguoiThucHien;
    }

    public void setNguoiThucHien(String nguoiThucHien) {
        this.nguoiThucHien = nguoiThucHien;
    }

    public String getNgaySanXuat() {
        return ngaySanXuat;
    }

    public void setNgaySanXuat(String ngaySanXuat) {
        this.ngaySanXuat = ngaySanXuat;
    }

    public LocalDateTime getHanDung() {
        return hanDung;
    }

    public void setHanDung(LocalDateTime hanDung) {
        this.hanDung = hanDung;
    }

    public LocalDateTime getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(LocalDateTime ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    public String getKhongDung() {
        return khongDung;
    }

    public void setKhongDung(String khongDung) {
        this.khongDung = khongDung;
    }

    public Boolean getNgungSuDung() {
        return ngungSuDung;
    }

    public void setNgungSuDung(Boolean ngungSuDung) {
        this.ngungSuDung = ngungSuDung;
    }
}
