package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.statistics.IndexingResponse;

public interface StartIndexingService {
    ResponseEntity<IndexingResponse> getStartIndexing();

    ResponseEntity<IndexingResponse> getStopIndexing();

}
