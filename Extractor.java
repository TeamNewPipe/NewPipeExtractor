package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import java.io.Serializable;

public abstract class Extractor implements Serializable {
    private final int serviceId;
    private final String url;
    private final UrlIdHandler urlIdHandler;
    private final StreamInfoItemCollector previewInfoCollector;

    public Extractor(UrlIdHandler urlIdHandler, int serviceId, String url) {
        this.urlIdHandler = urlIdHandler;
        this.serviceId = serviceId;
        this.url = url;
        this.previewInfoCollector = new StreamInfoItemCollector(serviceId);
    }

    public String getUrl() {
        return url;
    }

    public UrlIdHandler getUrlIdHandler() {
        return urlIdHandler;
    }

    public int getServiceId() {
        return serviceId;
    }

    protected StreamInfoItemCollector getStreamPreviewInfoCollector() {
        return previewInfoCollector;
    }
}
