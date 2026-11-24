package com.bvnd115.tracuuthuoc;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class Messages {

    public static final String API_KEY_INVALID = "error.apiKey.invalid";
    public static final String SESSION_INVALID = "error.session.invalid";
    public static final String LOGIN_INVALID = "error.login.invalid";
    public static final String KEYWORD_MISSING = "error.keyword.missing";
    public static final String DRUG_NOT_FOUND = "error.drug.notFound";
    public static final String DRUG_CODE_EXISTS = "error.drug.code.exists";
    public static final String DRUG_ID_INVALID = "error.drug.id.invalid";
    public static final String REQUEST_INVALID = "error.request.invalid";
    public static final String DISPLAY_FIELDS_MISSING = "error.display.fields.missing";
    public static final String DISPLAY_FIELD_INVALID = "error.display.field.invalid";
    public static final String FILE_MISSING = "error.file.missing";
    public static final String EXCEL_UNREADABLE = "error.excel.unreadable";
    public static final String EXCEL_EMPTY = "error.excel.empty";
    public static final String EXCEL_COLUMNS_MISSING = "error.excel.columns.missing";
    public static final String EXCEL_CODE_MISSING = "error.excel.code.missing";
    public static final String EXCEL_CODE_TOO_LONG = "error.excel.code.tooLong";
    public static final String EXCEL_NAME_MISSING = "error.excel.name.missing";
    public static final String EXCEL_HAN_DUNG_INVALID = "error.excel.hanDung.invalid";
    public static final String EXCEL_NGAY_HET_HAN_INVALID = "error.excel.ngayHetHan.invalid";
    public static final String EXCEL_PRICE_INVALID = "error.excel.price.invalid";
    public static final String EXCEL_RATE_INVALID = "error.excel.rate.invalid";

    private final MessageSource source;

    public Messages(MessageSource source) {
        this.source = source;
    }

    public String get(String code) {
        return source.getMessage(code, null, Locale.ROOT);
    }
}
