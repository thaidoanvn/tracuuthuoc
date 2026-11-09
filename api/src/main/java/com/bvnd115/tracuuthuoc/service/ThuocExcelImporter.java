package com.bvnd115.tracuuthuoc.service;

import com.bvnd115.tracuuthuoc.TextNormalizer;
import com.bvnd115.tracuuthuoc.Thuoc;
import com.bvnd115.tracuuthuoc.dto.ThuocImportError;
import com.bvnd115.tracuuthuoc.dto.ThuocImportResponse;
import com.bvnd115.tracuuthuoc.exception.ApiException;
import com.bvnd115.tracuuthuoc.repository.ThuocRepository;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThuocExcelImporter {

    private static final int ERROR_LIMIT = 20;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ThuocRepository repository;

    public ThuocExcelImporter(ThuocRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ThuocImportResponse importExcel(InputStream input) {
        Workbook workbook;
        try {
            workbook = WorkbookFactory.create(input);
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Không đọc được file Excel");
        }
        try (workbook) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "File Excel không có dữ liệu");
            }
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("vi"));
            int headerRow = findHeader(sheet, formatter);
            if (headerRow < 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "File Excel thiếu cột Mã thuốc hoặc Tên thuốc");
            }
            Map<String, Integer> columns = columns(sheet.getRow(headerRow), formatter);
            if (!columns.containsKey("ma thuoc") || !columns.containsKey("ten thuoc")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "File Excel thiếu cột Mã thuốc hoặc Tên thuốc");
            }
            int created = 0;
            int updated = 0;
            int failed = 0;
            List<ThuocImportError> errors = new ArrayList<>();
            for (int index = headerRow + 1; index <= sheet.getLastRowNum(); index++) {
                Row row = sheet.getRow(index);
                if (row == null || blankRow(row, columns, formatter)) {
                    continue;
                }
                try {
                    boolean isNew = saveRow(row, columns, formatter);
                    if (isNew) {
                        created++;
                    } else {
                        updated++;
                    }
                } catch (IllegalArgumentException exception) {
                    failed++;
                    if (errors.size() < ERROR_LIMIT) {
                        errors.add(new ThuocImportError(index + 1, exception.getMessage()));
                    }
                }
            }
            if (created == 0 && updated == 0 && failed == 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "File Excel không có dữ liệu");
            }
            return new ThuocImportResponse(created, updated, failed, errors);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Không đọc được file Excel");
        }
    }

    private boolean saveRow(Row row, Map<String, Integer> columns, DataFormatter formatter) {
        String code = value(row, columns, "ma thuoc", formatter).trim();
        String tenThuoc = clip(value(row, columns, "ten thuoc", formatter), 500);
        if (code.isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã thuốc");
        }
        if (code.length() > 64) {
            throw new IllegalArgumentException("Mã thuốc dài quá 64 ký tự");
        }
        if (tenThuoc.isEmpty()) {
            throw new IllegalArgumentException("Thiếu tên thuốc");
        }
        Thuoc thuoc = repository.findByCode(code).orElseGet(Thuoc::new);
        boolean created = thuoc.getId() == null;
        thuoc.setCode(code);
        thuoc.setTenThuoc(tenThuoc);
        thuoc.setHoatChat(clip(value(row, columns, "hoat chat", formatter), 500));
        thuoc.setDangBaoChe(clip(value(row, columns, "dang bao che", formatter), 500));
        thuoc.setHangSanXuat(clip(value(row, columns, "hang san xuat", formatter), 500));
        thuoc.setHoiChuan(clip(value(row, columns, "hoi chuan", formatter), 255));
        thuoc.setKiemSoatDacBiet(clip(value(row, columns, "ksdb", formatter), 255));
        thuoc.setBhytTyLe(rate(value(row, columns, "ty le", formatter)));
        thuoc.setDonGia(money(value(row, columns, "don gia", formatter)));
        thuoc.setSoGiayPhep(clip(value(row, columns, "sdk", formatter), 128));
        thuoc.setHamLuong(clip(value(row, columns, "ham luong", formatter), 255));
        thuoc.setQuyCach(clip(value(row, columns, "quy cach dong goi", formatter), 500));
        thuoc.setDonViTinh(clip(value(row, columns, "don vi tinh", formatter), 64));
        thuoc.setCongTy(clip(value(row, columns, "ten cong ty", formatter), 500));
        thuoc.setNuocSanXuat(clip(value(row, columns, "nuoc san xuat", formatter), 128));
        thuoc.setChiDinh(clip(value(row, columns, "chi dinh", formatter), 4000));
        thuoc.setChongChiDinh(clip(value(row, columns, "chong chi dinh", formatter), 4000));
        thuoc.setLieuDung(clip(value(row, columns, "lieu dung", formatter), 4000));
        thuoc.setDieuKienBhyt(clip(value(row, columns, "dieu kien thanh toan", formatter), 2000));
        thuoc.setSoQuyetDinh(clip(value(row, columns, "so qd", formatter), 128));
        thuoc.setNhom(clip(value(row, columns, "nhom", formatter), 255));
        thuoc.setNhomThuoc(clip(value(row, columns, "nhom thuoc", formatter), 255));
        thuoc.setBhyt(clip(value(row, columns, "bhyt", formatter), 64));
        thuoc.setBhytThanhToan(clip(value(row, columns, "bhyt thanh toan", formatter), 500));
        thuoc.setNguoiThucHien(clip(value(row, columns, "nguoi thuc hien", formatter), 255));
        thuoc.setNgaySanXuat(clip(value(row, columns, "ngay san xuat", formatter), 64));
        thuoc.setHanDung(dateTime(value(row, columns, "han dung", formatter), "Hạn dùng"));
        thuoc.setNgayHetHan(dateTime(value(row, columns, "ngay het han", formatter), "Ngày hết hạn"));
        String khongDung = clip(value(row, columns, "khong dung", formatter), 500);
        thuoc.setKhongDung(khongDung);
        thuoc.setNgungSuDung(discontinued(khongDung));
        repository.save(thuoc);
        return created;
    }

    static boolean discontinued(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String folded = TextNormalizer.fold(value);
        return !(folded.equals("khong") || folded.equals("no") || folded.equals("0") || folded.equals("false"));
    }

    private LocalDateTime dateTime(String raw, String label) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String text = raw.trim();
        DateTimeFormatter[] dateTimes = {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("d/M/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("M/d/uuuu H:mm:ss").withResolverStyle(ResolverStyle.STRICT),
                DateTimeFormatter.ofPattern("M/d/uuuu H:mm").withResolverStyle(ResolverStyle.STRICT)
        };
        for (DateTimeFormatter formatter : dateTimes) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next accepted datetime format.
            }
        }
        DateTimeFormatter[] dates = {
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("M/d/uuuu").withResolverStyle(ResolverStyle.STRICT)
        };
        for (DateTimeFormatter formatter : dates) {
            try {
                return LocalDate.parse(text, formatter).atStartOfDay();
            } catch (DateTimeParseException ignored) {
                // Try the next accepted date format.
            }
        }
        throw new IllegalArgumentException(label + " không hợp lệ");
    }

    private int findHeader(Sheet sheet, DataFormatter formatter) {
        int last = Math.min(sheet.getLastRowNum(), 15);
        for (int index = 0; index <= last; index++) {
            Row row = sheet.getRow(index);
            if (row == null) {
                continue;
            }
            Map<String, Integer> columns = columns(row, formatter);
            if (columns.containsKey("ma thuoc") && columns.containsKey("ten thuoc")) {
                return index;
            }
        }
        return -1;
    }

    private Map<String, Integer> columns(Row row, DataFormatter formatter) {
        Map<String, Integer> columns = new HashMap<>();
        if (row == null) {
            return columns;
        }
        for (Cell cell : row) {
            String key = headerKey(cellText(cell, formatter));
            if (!key.isEmpty() && !columns.containsKey(key)) {
                columns.put(key, cell.getColumnIndex());
            }
        }
        return columns;
    }

    private boolean blankRow(Row row, Map<String, Integer> columns, DataFormatter formatter) {
        for (int column : columns.values()) {
            if (!cellText(row.getCell(column), formatter).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String value(Row row, Map<String, Integer> columns, String key, DataFormatter formatter) {
        Integer column = columns.get(key);
        if (column == null) {
            return "";
        }
        return cellText(row.getCell(column), formatter);
    }

    private String cellText(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        try {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().format(DATE_FORMAT);
            }
        } catch (RuntimeException ignored) {
            // A numeric cell without a date format falls through to the displayed text.
        }
        return formatter.formatCellValue(cell).trim();
    }

    private static String headerKey(String value) {
        return TextNormalizer.fold(value).replace(",", "").replaceAll("\\s+", " ").trim();
    }

    private static String clip(String value, int max) {
        String text = value == null ? "" : value.trim();
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max);
    }

    private BigDecimal money(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }
        String cleaned = raw.replace("đ", "").replace("₫", "").replace(" ", "").trim();
        if (cleaned.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(normalizeNumber(cleaned));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Đơn giá không hợp lệ");
        }
    }

    private int rate(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        String cleaned = raw.replace("%", "").replace(" ", "").trim();
        if (cleaned.isEmpty()) {
            return 0;
        }
        try {
            BigDecimal number = new BigDecimal(normalizeNumber(cleaned));
            if (number.compareTo(BigDecimal.ZERO) > 0 && number.compareTo(BigDecimal.ONE) < 0) {
                number = number.multiply(BigDecimal.valueOf(100));
            }
            return number.setScale(0, RoundingMode.HALF_UP).intValue();
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Tỷ lệ không hợp lệ");
        }
    }

    private static String normalizeNumber(String raw) {
        String cleaned = raw;
        if (cleaned.contains(",") && cleaned.contains(".")) {
            if (cleaned.lastIndexOf(',') > cleaned.lastIndexOf('.')) {
                cleaned = cleaned.replace(".", "").replace(",", ".");
            } else {
                cleaned = cleaned.replace(",", "");
            }
        } else if (cleaned.contains(",")) {
            cleaned = cleaned.matches(".*,\\d{3}") ? cleaned.replace(",", "") : cleaned.replace(",", ".");
        } else if (cleaned.matches("\\d{1,3}(\\.\\d{3})+")) {
            cleaned = cleaned.replace(".", "");
        }
        return cleaned;
    }
}
