package ru.scraping.data.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.scraping.data.dao.entity.PurchaseEntity;
import ru.scraping.data.dao.repository.PurchaseRepository;
import ru.scraping.data.service.PurchaseService;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository repository;

    @Override
    public void savePurchase(List<PurchaseEntity> purchaseList) {
        repository.saveAll(purchaseList);
    }
}
