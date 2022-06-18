package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonObject;

final class MediaCCCLiveStreamMapperDTO {
    private final JsonObject streamJsonObj;
    private final String urlKey;
    private final JsonObject urlValue;

    MediaCCCLiveStreamMapperDTO(final JsonObject streamJsonObj,
                                final String urlKey,
                                final JsonObject urlValue) {
        this.streamJsonObj = streamJsonObj;
        this.urlKey = urlKey;
        this.urlValue = urlValue;
    }

    JsonObject getStreamJsonObj() {
        return streamJsonObj;
    }

    String getUrlKey() {
        return urlKey;
    }

    JsonObject getUrlValue() {
        return urlValue;
    }
}
