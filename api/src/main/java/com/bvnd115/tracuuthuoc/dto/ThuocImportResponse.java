package com.bvnd115.tracuuthuoc.dto;

import java.util.List;

public record ThuocImportResponse(int created, int updated, int failed, List<ThuocImportError> errors) {
}
