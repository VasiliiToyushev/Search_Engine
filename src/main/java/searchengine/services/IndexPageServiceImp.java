package searchengine.services;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.IndexPageResponse;
import searchengine.dto.statistics.TransferTools;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Data
@Service
@RequiredArgsConstructor
public class IndexPageServiceImp implements IndexPageService {

    private final AddSQL addSQL;
    private ForkJoinPool pool;
    private ScanSite scanSite;

    @SneakyThrows
    @Override
    public ResponseEntity<IndexPageResponse> getIndexPage(String url) {

        Site siteOld = null;
        Page pageOld = null;

        Iterable<Site> siteIterable = addSQL.getSiteRepository().findAll();
        Iterable<Page> pageIterable = addSQL.getPageRepository().findAll();

        for (Site site : siteIterable) {
            if (url.startsWith(site.getUrl())) siteOld = site;
        }

        if (siteOld == null) {
            return ResponseEntity.ok(new IndexPageResponse(false,
                    "Данная страница находится за пределами сайтов, " +
                            "указанных в конфигурационном файле"));
        } else {
            for (Page page : pageIterable) {
                if (url.equals(siteOld.getUrl().concat(page.getPath()))) {
                    pageOld = page;
                    thereIsPage(url, siteOld, page, pageOld);
                }
            }
            if (pageOld == null) {
                noPage(url, siteOld);
            }
        }
        return ResponseEntity.ok(new IndexPageResponse(true, ""));
    }


    public void noPage(String url, Site siteOld) {
        Set<TransferTools> links = scan(url);

        Page newPage = null;
        for (TransferTools tools : links) {
            newPage = addSQL.addPage(tools.getUrl(), siteOld, siteOld.getUrl(),
                    tools.getContent(), tools.getCode());
            addSQL.addListExtractLemmaTransferPageId(tools.getLemma(), newPage);
        }

        List<TransferTools> list = addSQL.getListExtractLemmaAndTransferPageId();

        addLemmaAndIndexRepositoryAndClearListsAndPoolShutdown(list, siteOld, newPage);
    }


    public void thereIsPage(String url, Site siteOld, Page page, Page pageOld) {
        Set<TransferTools> links = scan(url);

        for (TransferTools tools : links) {
            addSQL.addListExtractLemmaTransferPageId(tools.getLemma(), page);
        }

        List<TransferTools> list = addSQL.getListExtractLemmaAndTransferPageId();

        deletePageLemmaIndex(list, page);

        Page newPage = addSQL.addPage(url, siteOld, siteOld.getUrl(),
                pageOld.getContent(), pageOld.getCode());

        addLemmaAndIndexRepositoryAndClearListsAndPoolShutdown(list, siteOld, newPage);
    }


    public void addLemmaAndIndexRepositoryAndClearListsAndPoolShutdown(List<TransferTools> list, Site siteOld, Page newPage) {
        addLemmaRepository(list, siteOld);
        addIndexRepository(list, newPage);

        list.clear();
        scanSite.getLinks().clear();
        pool.shutdown();
    }


    public Set<TransferTools> scan(String url) {
        scanSite = new ScanSite(url);
        pool = new ForkJoinPool();
        pool.invoke(scanSite);
        return scanSite.getTransferTools();
    }


    public List<Lemma> getListLemmaRepository() {
        List<Lemma> lemmaNewList = new ArrayList<>();
        Iterable<Lemma> lemmaIterable = addSQL.getLemmaRepository().findAll();
        lemmaIterable.forEach(lemmaNewList::add);
        return lemmaNewList;
    }


    public void deletePageLemmaIndex(List<TransferTools> list, Page page) {
        List<Lemma> lemmaNewList = getListLemmaRepository();
        for (Lemma iterable : lemmaNewList) {
            for (TransferTools tools : list) {
                if (iterable.getLemma().equals(tools.getLemma())) {
                    if (iterable.getFrequency() == 1) {
                        addSQL.getLemmaRepository().delete(iterable);
                    } else {
                        Optional<Lemma> optionalLemma =
                                addSQL.getLemmaRepository().findById(iterable.getId());
                        Lemma newLemma = optionalLemma.get();
                        newLemma.setFrequency(iterable.getFrequency() - 1);
                        addSQL.getLemmaRepository().save(newLemma);
                    }
                }
            }
        }
        addSQL.getPageRepository().delete(page);
    }


    public void addLemmaRepository(List<TransferTools> list, Site siteOld) {
        List<Lemma> lemmaNewList = getListLemmaRepository();
        List<TransferTools> lemmaRemoveList = new ArrayList<>();
        List<TransferTools> toolsList = new ArrayList<>(list);
        for (TransferTools tools : toolsList) {
            for (Lemma iterable : lemmaNewList) {
                if (iterable.getLemma().equals(tools.getLemma())) {
                    Optional<Lemma> optionalLemma =
                            addSQL.getLemmaRepository().findById(iterable.getId());
                    Lemma newLemma = optionalLemma.get();
                    newLemma.setFrequency(iterable.getFrequency() + 1);
                    addSQL.getLemmaRepository().save(newLemma);
                    lemmaRemoveList.add(tools);
                }
            }
        }
        toolsList.removeAll(lemmaRemoveList);

        for (TransferTools tools : toolsList) {
            addSQL.addLemma(siteOld, tools.getLemma(), 1);
        }
    }


    public void addIndexRepository(List<TransferTools> list, Page newPage) {
        float rank = addSQL.sizePageList() / 100f;
        List<Lemma> lemmaNewList = getListLemmaRepository();
        System.out.println(lemmaNewList.size());
        System.out.println(list.size());
        for (TransferTools tools : list) {
            for (Lemma lemma : lemmaNewList) {
                if (tools.getLemma().equals(lemma.getLemma())) {
                    Index index = new Index();
                    float rankCount = rank * tools.getLemmaCountOnePage();
                    index.setLemmaId(lemma);
                    index.setPageId(newPage);
                    index.setRank(rankCount);
                    addSQL.getIndexRepository().save(index);
                }
            }
        }
    }

}
