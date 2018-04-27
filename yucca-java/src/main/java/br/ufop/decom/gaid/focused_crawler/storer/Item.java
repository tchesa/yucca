package br.ufop.decom.gaid.focused_crawler.storer;

public class Item {

    private String title;
    private String url;
    private String html;
    //Alteracao
    private Double similarity;

    public Item(String title, String url, String html) {
        this.title = title;
        this.url = url;
        this.html = html;
        this.similarity = 0.0;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getHtml() {
        return html;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

    @Override
    public String toString() {
        return "title=" + title + "\nurl=" + url +"\nsimilarity="+ similarity + "\nhtml=" + html;
    }

}
