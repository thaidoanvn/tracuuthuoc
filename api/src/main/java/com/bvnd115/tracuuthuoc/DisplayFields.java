package com.bvnd115.tracuuthuoc;

import java.util.List;

public final class DisplayFields {

    public static final List<String> KEYS = List.of(
            "code",
            "soQuyetDinh",
            "soGiayPhep",
            "hoatChat",
            "hamLuong",
            "dangBaoChe",
            "quyCach",
            "donViTinh",
            "donGia",
            "congTy",
            "hangSanXuat",
            "nuocSanXuat",
            "hoiChuan",
            "kiemSoatDacBiet",
            "nhom",
            "nhomThuoc",
            "bhyt",
            "bhytTyLe",
            "bhytThanhToan",
            "dieuKienBhyt",
            "nguoiThucHien",
            "ngaySanXuat",
            "hanDung",
            "ngayHetHan",
            "khongDung",
            "chiDinh",
            "chongChiDinh",
            "lieuDung"
    );

    private DisplayFields() {
    }

    public static boolean known(String key) {
        return KEYS.contains(key);
    }
}
