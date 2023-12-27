package ru.scraping.data.model;

import java.io.Serializable;
import java.util.List;

public record SupermarketPurchase(
        String hash,
        String purchaseTimestamp,
        String cost,
        String bonus,
        String marketAddress,
        List<SupermarketProduct> products) implements Serializable {
}
