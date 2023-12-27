package ru.scraping.data.service.impl.magnit;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import ru.scraping.data.config.ScrapingProperties;
import ru.scraping.data.dao.entity.RawPurchaseEntity;
import ru.scraping.data.model.ScrapingResult;
import ru.scraping.data.model.ScrapingSource;
import ru.scraping.data.model.Status;
import ru.scraping.data.model.SupermarketProduct;
import ru.scraping.data.model.SupermarketPurchase;
import ru.scraping.data.service.HashService;
import ru.scraping.data.service.RawPurchaseService;
import ru.scraping.data.service.ScrapingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import static ru.scraping.data.service.impl.magnit.MagnitXPath.*;
import static ru.scraping.data.util.SeleniumUtil.findElement;
import static ru.scraping.data.util.SeleniumUtil.getElement;
import static ru.scraping.data.util.SeleniumUtil.goToPage;
import static ru.scraping.data.util.SeleniumUtil.scrollDown;
import static ru.scraping.data.util.SeleniumUtil.scrollUp;
import static ru.scraping.data.util.SeleniumUtil.setImplicitlyWait;
import static ru.scraping.data.util.SeleniumUtil.timeoutInSec;
import static ru.scraping.data.util.SeleniumUtil.waitUntil;
import static ru.scraping.data.util.Util.parseFloats;

@Slf4j
@Service
@RequiredArgsConstructor
public class MagnitScrapingServiceImpl implements ScrapingService {

    private static final String ZERO_STR = "0";
    private static final String EMPTY_STR = "";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm");
    private static final ZoneId ZONE_ID = ZoneId.of("UTC+5");

    private final HashService hashService;
    private final RawPurchaseService rawPurchaseService;
    private final ScrapingProperties scrapingProperties;
    private final PlatformTransactionManager transactionManager;

    private TransactionTemplate tx;

