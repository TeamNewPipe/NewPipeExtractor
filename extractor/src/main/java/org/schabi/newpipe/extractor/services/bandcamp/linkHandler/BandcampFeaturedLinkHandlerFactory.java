// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor.KIOSK_FEATURED;

public class BandcampFeaturedLinkHandlerFactory extends ListLinkHandlerFactory {

    /**
     * This kiosk doesn't have a corresponding website
     */
    public static final String FEATURED_API_URL = "https://bandcamp.com/api/mobile/24/bootstrap_data";

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) {
        if (id.equals(KIOSK_FEATURED)) return FEATURED_API_URL;
        else return null;
    }

    @Override
    public String getId(String url) {
        return KIOSK_FEATURED;
    }

    @Override
    public boolean onAcceptUrl(String url) {
        return url.equals(FEATURED_API_URL);
    }
}
