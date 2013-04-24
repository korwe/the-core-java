package com.korwe.thecore.dto.syndication;

import java.util.List;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public class SyndicationFeed {
    private String title;
    private String description;
    private String url;

    private List<SyndicationEntry> entries;

    public List<SyndicationEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<SyndicationEntry> entries) {
        this.entries = entries;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
