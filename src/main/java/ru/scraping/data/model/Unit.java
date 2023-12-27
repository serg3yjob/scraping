package ru.scraping.data.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Unit {
    PACS("шт"),
    KG("кг");

    private final String name;

    public String nameStr() {
        return this.name;
    }
}
