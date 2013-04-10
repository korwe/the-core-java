package com.korwe.thecore.dto.syndication;

import java.util.List;

/**
 * @author <a href="mailto:tjad.clark@korwe.com">Tjad Clark</a>
 */
public class SyndicationFeed {
    private List<SyndicationEntry> entries;

    public List<SyndicationEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<SyndicationEntry> entries) {
        this.entries = entries;
    }

}
