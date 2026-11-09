package com.bvnd115.tracuuthuoc.controller;

import com.bvnd115.tracuuthuoc.dto.ThuocDetailResponse;
import com.bvnd115.tracuuthuoc.dto.ThuocSearchRequest;
import com.bvnd115.tracuuthuoc.dto.ThuocSearchResponse;
import com.bvnd115.tracuuthuoc.service.ThuocService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/thuoc")
public class ThuocController {

    private final ThuocService service;

    public ThuocController(ThuocService service) {
        this.service = service;
    }

    @PostMapping("/search")
    public ThuocSearchResponse search(@RequestBody ThuocSearchRequest request) {
        return new ThuocSearchResponse(service.search(request));
    }

    @GetMapping("/suggest")
    public ThuocSearchResponse suggest(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean tenThuoc,
            @RequestParam(required = false) Boolean batDauVoi,
            @RequestParam(required = false) Boolean hoatChat,
            @RequestParam(required = false) Boolean cssx
    ) {
        return new ThuocSearchResponse(service.suggest(keyword, tenThuoc, batDauVoi, hoatChat, cssx));
    }

    @GetMapping("/{id}")
    public ThuocDetailResponse detail(@PathVariable long id) {
        return service.detail(id);
    }
}
