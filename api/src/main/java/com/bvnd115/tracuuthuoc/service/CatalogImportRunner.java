package com.bvnd115.tracuuthuoc.service;

import com.bvnd115.tracuuthuoc.Thuoc;
import com.bvnd115.tracuuthuoc.repository.ThuocRepository;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class CatalogImportRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogImportRunner.class);

    private final ThuocRepository repository;

    public CatalogImportRunner(ThuocRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws IOException {
        if (repository.count() > 0) {
            return;
        }
        ClassPathResource catalog = new ClassPathResource("catalog/thuoc.csv");
        if (!catalog.exists()) {
            throw new IllegalStateException("Missing catalog/thuoc.csv");
        }
        List<Thuoc> rows = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(catalog.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            for (CSVRecord record : parser) {
                rows.add(toThuoc(record));
            }
        }
        repository.saveAll(rows);
        log.info("Imported {} sample drugs from catalog/thuoc.csv", rows.size());
    }

    private Thuoc toThuoc(CSVRecord record) {
        Thuoc thuoc = new Thuoc();
        thuoc.setCode(record.get("code"));
        thuoc.setTenThuoc(record.get("tenThuoc"));
        thuoc.setHoatChat(record.get("hoatChat"));
        thuoc.setDangBaoChe(record.get("dangBaoChe"));
        thuoc.setHangSanXuat(record.get("hangSanXuat"));
        thuoc.setHoiChuan(record.get("hoiChuan"));
        thuoc.setKiemSoatDacBiet(record.get("kiemSoatDacBiet"));
        thuoc.setBhytTyLe(Integer.valueOf(record.get("bhytTyLe")));
        thuoc.setDonGia(new BigDecimal(record.get("donGia")));
        thuoc.setSoGiayPhep(record.get("soGiayPhep"));
        thuoc.setHamLuong(record.get("hamLuong"));
        thuoc.setQuyCach(record.get("quyCach"));
        thuoc.setDonViTinh(record.get("donViTinh"));
        thuoc.setCongTy(record.get("congTy"));
        thuoc.setNuocSanXuat(record.get("nuocSanXuat"));
        thuoc.setChiDinh(record.get("chiDinh"));
        thuoc.setChongChiDinh(record.get("chongChiDinh"));
        thuoc.setLieuDung(record.get("lieuDung"));
        thuoc.setDieuKienBhyt(record.get("dieuKienBhyt"));
        return thuoc;
    }
}
