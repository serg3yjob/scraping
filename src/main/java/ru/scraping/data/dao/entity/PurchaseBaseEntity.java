package ru.scraping.data.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
public class PurchaseBaseEntity extends BaseEntity {

    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    @Column(name = "system_ts")
    private ZonedDateTime systemTs;

    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    @Column(name = "purchase_ts")
    private ZonedDateTime purchaseTs;
}
