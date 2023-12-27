package ru.scraping.data.model;

import java.io.Serializable;

public record ScrapingResult(
        int scrapingSession,
        int allPurchaseFound,
        int allPurchaseSaved) implements Serializable {
}
