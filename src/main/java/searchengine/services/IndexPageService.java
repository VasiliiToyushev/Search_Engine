package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.statistics.IndexPageResponse;

public interface IndexPageService {
    ResponseEntity<IndexPageResponse> getIndexPage(String query);
}
