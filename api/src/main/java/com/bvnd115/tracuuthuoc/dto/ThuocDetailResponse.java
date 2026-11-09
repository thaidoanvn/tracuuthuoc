package com.bvnd115.tracuuthuoc.dto;

import com.bvnd115.tracuuthuoc.Thuoc;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ThuocDetailResponse(
        Long id,
        String code,
        String tenThuoc,
        String hoatChat,
        String dangBaoChe,
        String hangSanXuat,
        String hoiChuan,
        String kiemSoatDacBiet,
        Integer bhytTyLe,
        BigDecimal donGia,
        String soGiayPhep,
        String hamLuong,
        String quyCach,
        String donViTinh,
        String congTy,
        String nuocSanXuat,
        String chiDinh,
        String chongChiDinh,
        String lieuDung,
        String dieuKienBhyt,
        String soQuyetDinh,
        String nhom,
        String nhomThuoc,
        String bhyt,
        String bhytThanhToan,
        String nguoiThucHien,
        String ngaySanXuat,
        LocalDateTime hanDung,
        LocalDateTime ngayHetHan,
        String khongDung
) {
    public static ThuocDetailResponse from(Thuoc thuoc) {
        return new ThuocDetailResponse(
                thuoc.getId(),
                thuoc.getCode(),
                thuoc.getTenThuoc(),
                thuoc.getHoatChat(),
                thuoc.getDangBaoChe(),
                thuoc.getHangSanXuat(),
                thuoc.getHoiChuan(),
                thuoc.getKiemSoatDacBiet(),
                thuoc.getBhytTyLe(),
                thuoc.getDonGia(),
                blank(thuoc.getSoGiayPhep()),
                blank(thuoc.getHamLuong()),
                blank(thuoc.getQuyCach()),
                blank(thuoc.getDonViTinh()),
                blank(thuoc.getCongTy()),
                blank(thuoc.getNuocSanXuat()),
                blank(thuoc.getChiDinh()),
                blank(thuoc.getChongChiDinh()),
                blank(thuoc.getLieuDung()),
                blank(thuoc.getDieuKienBhyt()),
                blank(thuoc.getSoQuyetDinh()),
                blank(thuoc.getNhom()),
                blank(thuoc.getNhomThuoc()),
                blank(thuoc.getBhyt()),
                blank(thuoc.getBhytThanhToan()),
                blank(thuoc.getNguoiThucHien()),
                blank(thuoc.getNgaySanXuat()),
                thuoc.getHanDung(),
                thuoc.getNgayHetHan(),
                blank(thuoc.getKhongDung())
        );
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }
}
