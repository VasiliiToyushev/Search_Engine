package searchengine.dto.statistics;

import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
public class StatisticsResponse {
    private boolean result;
    private StatisticsData statistics;

    public StatisticsResponse(boolean result) {
        this.result = result;
    }
    public StatisticsResponse() {

    }

    public StatisticsResponse(boolean result, StatisticsData statistics) {
        this.result = result;
        this.statistics = statistics;
    }
}
