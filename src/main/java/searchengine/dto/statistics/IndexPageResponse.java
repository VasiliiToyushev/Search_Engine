package searchengine.dto.statistics;

import lombok.Data;

@Data
public class IndexPageResponse {
    private boolean result;
    private String error;


    public IndexPageResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
