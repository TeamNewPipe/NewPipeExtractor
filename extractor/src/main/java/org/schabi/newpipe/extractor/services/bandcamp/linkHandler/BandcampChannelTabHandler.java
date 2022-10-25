package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import com.grack.nanojson.JsonArray;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

public class BandcampChannelTabHandler extends ChannelTabHandler {
    private final JsonArray discographs;

    public BandcampChannelTabHandler(final ListLinkHandler linkHandler, final Tab tab,
                                     final JsonArray discographs) {
        super(linkHandler, tab);
        this.discographs = discographs;
    }

    public JsonArray getDiscographs() {
        return discographs;
    }
}
