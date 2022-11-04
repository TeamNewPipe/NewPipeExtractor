package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import com.grack.nanojson.JsonArray;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.util.Collections;

public class BandcampChannelTabHandler extends ListLinkHandler {
    private final JsonArray discographs;

    public BandcampChannelTabHandler(final String url, final String id, final String tab,
                                     final JsonArray discographs) {
        super(url, url, id, Collections.singletonList(tab), "");
        this.discographs = discographs;
    }

    public JsonArray getDiscographs() {
        return discographs;
    }
}
