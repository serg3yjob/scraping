package ru.scraping.data.controller;

public final class URL {

    private URL() {

    }

    /**
     * API_V1
     */
    public static final String API_V1 = "/api/v1";
    /**
     * SCRAPING
     */
    public static final String SCRAPING = API_V1 + "/scrapping";
    /**
     * MAGNIT_SCRAPING_START
     */
    public static final String MAGNIT_SCRAPING_START = SCRAPING + "/magnit/start-scraping";
    /**
     * MAGNIT_ETL_START
     */
    public static final String MAGNIT_ETL_START = SCRAPING + "/magnit/start-etl";
}
