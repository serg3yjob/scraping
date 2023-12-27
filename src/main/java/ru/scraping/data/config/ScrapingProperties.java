package ru.scraping.data.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.scraping")
@Getter
@Setter
public class ScrapingProperties {

    private Magnit magnit;

    @Getter
    @Setter
    public static class Magnit {

        private String startUrl;
        private String todayUrl;
    }
}
