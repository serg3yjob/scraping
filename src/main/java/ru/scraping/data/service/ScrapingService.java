package ru.scraping.data.service;

import ru.scraping.data.model.ScrapingResult;

public interface ScrapingService {

    /**
     * Start and perform data scraping from web
     *
     * @return ScrapingResult
     */
    ScrapingResult scrapeData();
}
