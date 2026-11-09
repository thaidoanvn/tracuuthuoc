package com.bvnd115.tracuuthuoc.controller;

import com.bvnd115.tracuuthuoc.dto.AdminLoginRequest;
import com.bvnd115.tracuuthuoc.dto.AdminLoginResponse;
import com.bvnd115.tracuuthuoc.dto.ThuocDetailResponse;
import com.bvnd115.tracuuthuoc.dto.ThuocImportResponse;
import com.bvnd115.tracuuthuoc.dto.ThuocSearchResponse;
import com.bvnd115.tracuuthuoc.dto.ThuocWriteRequest;
import com.bvnd115.tracuuthuoc.exception.ApiException;
import com.bvnd115.tracuuthuoc.service.AdminSessionService;
import com.bvnd115.tracuuthuoc.service.ThuocExcelImporter;
import com.bvnd115.tracuuthuoc.service.ThuocService;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminSessionService sessions;
    private final ThuocService thuocService;
    private final ThuocExcelImporter excelImporter;

    public AdminController(AdminSessionService sessions, ThuocService thuocService, ThuocExcelImporter excelImporter) {
        this.sessions = sessions;
        this.thuocService = thuocService;
        this.excelImporter = excelImporter;
    }

    @PostMapping("/login")
    public AdminLoginResponse login(@Valid @RequestBody AdminLoginRequest request) {
        return new AdminLoginResponse(sessions.login(request.username(), request.password()));
    }

    @GetMapping("/thuoc")
    public ThuocSearchResponse list(@RequestParam(required = false) String keyword) {
        return new ThuocSearchResponse(thuocService.list(keyword));
    }

    @GetMapping("/thuoc/{id}")
    public ThuocDetailResponse detail(@PathVariable long id) {
        return thuocService.detail(id);
    }

    @PostMapping(value = "/thuoc/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ThuocImportResponse importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chưa chọn file");
        }
        return excelImporter.importExcel(file.getInputStream());
    }

    @PostMapping("/thuoc")
    public ThuocDetailResponse create(@Valid @RequestBody ThuocWriteRequest request) {
        return thuocService.create(request);
    }

    @PutMapping("/thuoc/{id}")
    public ThuocDetailResponse update(@PathVariable long id, @Valid @RequestBody ThuocWriteRequest request) {
        return thuocService.update(id, request);
    }

    @DeleteMapping("/thuoc/{id}")
    public void delete(@PathVariable long id) {
        thuocService.delete(id);
    }
}
