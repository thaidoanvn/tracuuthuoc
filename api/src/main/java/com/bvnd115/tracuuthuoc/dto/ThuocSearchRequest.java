package com.bvnd115.tracuuthuoc.dto;

public record ThuocSearchRequest(
        String keyword,
        Boolean tenThuoc,
        Boolean batDauVoi,
        Boolean hoatChat,
        Boolean cssx
) {
}
