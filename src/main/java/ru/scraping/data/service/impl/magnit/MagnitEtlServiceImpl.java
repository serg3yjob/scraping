package ru.scraping.data.service.impl.magnit;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import ru.scraping.data.dao.entity.ProductEntity;
import ru.scraping.data.dao.entity.PurchaseEntity;
import ru.scraping.data.dao.entity.RawPurchaseEntity;
import ru.scraping.data.model.ScrapingSource;
import ru.scraping.data.model.Status;
import ru.scraping.data.model.SupermarketPurchase;
import ru.scraping.data.model.Unit;
import ru.scraping.data.service.EtlService;
import ru.scraping.data.service.PurchaseService;
import ru.scraping.data.service.RawPurchaseService;
import ru.scraping.data.util.Util;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static ru.scraping.data.model.Unit.KG;
import static ru.scraping.data.model.Unit.PACS;

@Slf4j
@Service
@RequiredArgsConstructor
public class MagnitEtlServiceImpl implements EtlService {

    private static final int PAGE_SIZE = 10;
    private static final String COLON = ":";
    private static final String SPACE = " ";
    private static final String EMPTY_STR = "";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm");
    private static final ZoneId ZONE_ID = ZoneId.of("UTC+5");

    private final RawPurchaseService rawPurchaseService;
    private final PurchaseService purchaseService;
    private final PlatformTransactionManager transactionManager;

    private TransactionTemplate tx;

    @PostConstruct
    public void init() {
        tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void etl() {
        log.info("#magnitEtlProcess: started");
        while (true) {
            Pageable pageRequest =
                    PageRequest.of(0, PAGE_SIZE, Sort.by("purchaseTs").ascending());
            Page<RawPurchaseEntity> purchasesPage =
                    rawPurchaseService.findAllBySourceAndStatus(ScrapingSource.MAGNIT, Status.NEW, pageRequest);

            if (purchasesPage.hasContent()) {
                log.debug("#magnitEtlProcess: found page with data, content size [{}]",
                          purchasesPage.getContent().size());
                List<PurchaseEntity> purchaseList = transformToPurchase(purchasesPage.getContent());
                log.info("#magnitEtlProcess: {} purchase transformed", purchaseList.size());
                tx.executeWithoutResult(status -> loadPurchase(purchaseList, purchasesPage.getContent()));
                log.info("#magnitEtlProcess: {} purchase saved", purchaseList.size());
                continue;
            }

            break;
        }
        log.info("#magnitEtlProcess: finished");
    }

    private List<PurchaseEntity> transformToPurchase(List<RawPurchaseEntity> rawPurchaseList) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        List<PurchaseEntity> purchaseList = new ArrayList<>(rawPurchaseList.size());

        rawPurchaseList.forEach(rawPurchase -> {
            SupermarketPurchase supermarketPurchase = rawPurchase.getJson();
            PurchaseEntity purchase = new PurchaseEntity();
            List<ProductEntity> products = new ArrayList<>(supermarketPurchase.products().size());

            supermarketPurchase.products().forEach(supermarketProduct -> {
                String name = parseName(supermarketProduct.name());
                BigDecimal quantity = parseQuantity(supermarketProduct.quantity());
                Unit unit = parseUnit(supermarketProduct.quantity());
                BigDecimal totalCost = parseCost(supermarketProduct.cost());
                BigDecimal unitCost = totalCost.divide(quantity,
                                                       RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
                ProductEntity product = new ProductEntity()
                        .setName(name)
                        .setQuantity(quantity)
                        .setUnit(unit)
                        .setTotalCost(totalCost)
                        .setUnitCost(unitCost)
                        .setPurchase(purchase);
                products.add(product);
            });

            purchase.setStatus(Status.NEW);
            purchase.setCost(calculatePurchaseCost(products));
            purchase.setBonus(parseBonus(supermarketPurchase.bonus()));
            purchase.setPurchaseTs(parsePurchaseTs(supermarketPurchase.purchaseTimestamp()));
            purchase.setMarketAddress(supermarketPurchase.marketAddress());
            purchase.setRawPurchase(rawPurchase);
            purchase.setProducts(products);
            purchase.setSystemTs(now);
            purchaseList.add(purchase);
        });

        return purchaseList;
    }

    private ZonedDateTime parsePurchaseTs(String purchaseTimestamp) {
        return LocalDateTime.parse(purchaseTimestamp, FORMATTER).atZone(ZONE_ID);
    }

    private BigDecimal parseBonus(String bonus) {
        return Optional.ofNullable(bonus)
                       .map(Util::parseFloats)
                       .orElse(BigDecimal.ZERO);
    }

    private BigDecimal parseCost(String cost) {
        return Optional.ofNullable(cost)
                       .map(str -> str.replace(SPACE, EMPTY_STR))
                       .map(Util::parseFloats)
                       .orElse(BigDecimal.ZERO);
    }

    private Unit parseUnit(String quantity) {
        if (Optional.ofNullable(quantity)
                    .orElse(PACS.nameStr())
                    .contains(PACS.nameStr())) {
            return PACS;
        }
        return KG;
    }

    private BigDecimal parseQuantity(String quantity) {
        return Optional.ofNullable(quantity)
                       .map(str -> str.split(SPACE)[0])
                       .map(Util::parseFloats)
                       .orElse(BigDecimal.ONE);
    }

    private String parseName(String name) {
        return Optional.ofNullable(name)
                       .map(str -> str.split(COLON)[0])
                       .orElse(EMPTY_STR);
    }

    private BigDecimal calculatePurchaseCost(List<ProductEntity> products) {
        return products.stream()
                       .map(productEntity ->
                                    productEntity.getQuantity()
                                                 .multiply(productEntity.getUnitCost())
                                                 .setScale(2, RoundingMode.HALF_UP))
                       .reduce(BigDecimal.ZERO, (cost1, cost2) -> cost1.add(cost2)
                                                                       .setScale(2, RoundingMode.HALF_UP));
    }

    private void loadPurchase(List<PurchaseEntity> purchaseList, List<RawPurchaseEntity> rawPurchaseList) {
        purchaseService.savePurchase(purchaseList);
        rawPurchaseService.updateRawPurchaseStatus(Status.PROCESSED,
                                                   rawPurchaseList.stream()
                                                                  .map(RawPurchaseEntity::getId)
                                                                  .toList());
    }
}
