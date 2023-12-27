package ru.scraping.data.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.scraping.data.model.ScrapingResult;
import ru.scraping.data.service.EtlService;
import ru.scraping.data.service.ScrapingService;
import static ru.scraping.data.controller.URL.*;

@RestController
@Tag(name = "Scraping API")
@RequiredArgsConstructor
public class ScrapingController {

    private final ScrapingService scrapingService;
    private final EtlService etlService;

    @Operation(description = "Start purchase data scraping from magnit supermarket personal account")
    @ApiResponse(responseCode = "200",
                 description = "Scrapping started and finished successfully",
                 content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ScrapingResult.class)))
    @ApiResponse(responseCode = "503", description = "Service unavailable")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping(name = MAGNIT_SCRAPING_START)
    public ScrapingResult startMagnitScraping() {
        return scrapingService.scrapeData();
    }

    @Operation(description = "Start and perform ETL process for source = MAGNIT")
    @ApiResponse(responseCode = "200", description = "ETL process started and finished successfully")
    @ApiResponse(responseCode = "503", description = "Service unavailable")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PutMapping(name = MAGNIT_ETL_START)
    public void startMagnitEtl() {
        etlService.etl();
    }
}
