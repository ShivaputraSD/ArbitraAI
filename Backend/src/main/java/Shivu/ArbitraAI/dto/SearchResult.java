package Shivu.ArbitraAI.dto;

public class SearchResult {
    private String title;
    private String url;
    private String content;

    public SearchResult() {}

    public SearchResult(String title, String url, String content) {
        this.title = title;
        this.url = url;
        this.content = content;
    }

    public String title() {
        return title;
    }

    public String url() {
        return url;
    }

    public String content() {
        return content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }
}