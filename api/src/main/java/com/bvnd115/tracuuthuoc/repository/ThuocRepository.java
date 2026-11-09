package com.bvnd115.tracuuthuoc.repository;

import com.bvnd115.tracuuthuoc.Thuoc;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ThuocRepository extends JpaRepository<Thuoc, Long>, JpaSpecificationExecutor<Thuoc> {

    Optional<Thuoc> findByCode(String code);
}
