package br.ufop.decom.gaid.queryExpansion;

import edu.uci.ics.crawler4j.url.WebURL;
import java.util.List;

public abstract class QueryExpansion {

    public String[] getTerms(List<WebURL> seeds, int maxTerms) {
        String[] seedUrls = new String[seeds.size()];
        for (int i = 0; i < seeds.size(); i++) seedUrls[i] = seeds.get(i).getURL();
        return getTerms(seedUrls, maxTerms);
    }

    public abstract String[] getTerms(String[] urls, int maxTerms);
}
