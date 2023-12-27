package ru.scraping.data.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Unit {

    /**
     * PACS unit
     */
    PACS("шт"),
    /**
     * KG unit
     */
    KG("кг");

    private final String name;

    public String nameStr() {
        return this.name;
    }
}
