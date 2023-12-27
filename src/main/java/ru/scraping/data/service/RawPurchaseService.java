package ru.scraping.data.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.scraping.data.dao.entity.RawPurchaseEntity;
import ru.scraping.data.model.ScrapingSource;
import ru.scraping.data.model.Status;
import java.time.ZonedDateTime;
import java.util.List;

public interface RawPurchaseService {

    /**
     * Save raw purchse
     *
     * @param rawPurchase - raw purchse to save
     */
    void saveRawPurchase(RawPurchaseEntity rawPurchase);

    /**
     * Save raw purchse
     *
     * @param rawPurchaseList - list raw purchse to save
     */
    void saveRawPurchase(List<RawPurchaseEntity> rawPurchaseList);

    /**
     * Update status for raw purchse
     *
     * @param newStatus - new status to update
     * @param rawPurchaseIds - list ids for update status
     */
    void updateRawPurchaseStatus(Status newStatus, List<Long> rawPurchaseIds);

    /**
     * Get scraping session number (begin from 1 if database is empty)
     *
     * @return int - scraping session number
     */
    int getScrapingSessionNumber();

    /**
     * Get last purchase ts
     *
     * @param source - source of scraped data
     * @return ZonedDateTime - last purchase ts
     */
    ZonedDateTime getLastPurchaseTs(ScrapingSource source);

    /**
     * Find all purchase by source and pageable
     *
     * @param source - source of scraped data
     * @param pageable - pageble
     * @param status - scraped data status
     * @return page of searched purchases
     */
    Page<RawPurchaseEntity> findAllBySourceAndStatus(ScrapingSource source, Status status, Pageable pageable);
}
