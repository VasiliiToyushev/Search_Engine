package searchengine.dto.statistics;

import lombok.Data;
import searchengine.model.Page;

@Data
public class TransferTools {
    private String content;
    private int code;
    private String url;
    private String lemma;
    private int lemmaCountOnePage;
    private Page page;
    private String error;

}
