package searchengine.services;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import searchengine.dto.statistics.TransferTools;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;

@Data
@Service
@RequiredArgsConstructor
public class AddSQL {
    private ExtractLemma extractLemma = new ExtractLemma();
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    private Set<Index> indexSet = new HashSet<>();
    private List<Lemma> LemmaTransfer = new ArrayList<>();
    private List<TransferTools> listExtractLemmaAndTransferPageId = new ArrayList<>();

    public searchengine.model.Site addSite(searchengine.config.Site siteFromSettings) {
        searchengine.model.Site site = new searchengine.model.Site();
        site.setName(siteFromSettings.getName());
        site.setUrl(siteFromSettings.getUrl());
        site.setStatus(Status.INDEXING);
        site.setLastError("");
        siteRepository.save(site);
        return site;
    }


    public void changeStatusSites() {
        Iterable<searchengine.model.Site> siteEntityIterable = siteRepository.findAll();
        siteEntityIterable.forEach(siteEntity1 -> {
            siteEntity1.setStatus(Status.INDEXED);
            siteRepository.save(siteEntity1);
        });
    }


    public Page addPage(String link, searchengine.model.Site siteId, String url,
                        String content, int code) {
        Page page = new Page();
        page.setSiteId(siteId);
        page.setPath(link.replace(url, ""));
        page.setCode(code);
        page.setContent(content);
        pageRepository.save(page);
        return page;
    }


    public Lemma addLemma(searchengine.model.Site siteId, String lemma, int frequency) {
        Lemma newLemma = new Lemma();
        newLemma.setSiteId(siteId);
        newLemma.setLemma(lemma);
        newLemma.setFrequency(frequency);
        lemmaRepository.save(newLemma);
        return newLemma;
    }


    public void addListExtractLemmaTransferPageId(String lem, Page page) {
        Map<String, Integer> extractorLemma = extractLemma.extractorLemma(lem);

        extractorLemma.forEach((k, v) -> {
            TransferTools tools = new TransferTools();
            tools.setLemma(k);
            tools.setLemmaCountOnePage(v);
            tools.setPage(page);
            listExtractLemmaAndTransferPageId.add(tools);
        });
    }


    public void saveLemmaAndAddListLemma(searchengine.model.Site siteId) {
        Map<String, Integer> map = extractLemma.countFrequency(listExtractLemmaAndTransferPageId);

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String lemmaWord = entry.getKey();
            int frequency = entry.getValue();

            Lemma lemma = addLemma(siteId, lemmaWord, frequency);

            LemmaTransfer.add(lemma);
        }
    }


    public void addIndex() {
        indexRepository.saveAll(indexSet);
    }


    public void addIndexSet() {
        float rank = sizePageList() / 100f;

        for (TransferTools tools : listExtractLemmaAndTransferPageId) {

            Index index = new Index();
            for (Lemma lemma : LemmaTransfer) {
                if (tools.getLemma().equals(lemma.getLemma())) {
                    float rankCount = rank * tools.getLemmaCountOnePage();
                    index.setLemmaId(lemma);
                    index.setPageId(tools.getPage());
                    index.setRank(rankCount);
                }
            }
            indexSet.add(index);
        }
    }


    public int sizePageList() {
        List<Page> pageList = new ArrayList<>();
        Iterable<Page> pageIterable = pageRepository.findAll();
        pageIterable.forEach(pageList::add);
        return pageList.size();
    }


    public int sizeLemmaList() {
        List<Lemma> lemmaList = new ArrayList<>();
        Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();
        lemmaIterable.forEach(lemmaList::add);
        return lemmaList.size();
    }

}

