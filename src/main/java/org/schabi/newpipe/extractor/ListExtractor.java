package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import java.io.IOException;

/**
 * Base class to extractors that have a list (e.g. playlists, channels).
 */
public abstract class ListExtractor extends Extractor {
    protected String nextStreamsUrl;

    public ListExtractor(UrlIdHandler urlIdHandler, int serviceId, String url) {
        super(urlIdHandler, serviceId, url);
    }

    public boolean hasMoreStreams(){
        return nextStreamsUrl != null && !nextStreamsUrl.isEmpty();
    }

    public abstract StreamInfoItemCollector getNextStreams() throws ExtractionException, IOException;

    public String getNextStreamsUrl() {
        return nextStreamsUrl;
    }

    public void setNextStreamsUrl(String nextStreamsUrl) {
        this.nextStreamsUrl = nextStreamsUrl;
    }

}
