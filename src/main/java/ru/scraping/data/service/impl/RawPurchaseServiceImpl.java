package ru.scraping.data.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.scraping.data.dao.entity.RawPurchaseEntity;
import ru.scraping.data.dao.repository.RawPurchaseRepository;
import ru.scraping.data.model.ScrapingSource;
import ru.scraping.data.model.Status;
import ru.scraping.data.service.RawPurchaseService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RawPurchaseServiceImpl implements RawPurchaseService {

    private static final ZoneId ZONE_ID = ZoneId.of("UTC+5");

    private final RawPurchaseRepository repository;

    @Override
    public void saveRawPurchase(RawPurchaseEntity rawPurchase) {
        repository.save(rawPurchase);
    }

    @Override
    public void saveRawPurchase(List<RawPurchaseEntity> rawPurchaseList) {
        repository.saveAll(rawPurchaseList);
    }

    @Override
    public void updateRawPurchaseStatus(Status newStatus, List<Long> rawPurchaseIds) {
        repository.updateStatusByIds(newStatus, rawPurchaseIds);
    }

    @Override
    public int getScrapingSessionNumber() {
        return repository.findMaxSessionNumber()
                         .map(session -> session.intValue() + 1)
                         .orElse(1);
    }

    @Override
    public ZonedDateTime getLastPurchaseTs(ScrapingSource source) {
        return repository.findMaxLastPurchaseTs(source)
                         .orElse(LocalDateTime.MIN.atZone(ZONE_ID));
    }

    @Override
    public Page<RawPurchaseEntity> findAllBySourceAndStatus(ScrapingSource source, Status status, Pageable pageable) {
        return repository.findAllBySourceAndStatus(source, status, pageable);
    }
}
