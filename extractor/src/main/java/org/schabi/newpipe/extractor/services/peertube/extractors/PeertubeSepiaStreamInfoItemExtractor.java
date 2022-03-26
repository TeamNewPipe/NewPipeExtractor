package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonObject;

/**
 * A StreamInfoItem collected from SepiaSearch
 */
public class PeertubeSepiaStreamInfoItemExtractor extends PeertubeStreamInfoItemExtractor {

    public PeertubeSepiaStreamInfoItemExtractor(final JsonObject item, final String baseUrl) {
        super(item, baseUrl);
        final String embedUrl = super.item.getString("embedUrl");
        final String embedPath = super.item.getString("embedPath");
        final String itemBaseUrl = embedUrl.replace(embedPath, "");
        setBaseUrl(itemBaseUrl);

        // Usually, all videos, pictures and other content are hosted on the instance,
        // or can be accessed by the same URL path if the instance with baseUrl federates the one
        // where the video is actually uploaded. But it can't be accessed with Sepiasearch, so we
        // use the item's instance as base URL.
    }
}
