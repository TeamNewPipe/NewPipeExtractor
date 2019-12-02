package org.schabi.newpipe.extractor.services.peertube.extractors;

import java.util.List;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;

public class PeertubeSubscriptionExtractor extends SubscriptionExtractor {

    public PeertubeSubscriptionExtractor(StreamingService service, List<ContentSource> supportedSources) {
        super(service, supportedSources);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getRelatedUrl() {
        // TODO Auto-generated method stub
        return null;
    }

}
