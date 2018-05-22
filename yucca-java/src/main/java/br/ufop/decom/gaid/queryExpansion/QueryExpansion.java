package br.ufop.decom.gaid.queryExpansion;

import edu.uci.ics.crawler4j.url.WebURL;

public abstract class QueryExpansion {

    public String[] getTerms(WebURL[] urls) {
        String[] _urls = new String[urls.length];
        for (int i = 0; i < urls.length; i++) {
            _urls[i] = urls[i].getURL();
        }
        return getTerms(_urls);
    }

    public abstract String[] getTerms(String[] urls);
}
