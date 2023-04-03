package searchengine.services;

import lombok.SneakyThrows;
import searchengine.dto.statistics.DetailedSearchItem;

import java.util.Set;

public interface SearchService {
    @SneakyThrows
    Set<DetailedSearchItem> getSearch(String query, String site);
}
