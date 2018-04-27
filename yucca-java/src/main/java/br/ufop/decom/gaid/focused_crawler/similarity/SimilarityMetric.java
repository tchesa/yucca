package br.ufop.decom.gaid.focused_crawler.similarity;

public interface SimilarityMetric {

    public double similarity(String text) throws Exception;

    public double similarity(String pageText, String urlText) throws Exception;

    /*Teste*/
    public double similarity(String pageText, String urlText, int docid) throws Exception;

    public double similarity2(String pageText, String urlText, int docid) throws Exception;

    public boolean isSimilar(double similarityValue);


}
