package searchengine.services;

import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.dto.statistics.TransferTools;

import java.util.*;

public class ExtractLemma {

    @SneakyThrows
    public Map<String, Integer> extractorLemma(String lem) {
        String str = lem.replaceAll("[^А-яёЁ\\s]", "").toLowerCase(Locale.ROOT);
        String[] mas = str.split("\\s+");
        List<String> wordBaseForms = new ArrayList<>();

        LuceneMorphology luceneMorph = new RussianLuceneMorphology();

        for (String item : mas) {
            if (item.isBlank()) {
                continue;
            }
            List<String> listInfo = luceneMorph.getMorphInfo(item);
            String info = listInfo.toString().replaceAll("^А-яёЁ", "");
            if (!(info.contains("ПРЕДЛ") ||
                    info.contains("МЕЖД") ||
                    info.contains("СОЮЗ"))) {

                wordBaseForms.addAll(luceneMorph.getNormalForms(item));
            }
        }
        Map<String, Integer> mapLemmaCountOnePage = new TreeMap<>(String::compareTo);
        mapLemmaCountOnePage = LemmaCountOnePage(wordBaseForms, mapLemmaCountOnePage);
        return mapLemmaCountOnePage;
    }


    public Map<String, Integer> LemmaCountOnePage(List<String> wordBaseForms, Map<String, Integer> mapLemmaCountOnePage) {
        wordBaseForms.forEach(word -> {
            int count = mapLemmaCountOnePage.getOrDefault(word, 0);
            mapLemmaCountOnePage.put(word, count + 1);
        });
        return mapLemmaCountOnePage;
    }


    public Map<String, Integer> countFrequency(List<TransferTools> listLemmas) {

        Map<String, Integer> map = new TreeMap<>(String::compareTo);

        int count;
        for (TransferTools list : listLemmas) {
            count = map.getOrDefault(list.getLemma(), 0);
            map.put(list.getLemma(), count + 1);

        }
        return map;
    }
}
