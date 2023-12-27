package ru.scraping.data.service;

public interface HashService {

    /**
     * Hashing input string
     *
     * @param toHashStr - input string
     * @return - hash in base64 encoding
     */
    String hash(String toHashStr);
}
