package ru.scraping.data.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.scraping.data.model.ScrapingSource;
import ru.scraping.data.model.Status;
import ru.scraping.data.model.SupermarketPurchase;

@SuperBuilder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "raw_purchase", schema = "scraping")
public class RawPurchaseEntity extends PurchaseBaseEntity {

    @Column(name = "hash")
    private String hash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private ScrapingSource source;

    @Column(name = "session")
    private Integer session;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json")
    private SupermarketPurchase json;
}
