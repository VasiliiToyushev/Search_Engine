package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.DatasourceSQL;
import searchengine.dto.statistics.DetailedSearchItem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImp implements SearchService {
    private final DatasourceSQL datasourceSQL;

    @SneakyThrows
    @Override
    public Set<DetailedSearchItem> getSearch(String query, String sites) {
        Statement statement = connectSql();

        String queryLow = query.toLowerCase().trim();
        String[] mas = queryLow.split("\\s+");

        Set<DetailedSearchItem> data = new TreeSet<>(Comparator.comparing(DetailedSearchItem::getRelevance).thenComparing(DetailedSearchItem::getTitle));
        Set<DetailedSearchItem> list = new HashSet<>();

        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        for (String s : mas) {
            searchLemma(luceneMorph, s, sites, statement, list, mas, data);
        }
        data = sumRank(list, data);
        return data;
    }


    @SneakyThrows
    public void searchLemma(LuceneMorphology luceneMorph, String s, String sites, Statement statement, Set<DetailedSearchItem> list, String[] mas, Set<DetailedSearchItem> data) {
        List<String> luceneMorphologyList = luceneMorph.getNormalForms(s);

        String morph = luceneMorphologyList.get(0).replaceAll("[^А-яёЁ]", "");

        String select;
        if (sites.equals("")) select = selectSql(morph);

        else select = select2Sql(morph, sites);

        ResultSet resultSet = statement.executeQuery(select);

        resultSetExtract(resultSet, s, list, mas, data, morph);
    }


    @SneakyThrows
    public void resultSetExtract(ResultSet resultSet, String s, Set<DetailedSearchItem> list, String[] mas, Set<DetailedSearchItem> data, String morph) {
        while (resultSet.next()) {
            String path = resultSet.getString("path");
            String siteName = resultSet.getString("name");
            String site = resultSet.getString("url");
            String rank = resultSet.getString("rank");
            String frequency = resultSet.getString("frequency");
            int frequencyInt = Integer.parseInt(frequency);
            float rankF = (float) Double.parseDouble(rank);
            String content = resultSet.getString("content");

            String title = substringTitle(content);

            String snippet = snip(content, s);

            DetailedSearchItem detailedSearchItem = addDetailedSearchItem(site, siteName, path, title, snippet, rankF, morph, frequencyInt);
            list.add(detailedSearchItem);

            if (mas.length == 1) {
                if (data.size() >= 30) break;
                data.add(detailedSearchItem);
            }
        }
    }


    public String snip(String content, String s) {
        Document doc = Jsoup.parse(content);
        Elements el = doc.getElementsContainingOwnText(s);
        String first = s.replaceFirst(String.valueOf(s.charAt(0)),String.valueOf(s.charAt(0)).toUpperCase());
        String replace =  "<b>"+ first +"</b>";
        String snipEl = el.text().replaceAll(first,replace);
        String snippet;
        if (snipEl.length() >= 200) snippet = snipEl.substring(0, 200);
        else snippet = snipEl;
        return snippet;
    }


    public String substringTitle(String content) {
        Document doc = Jsoup.parse(content);
        return doc.title();
    }


    public String selectSql(String morph) {
        return "select p.path, s.name, s.url, i.rank, l.frequency, p.content from `index` i " +
                "join `lemma` l on i.lemma_id = l.id " +
                "join `page` p on i.page_id = p.id " +
                "join `site` s on p.site_id = s.id  or l.site_id = s.id " +
                "where l.lemma like  '%" + morph + "%'";
    }


    public String select2Sql(String morph, String sites) {
        return "SELECT p.path, s.name, s.url, i.rank, l.frequency, p.content from `index` i " +
                "join `lemma` l on i.lemma_id = l.id " +
                "join `page` p on i.page_id = p.id " +
                "join `site` s on p.site_id = s.id or l.site_id = s.id " +
                "where s.url like '" + sites + "' and " +
                "lemma like  '%" + morph + "%'";
    }

    public DetailedSearchItem addDetailedSearchItem(String site, String siteName, String path, String substringTitle, String substringSnippet, float rankF, String morph, int frequencyInt) {
        DetailedSearchItem detailedSearchItem = new DetailedSearchItem();
        detailedSearchItem.setSite(site);
        detailedSearchItem.setSiteName(siteName);
        detailedSearchItem.setUri(path);
        detailedSearchItem.setTitle(substringTitle);
        detailedSearchItem.setSnippet(substringSnippet);
        detailedSearchItem.setRelevance(rankF);

        detailedSearchItem.setQuery(morph);
        detailedSearchItem.setFrequency(frequencyInt);
        return detailedSearchItem;
    }

    @SneakyThrows
    public Statement connectSql() {
        String userName = datasourceSQL.getDataSource().getUsername();
        String pass = datasourceSQL.getDataSource().getPassword();
        String url = datasourceSQL.getDataSource().getUrl();

        Connection connection = DriverManager.getConnection(url, userName, pass);
        return connection.createStatement();
    }


    public Set<DetailedSearchItem> sumRank(Set<DetailedSearchItem> list, Set<DetailedSearchItem> data) {
        for (DetailedSearchItem str : list) {

            for (DetailedSearchItem str2 : list) {
                if (!(str.getQuery().equals(str2.getQuery())) &&
                        str.getTitle().equals(str2.getTitle()) &&
                        str.getFrequency() <= str2.getFrequency()) {
                    str.setRelevance(str2.getRelevance() + str.getRelevance());
                    if (data.size() >= 30) break;
                    data.add(str);
                }
            }
        }
        return data;
    }
}

