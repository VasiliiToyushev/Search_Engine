package searchengine.services;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesFromSettingList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final Random random = new Random();
    private final SitesFromSettingList sites;
    private final AddSQL addSQL;
    private final StartIndexingServiceImp startIndexingServiceImp;


    public StatisticsResponse statistics(String statuses, String errors) {
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (Site site : sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = addSQL.sizePageList();
            int lemmas = addSQL.sizeLemmaList();
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(statuses);
            item.setError(errors);
            item.setStatusTime(System.currentTimeMillis() -
                    (random.nextInt(10_000)));
            total.setPages(pages);
            total.setLemmas(lemmas);
            detailed.add(item);
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = {"INDEXED", "FAILED", "INDEXING"};
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                "Индексация остановлена пользователем",
                ""
        };
        if (!startIndexingServiceImp.isIndexing() &&
                !startIndexingServiceImp.isIndexed() &&
                !startIndexingServiceImp.isFailed())
            return statistics("", "");

        else if (startIndexingServiceImp.isIndexing())
            return statistics(statuses[2],
                    errors[3]);

        else if (startIndexingServiceImp.isFailed())
            return statistics(statuses[1],
                    errors[2]);

        else return statistics(statuses[0],
                    errors[3]);
    }
}
