package searchengine.services;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import searchengine.dto.statistics.TransferTools;


import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class ScanSite extends RecursiveAction {

    private final String url;
    private static Set<String> links = new TreeSet<>();
    private static Set<TransferTools> transferTools = new TreeSet<>(Comparator.comparing(TransferTools::getUrl));

    public ScanSite(String url) {
        this.url = url.trim();
    }

    public Set<TransferTools> getTransferTools() {
        return transferTools;
    }

    public Set<String> getLinks() {
        return links;
    }


    @SneakyThrows
    @Override
    protected void compute() {
        List<ScanSite> taskList = new ArrayList<>();

        var jsoup = Jsoup.connect(url);

        Document doc = jsoup.ignoreContentType(true)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) " +
                        "Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .ignoreHttpErrors(true).get();

        int docCode = jsoup.response().statusCode();

        String docHtml = doc.outerHtml();
        TransferTools tools = new TransferTools();
        tools.setUrl(url);
        if (docCode != 200) docHtml = "error";
        tools.setContent(docHtml);
        tools.setCode(docCode);
        tools.setLemma(doc.text());
        transferTools.add(tools);

        for (Element el : doc.select("a")) {
            String atr = el.absUrl("href");
            if (atr.startsWith(url) && !links.contains(atr) && !atr.contains("#")) {
                ScanSite task = new ScanSite(atr);
                task.fork();
                taskList.add(task);
                links.add(atr);
            }
        }
        taskList.forEach(ForkJoinTask::join);
    }
}
