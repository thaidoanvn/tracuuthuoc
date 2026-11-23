package com.bvnd115.tracuuthuoc.service;

import com.bvnd115.tracuuthuoc.TextNormalizer;
import com.bvnd115.tracuuthuoc.Thuoc;
import com.bvnd115.tracuuthuoc.dto.ThuocDetailResponse;
import com.bvnd115.tracuuthuoc.dto.ThuocSummaryResponse;
import com.bvnd115.tracuuthuoc.repository.ThuocRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class ThuocCatalogCache {

    static final String CACHE = "thuocCatalog";

    private final ThuocRepository repository;
    private final CacheManager cacheManager;

    public ThuocCatalogCache(ThuocRepository repository, CacheManager cacheManager) {
        this.repository = repository;
        this.cacheManager = cacheManager;
    }

    @Cacheable(CACHE)
    @Transactional(readOnly = true)
    public Catalog snapshot() {
        List<CachedThuoc> drugs = repository.findAll().stream()
                .map(CachedThuoc::from)
                .sorted(Comparator.comparing(CachedThuoc::tenThuoc).thenComparingLong(CachedThuoc::id))
                .toList();
        Map<Long, CachedThuoc> byId = new HashMap<>();
        for (CachedThuoc drug : drugs) {
            byId.put(drug.id(), drug);
        }
        return new Catalog(drugs, Map.copyOf(byId));
    }

    public void evictAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    clear();
                }
            });
            return;
        }
        clear();
    }

    public void clear() {
        Cache cache = cacheManager.getCache(CACHE);
        if (cache != null) {
            cache.clear();
        }
    }

    public record Catalog(List<CachedThuoc> drugs, Map<Long, CachedThuoc> byId) {
    }

    public record CachedThuoc(
            long id,
            String tenThuoc,
            String tenThuocNorm,
            String hoatChatNorm,
            String hangSanXuatNorm,
            String searchableText,
            boolean discontinued,
            ThuocSummaryResponse summary,
            ThuocDetailResponse detail
    ) {
        static CachedThuoc from(Thuoc thuoc) {
            return new CachedThuoc(
                    thuoc.getId(),
                    thuoc.getTenThuoc(),
                    TextNormalizer.fold(thuoc.getTenThuoc()),
                    TextNormalizer.fold(thuoc.getHoatChat()),
                    TextNormalizer.fold(thuoc.getHangSanXuat()),
                    searchableText(thuoc),
                    Boolean.TRUE.equals(thuoc.getNgungSuDung()),
                    ThuocSummaryResponse.from(thuoc),
                    ThuocDetailResponse.from(thuoc)
            );
        }

        private static String searchableText(Thuoc thuoc) {
            return TextNormalizer.fold(String.join(
                    "\n",
                    text(thuoc.getCode()),
                    text(thuoc.getTenThuoc()),
                    text(thuoc.getHoatChat()),
                    text(thuoc.getDangBaoChe()),
                    text(thuoc.getHangSanXuat()),
                    text(thuoc.getHoiChuan()),
                    text(thuoc.getKiemSoatDacBiet()),
                    text(thuoc.getSoGiayPhep()),
                    text(thuoc.getHamLuong()),
                    text(thuoc.getQuyCach()),
                    text(thuoc.getDonViTinh()),
                    text(thuoc.getCongTy()),
                    text(thuoc.getNuocSanXuat()),
                    text(thuoc.getChiDinh()),
                    text(thuoc.getChongChiDinh()),
                    text(thuoc.getLieuDung()),
                    text(thuoc.getDieuKienBhyt()),
                    text(thuoc.getSoQuyetDinh()),
                    text(thuoc.getNhom()),
                    text(thuoc.getNhomThuoc()),
                    text(thuoc.getBhyt()),
                    text(thuoc.getBhytThanhToan()),
                    text(thuoc.getNguoiThucHien()),
                    text(thuoc.getNgaySanXuat()),
                    text(thuoc.getKhongDung()),
                    thuoc.getBhytTyLe() == null ? "" : thuoc.getBhytTyLe().toString(),
                    thuoc.getDonGia() == null ? "" : thuoc.getDonGia().toPlainString(),
                    thuoc.getHanDung() == null ? "" : thuoc.getHanDung().toString(),
                    thuoc.getNgayHetHan() == null ? "" : thuoc.getNgayHetHan().toString()
            ));
        }

        private static String text(String value) {
            return value == null ? "" : value;
        }
    }
}
