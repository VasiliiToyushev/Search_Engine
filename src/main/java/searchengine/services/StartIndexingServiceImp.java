package searchengine.services;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesFromSettingList;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.dto.statistics.TransferTools;
import searchengine.model.Page;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Data
@Service
@RequiredArgsConstructor
public class StartIndexingServiceImp implements StartIndexingService {
    private final AddSQL addSQL;
    private final SitesFromSettingList sitesFromSettingList;
    private boolean indexing = false;
    private boolean indexed = false;
    private boolean failed = false;

    private List<Thread> threadList = new ArrayList<>();
    private ForkJoinPool pool;

    @SneakyThrows
    public ResponseEntity<IndexingResponse> getStartIndexing() {
        if (isIndexing()) {
            return ResponseEntity.ok(new IndexingResponse(true, "Индексация уже запущена"));
        }
        deleteRepository();

        setIndexing(true);
        setFailed(false);

        long start = System.currentTimeMillis();

        for (Site site : sitesFromSettingList.getSites()) {

            threadList.add(new Thread(() -> addThreadList(site, start)));
        }
        threadList.forEach(Thread::start);

        System.out.println(System.currentTimeMillis() - start);

        return ResponseEntity.ok(new IndexingResponse(true, "Индексация запущена"));
    }


    public void addThreadList(Site siteFromSettings, long start) {
        synchronized (this) {
            searchengine.model.Site site = addSQL.addSite(siteFromSettings);

            ScanSite scanSite = new ScanSite(siteFromSettings.getUrl());

            pool = new ForkJoinPool();
            pool.invoke(scanSite);

            Set<TransferTools> links = scanSite.getTransferTools();
            extractLinks(links, site, siteFromSettings.getUrl());

            addRepositoryAndChangeStatus(site);

            clearList(links, scanSite);
            pool.shutdown();
        }
        setIndexing(false);
        setIndexed(true);

        System.out.println(System.currentTimeMillis() - start);
    }


    public void addRepositoryAndChangeStatus(searchengine.model.Site site) {
        addSQL.saveLemmaAndAddListLemma(site);

        addSQL.getLemmaRepository().saveAll(addSQL.getLemmaTransfer());

        addSQL.addIndexSet();
        addSQL.addIndex();

        addSQL.changeStatusSites();
    }


    public void deleteRepository() {
        addSQL.getIndexRepository().deleteAll();
        addSQL.getLemmaRepository().deleteAll();
        addSQL.getPageRepository().deleteAll();
        addSQL.getSiteRepository().deleteAll();
    }


    public void extractLinks(Set<TransferTools> links, searchengine.model.Site site, String url) {
        for (TransferTools link : links) {
            Page page = addSQL.addPage(link.getUrl(), site, url,
                    link.getContent(), link.getCode());

            addSQL.addListExtractLemmaTransferPageId(link.getLemma(), page);
        }
    }


    public void clearList(Set<TransferTools> links, ScanSite scanSite) {
        scanSite.getLinks().clear();
        links.clear();
        addSQL.getIndexSet().clear();
        addSQL.getListExtractLemmaAndTransferPageId().clear();
        addSQL.getLemmaTransfer().clear();
    }


    @Override
    @SneakyThrows
    public ResponseEntity<IndexingResponse> getStopIndexing() {
        if (!isIndexing()) {
            threadList.clear();
            return ResponseEntity.ok(new IndexingResponse(true, "Индексация не запущена"));
        }
        pool.shutdownNow();
        threadList.forEach(Thread::stop);
        threadList.clear();
        setIndexing(false);
        setFailed(true);
        return ResponseEntity.ok(new IndexingResponse(true, "Индексация остановлена"));
    }

}
