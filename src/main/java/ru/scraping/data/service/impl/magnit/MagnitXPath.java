package ru.scraping.data.service.impl.magnit;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MagnitXPath {

    XPATH_TO_PURCHASE_BUTTON("//*[@id=\"__nuxt\"]/div[2]/div/div[4]/a"),
    XPATH_FIRST_PURCHASE("//*[@id=\"__nuxt\"]/div[2]/div/div[3]/div[1]/section[1]/div/div[1]"),
    XPATH_PURCHASE_CLOSE_BTN("/html/body/div[5]/div[1]/div[1]/div/div[3]/div/div"),
    XPATH_PURCHASE_DATE("/html/body/div[5]/div[1]/div[2]/div/div/section[1]/time/span[1]"),
    XPATH_PURCHASE_TIME("/html/body/div[5]/div[1]/div[2]/div/div/section[1]/time/span[2]"),
    XPATH_PURCHASE_COST("/html/body/div[5]/div[1]/div[2]/div/div/section[2]/div[8]/ul/li[2]/span[3]"),
    XPATH_PURCHASE_BONUS("/html/body/div[5]/div[1]/div[2]/div/div/section[3]/ul/li[1]/div/span"),
    XPATH_PURCHASE_MARKET_ADDRESS("/html/body/div[5]/div[1]/div[2]/div/div/section[4]/div[2]/span"),
    XPATH_PURCHASE_SECTION_1("//*[@id=\"__nuxt\"]/div[2]/div/div[3]/div[1]/section[1]/div/div[%d]"),
    XPATH_PURCHASE_SECTION_MORE_1("//*[@id=\"__nuxt\"]/div[2]/div/div[3]/div[1]/section[%d]/div[2]/div[%d]"),
    XPATH_PRODUCT_NAME("/html/body/div[5]/div[1]/div[2]/div/div/section[2]/div[%d]/span"),
    XPATH_PRODUCT_COST("/html/body/div[5]/div[1]/div[2]/div/div/section[2]/div[%d]/div/div[1]"),
    XPATH_PRODUCT_QUANTITY("/html/body/div[5]/div[1]/div[2]/div/div/section[2]/div[%d]/div/div[2]"),
    XPATH_PURCHASE_TIMESTAMP("//div[2]/span");

    private final String xPath;

    public String xPath() {
        return this.xPath;
    }
}