    @PostConstruct
    public void init() {
        tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public ScrapingResult scrapeData() {
        log.info("#magnitScrapeData: started");
        WebDriver driver = new ChromeDriver();

        log.debug("#magnitScrapeData: go to today page");
        goToTodayPage(driver);

        log.debug("#magnitScrapeData: go to purchase list page");
        goToPurchaseListPage(driver);
        log.debug("#magnitScrapeData: purchase list page is opened");

        log.debug("#magnitScrapeData: find all purchase");
        List<WebElement> allPurchase = findAllPurchase(driver);
        log.info("#magnitScrapeData: found {} purchases", allPurchase.size());

        int scrapingSessionNumber = rawPurchaseService.getScrapingSessionNumber();
        ZonedDateTime maxTs = rawPurchaseService.getLastPurchaseTs(ScrapingSource.MAGNIT);
        int counter = 0;
        ListIterator<WebElement> listIterator = allPurchase.listIterator(allPurchase.size());
        while (listIterator.hasPrevious()) {
            WebElement purchase = listIterator.previous();
            SupermarketPurchase supermarketPurchase = scrapeData(purchase, maxTs, driver);
            if (supermarketPurchase != null) {
                savePurchase(supermarketPurchase, scrapingSessionNumber);
                counter++;
            }
        }
        if (counter > 0) {
            log.info("#magnitScrapeData: {} purchases saved", counter);
        }

        log.info("#magnitScrapeData: finished");
        return new ScrapingResult(scrapingSessionNumber, allPurchase.size(), counter);
    }

    private void goToPurchaseListPage(WebDriver driver) {
        WebElement toPurchaseButton = getElement(XPATH_TO_PURCHASE_BUTTON.xPath(), driver);
        toPurchaseButton.click();
        waitUntil(XPATH_FIRST_PURCHASE.xPath(), 60, driver);
    }

    private void goToTodayPage(WebDriver driver) {
        goToPage(scrapingProperties.getMagnit().getTodayUrl(), driver);
        timeoutInSec(2 * 60);
    }

    private void savePurchase(SupermarketPurchase supermarketPurchase, int session) {
        log.debug("#magnitScrapeData: saving purchase [{}]", supermarketPurchase);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC+5"));
        RawPurchaseEntity rawPurchase = RawPurchaseEntity.builder()
                .hash(supermarketPurchase.hash())
                .status(Status.NEW)
                .systemTs(now)
                .purchaseTs(now)
                .json(supermarketPurchase)
                .session(session)
                .source(ScrapingSource.MAGNIT)
                .build();
        tx.executeWithoutResult(transactionStatus -> rawPurchaseService.saveRawPurchase(rawPurchase));
        log.debug("#magnitScrapeData: purchase saved");
    }

    private SupermarketPurchase scrapeData(WebElement purchase, ZonedDateTime maxTs, WebDriver driver) {
        setImplicitlyWait(60, driver);

        if (purchaseIsOld(maxTs, purchase)) {
            return null;
        }
        clickPurchaseWithScrolling(purchase, driver);

        WebElement closeBtn = getElement(XPATH_PURCHASE_CLOSE_BTN.xPath(), driver);
        WebElement date = getElement(XPATH_PURCHASE_DATE.xPath(), driver);
        setImplicitlyWait(0, driver);
        WebElement time = getElement(XPATH_PURCHASE_TIME.xPath(), driver);
        WebElement cost = findElement(XPATH_PURCHASE_COST.xPath(), driver);
        WebElement bonus = findElement(XPATH_PURCHASE_BONUS.xPath(), driver);
        WebElement marketAddress = findElement(XPATH_PURCHASE_MARKET_ADDRESS.xPath(), driver);

        List<SupermarketProduct> productList = getProductList(driver);
        SupermarketPurchase supermarketPurchase = null;
        if (CollectionUtils.isNotEmpty(productList)) {
            BigDecimal costParsed = parseFloats(Optional.ofNullable(cost).map(WebElement::getText).orElse(ZERO_STR));
            supermarketPurchase = new SupermarketPurchase(
                    getPurchaseHash(date, time, marketAddress, costParsed),
                    date.getText() + time.getText(),
                    Optional.ofNullable(cost).map(WebElement::getText).orElse(ZERO_STR),
                    Optional.ofNullable(bonus).map(WebElement::getText).orElse(ZERO_STR),
                    Optional.ofNullable(marketAddress).map(WebElement::getText).orElse(EMPTY_STR),
                    productList

            );
        }

        closeBtn.click();
        timeoutInSec(2);
        return supermarketPurchase;
    }

    private boolean purchaseIsOld(ZonedDateTime maxTs, WebElement purchase) {
        ZonedDateTime purchaseTs =
                parsePurchaseTs(purchase.findElement(RelativeLocator.RelativeBy.xpath(XPATH_PURCHASE_TIMESTAMP.xPath())).getText());
        return purchaseTs.isBefore(maxTs);
    }

    private String getPurchaseHash(WebElement date, WebElement time, WebElement marketAddress, BigDecimal costParsed) {
        StringBuilder builder = new StringBuilder(date.getText());
        builder.append(time.getText())
               .append(Optional.ofNullable(costParsed).map(BigDecimal::toString).orElse(ZERO_STR))
               .append(Optional.ofNullable(marketAddress).map(WebElement::getText).orElse(EMPTY_STR));
        return hashService.hash(builder.toString());
    }

    private void clickPurchaseWithScrolling(WebElement purchase, WebDriver driver) {
        while (true) {
            try {
                purchase.click();
            } catch (Exception e1) {
                scrollUp(driver);
                continue;
            }
            break;
        }
    }

    private List<SupermarketProduct> getProductList(WebDriver driver) {
        List<SupermarketProduct> productList = new ArrayList<>();
        for (int div = 1; ; div++) {
            try {
                WebElement productName = getProductName(div, driver);
                WebElement productCost = getProductCost(div, driver);
                WebElement productQuantity = getProductQuantity(div, driver);
                productList.add(new SupermarketProduct(productName.getText(),
                                                       productCost.getText(),
                                                       productQuantity.getText()));

            } catch (Exception e) {
                break;
            }
        }
        return productList;
    }

    private List<WebElement> findAllPurchase(WebDriver driver) {
        setImplicitlyWait(0, driver);
        scrollDown(driver);
        timeoutInSec(5);

        List<WebElement> purchaseList = new ArrayList<>();

        for (int section = 1; ; section++) {
            for (int div = 1; ; div++) {
                WebElement purchaseElement = findPurchaseElementWithScrolling(driver, section, div);
                if (purchaseElement == null) {
                    break;
                }
                purchaseList.add(purchaseElement);
            }
            if (findPurchaseElementWithScrolling(driver, section + 1, 1) == null) {
                break;
            }
        }

        return purchaseList;
    }

    private WebElement findPurchaseElementWithScrolling(WebDriver driver, int section, int div) {
        WebElement purchaseElement = findPurchaseElement(section, div, driver);
        if (purchaseElement == null) {
            scrollDown(driver);
            timeoutInSec(5);
            purchaseElement = findPurchaseElement(section, div, driver);
        }
        return purchaseElement;
    }

    private WebElement findPurchaseElement(int section, int div, WebDriver driver) {
        String xPath = EMPTY_STR;
        if (section == 1) {
            xPath = String.format(XPATH_PURCHASE_SECTION_1.xPath(), div);
        } else if (section > 1) {
            xPath = String.format(XPATH_PURCHASE_SECTION_MORE_1.xPath(), section, div);
        }
        return findElement(xPath, driver);
    }

    private WebElement getProductName(int div, WebDriver driver) {
        String xPath = String.format(XPATH_PRODUCT_NAME.xPath(), div);
        return getElement(xPath, driver);
    }

    private WebElement getProductCost(int div, WebDriver driver) {
        String xPath = String.format(XPATH_PRODUCT_COST.xPath(), div);
        return getElement(xPath, driver);
    }

    private WebElement getProductQuantity(int div, WebDriver driver) {
        String xPath = String.format(XPATH_PRODUCT_QUANTITY.xPath(), div);
        return getElement(xPath, driver);
    }

    private ZonedDateTime parsePurchaseTs(String purchaseTimestamp) {
        return LocalDateTime.parse(purchaseTimestamp, FORMATTER).atZone(ZONE_ID);
    }
}
