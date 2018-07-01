package org.schabi.newpipe.extractor.uih;

import java.io.Serializable;

public class UIHandler implements Serializable {
    protected final String originalUrl;
    protected final String url;
    protected final String id;

    public UIHandler(String originalUrl, String url, String id) {
        this.originalUrl = originalUrl;
        this.url = url;
        this.id = id;
    }

    public UIHandler(UIHandler handler) {
        this(handler.originalUrl, handler.url, handler.id);
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return id;
    }
}
