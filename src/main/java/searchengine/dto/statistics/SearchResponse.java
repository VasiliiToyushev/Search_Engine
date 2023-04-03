package searchengine.dto.statistics;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Set;

@Data
public class SearchResponse {
    private String error;
    private int count;
    private HttpStatus status;
    private Set<DetailedSearchItem> data;
    private boolean result;

    public SearchResponse(boolean result, int count, Set<DetailedSearchItem> data, HttpStatus status) {
        this.count = count;
        this.status = status;
        this.data = data;
        this.result = result;
    }

    public SearchResponse(String error, HttpStatus status, boolean result) {
        this.error = error;
        this.status = status;
        this.result = result;
    }
}
