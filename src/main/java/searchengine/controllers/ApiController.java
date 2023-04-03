package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.*;
import searchengine.services.*;

import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    private final StatisticsServiceImpl statisticsService;
    private final StartIndexingService indexingService;
    private final SearchService searchService;
    private final IndexPageService indexPageService;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }


    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {

        return indexingService.getStartIndexing();
    }


    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {

        return indexingService.getStopIndexing();
    }


    @GetMapping("/search")
    public SearchResponse search(@RequestParam("query") String query,
                                 @RequestParam(name = "site", defaultValue = "") String site) {
        if (query.isBlank()) {
            return new SearchResponse("Задан пустой поисковый запрос",
                    HttpStatus.NOT_FOUND, true);
        }
        Set<DetailedSearchItem> data = searchService.getSearch(query, site);

        if (data.isEmpty()) {
            return new SearchResponse("Founds 0",
                    HttpStatus.NOT_FOUND, true);
        }
        return new SearchResponse(true, 30, data, HttpStatus.OK);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexPageResponse> indexPage(@RequestParam(name = "url", defaultValue = "") String url) {

        return indexPageService.getIndexPage(url);
    }


}
