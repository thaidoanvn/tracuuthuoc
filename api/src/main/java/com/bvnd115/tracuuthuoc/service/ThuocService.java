package com.bvnd115.tracuuthuoc.service;

import com.bvnd115.tracuuthuoc.TextNormalizer;
import com.bvnd115.tracuuthuoc.Thuoc;
import com.bvnd115.tracuuthuoc.dto.ThuocDetailResponse;
import com.bvnd115.tracuuthuoc.dto.ThuocSearchRequest;
import com.bvnd115.tracuuthuoc.dto.ThuocSummaryResponse;
import com.bvnd115.tracuuthuoc.dto.ThuocWriteRequest;
import com.bvnd115.tracuuthuoc.exception.ApiException;
import com.bvnd115.tracuuthuoc.repository.ThuocRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThuocService {

    static final int SUGGEST_MIN_LENGTH = 3;
    static final int SUGGEST_LIMIT = 10;

    private final ThuocRepository repository;

    public ThuocService(ThuocRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ThuocSummaryResponse> search(ThuocSearchRequest request) {
        if (request == null || request.keyword() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu từ khóa");
        }
        String keyword = request.keyword().trim();
        if (keyword.isEmpty()) {
            return List.of();
        }
        boolean tenThuoc = Boolean.TRUE.equals(request.tenThuoc());
        boolean batDauVoi = Boolean.TRUE.equals(request.batDauVoi());
        boolean hoatChat = Boolean.TRUE.equals(request.hoatChat());
        boolean cssx = Boolean.TRUE.equals(request.cssx());
        if (!tenThuoc && !batDauVoi && !hoatChat && !cssx) {
            return List.of();
        }
        String pattern = TextNormalizer.escapeLike(TextNormalizer.fold(keyword));
        return repository.findAll(matching(pattern, tenThuoc, batDauVoi, hoatChat, cssx)).stream()
                .map(ThuocSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThuocSummaryResponse> suggest(
            String keyword,
            Boolean tenThuoc,
            Boolean batDauVoi,
            Boolean hoatChat,
            Boolean cssx
    ) {
        if (keyword == null || keyword.trim().length() < SUGGEST_MIN_LENGTH) {
            return List.of();
        }
        return search(new ThuocSearchRequest(keyword, tenThuoc, batDauVoi, hoatChat, cssx)).stream()
                .limit(SUGGEST_LIMIT)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThuocSummaryResponse> list(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return repository.findAll(Sort.by("tenThuoc")).stream()
                    .map(ThuocSummaryResponse::from)
                    .toList();
        }
        return search(new ThuocSearchRequest(keyword, true, true, true, true));
    }

    @Transactional
    public ThuocDetailResponse create(ThuocWriteRequest request) {
        requireUniqueCode(request.code().trim(), null);
        Thuoc thuoc = new Thuoc();
        apply(thuoc, request);
        return ThuocDetailResponse.from(repository.save(thuoc));
    }

    @Transactional
    public ThuocDetailResponse update(long id, ThuocWriteRequest request) {
        Thuoc thuoc = repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy thuốc"));
        requireUniqueCode(request.code().trim(), id);
        apply(thuoc, request);
        return ThuocDetailResponse.from(repository.save(thuoc));
    }

    @Transactional
    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy thuốc");
        }
        repository.deleteById(id);
    }

    private void requireUniqueCode(String code, Long currentId) {
        repository.findByCode(code).ifPresent(existing -> {
            if (currentId == null || !currentId.equals(existing.getId())) {
                throw new ApiException(HttpStatus.CONFLICT, "Mã thuốc đã tồn tại");
            }
        });
    }

    private void apply(Thuoc thuoc, ThuocWriteRequest request) {
        thuoc.setCode(request.code().trim());
        thuoc.setTenThuoc(request.tenThuoc().trim());
        thuoc.setHoatChat(text(request.hoatChat()));
        thuoc.setDangBaoChe(text(request.dangBaoChe()));
        thuoc.setHangSanXuat(text(request.hangSanXuat()));
        thuoc.setHoiChuan(text(request.hoiChuan()));
        thuoc.setKiemSoatDacBiet(text(request.kiemSoatDacBiet()));
        thuoc.setBhytTyLe(request.bhytTyLe());
        thuoc.setDonGia(request.donGia());
        thuoc.setSoGiayPhep(text(request.soGiayPhep()));
        thuoc.setHamLuong(text(request.hamLuong()));
        thuoc.setQuyCach(text(request.quyCach()));
        thuoc.setDonViTinh(text(request.donViTinh()));
        thuoc.setCongTy(text(request.congTy()));
        thuoc.setNuocSanXuat(text(request.nuocSanXuat()));
        thuoc.setChiDinh(text(request.chiDinh()));
        thuoc.setChongChiDinh(text(request.chongChiDinh()));
        thuoc.setLieuDung(text(request.lieuDung()));
        thuoc.setDieuKienBhyt(text(request.dieuKienBhyt()));
        thuoc.setSoQuyetDinh(text(request.soQuyetDinh()));
        thuoc.setNhom(text(request.nhom()));
        thuoc.setNhomThuoc(text(request.nhomThuoc()));
        thuoc.setBhyt(text(request.bhyt()));
        thuoc.setBhytThanhToan(text(request.bhytThanhToan()));
        thuoc.setNguoiThucHien(text(request.nguoiThucHien()));
        thuoc.setNgaySanXuat(text(request.ngaySanXuat()));
        thuoc.setHanDung(request.hanDung());
        thuoc.setNgayHetHan(request.ngayHetHan());
        String khongDung = text(request.khongDung());
        thuoc.setKhongDung(khongDung);
        thuoc.setNgungSuDung(ThuocExcelImporter.discontinued(khongDung));
    }

    private static String text(String value) {
        return value == null ? "" : value.trim();
    }

    @Transactional(readOnly = true)
    public ThuocDetailResponse detail(long id) {
        return repository.findById(id)
                .map(ThuocDetailResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy thuốc"));
    }

    private Specification<Thuoc> matching(
            String pattern,
            boolean tenThuoc,
            boolean batDauVoi,
            boolean hoatChat,
            boolean cssx
    ) {
        return (root, query, criteria) -> {
            List<Predicate> selected = new ArrayList<>();
            if (tenThuoc) {
                selected.add(criteria.like(root.get("tenThuocNorm"), "%" + pattern + "%", '\\'));
            }
            if (batDauVoi) {
                selected.add(criteria.like(root.get("tenThuocNorm"), pattern + "%", '\\'));
            }
            if (hoatChat) {
                selected.add(criteria.like(root.get("hoatChatNorm"), "%" + pattern + "%", '\\'));
            }
            if (cssx) {
                selected.add(criteria.like(root.get("hangSanXuatNorm"), "%" + pattern + "%", '\\'));
            }
            query.orderBy(criteria.asc(root.get("tenThuoc")), criteria.asc(root.get("id")));
            Predicate inUse = criteria.or(
                    criteria.isNull(root.get("ngungSuDung")),
                    criteria.isFalse(root.get("ngungSuDung"))
            );
            return criteria.and(criteria.or(selected.toArray(Predicate[]::new)), inUse);
        };
    }
}
