package ru.scraping.data.dao.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.scraping.data.model.Status;
import java.math.BigDecimal;
import java.util.List;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "purchase", schema = "scraping")
public class PurchaseEntity extends PurchaseBaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "cost")
    private BigDecimal cost;

    @Column(name = "bonus")
    private BigDecimal bonus;

    @Column(name = "market_address")
    private String marketAddress;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_purchase_id")
    private RawPurchaseEntity rawPurchase;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "purchase")
    private List<ProductEntity> products;
}
