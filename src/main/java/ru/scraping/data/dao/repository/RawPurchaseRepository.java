package ru.scraping.data.dao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.scraping.data.dao.entity.RawPurchaseEntity;
import ru.scraping.data.model.ScrapingSource;
import ru.scraping.data.model.Status;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RawPurchaseRepository extends JpaRepository<RawPurchaseEntity, Long> {

    @Query(value = "select max(session) from scraping.raw_purchase",
           nativeQuery = true)
    Optional<Long> findMaxSessionNumber();

    @Query(value = "select max(purchase_ts) from scraping.raw_purchase",
           nativeQuery = true)
    Optional<ZonedDateTime> findMaxLastPurchaseTs(ScrapingSource source);

    Page<RawPurchaseEntity> findAllBySourceAndStatus(ScrapingSource source, Status status, Pageable pageable);

    @Modifying
    @Query("update RawPurchaseEntity rp set rp.status = (:status) where rp.id in (:ids)")
    void updateStatusByIds(@Param("status") Status status,
                           @Param("ids") List<Long> ids);
}
