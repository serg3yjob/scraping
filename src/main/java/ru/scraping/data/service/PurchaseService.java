package ru.scraping.data.service;

import ru.scraping.data.dao.entity.PurchaseEntity;
import java.util.List;

public interface PurchaseService {

    /**
     * Save purchse list
     *
     * @param purchaseList - list purchse to save
     */
    void savePurchase(List<PurchaseEntity> purchaseList);
}
