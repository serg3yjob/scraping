package ru.scraping.data.model;

import java.io.Serializable;

public record SupermarketProduct(
        String name,
        String cost,
        String quantity) implements Serializable {
}
