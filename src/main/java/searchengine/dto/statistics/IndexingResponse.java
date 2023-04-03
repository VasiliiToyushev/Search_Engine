package searchengine.dto.statistics;

import lombok.Data;

@Data
public class IndexingResponse {
    private boolean result;
    private String massage;

    public IndexingResponse(boolean result, String massage) {
        this.result = result;
        this.massage = massage;
    }
}
