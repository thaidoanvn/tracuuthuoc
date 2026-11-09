package com.bvnd115.tracuuthuoc.dto;

import com.bvnd115.tracuuthuoc.Thuoc;
import java.math.BigDecimal;

public record ThuocSummaryResponse(
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
        String khongDung
) {
    public static ThuocSummaryResponse from(Thuoc thuoc) {
        return new ThuocSummaryResponse(
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
                thuoc.getKhongDung() == null ? "" : thuoc.getKhongDung()
        );
    }
}
