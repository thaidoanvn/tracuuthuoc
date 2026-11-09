package com.bvnd115.tracuuthuoc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ThuocWriteRequest(
        @NotBlank String code,
        @NotBlank String tenThuoc,
        String hoatChat,
        String dangBaoChe,
        String hangSanXuat,
        String hoiChuan,
        String kiemSoatDacBiet,
        @NotNull Integer bhytTyLe,
        @NotNull BigDecimal donGia,
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
}
