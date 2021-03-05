// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.util.List;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor.FEATURED_API_URL;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor.KIOSK_FEATURED;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioExtractor.KIOSK_RADIO;
import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioExtractor.RADIO_API_URL;

public class BandcampFeaturedLinkHandlerFactory extends ListLinkHandlerFactory {

    @Override
    public String getUrl(final String id, final List<String> contentFilter, final String sortFilter) {
        if (id.equals(KIOSK_FEATURED)) {
            return FEATURED_API_URL; // doesn't have a website
        } else if (id.equals(KIOSK_RADIO)) {
            return RADIO_API_URL; // doesn't have its own website
        } else {
            return null;
        }
    }

    @Override
    public String getId(String url) {
        url = Utils.replaceHttpWithHttps(url);
        if (BandcampExtractorHelper.isRadioUrl(url) || url.equals(RADIO_API_URL)) {
            return KIOSK_RADIO;
        } else if (url.equals(FEATURED_API_URL)) {
            return KIOSK_FEATURED;
        } else {
            return null;
        }
    }

    @Override
    public boolean onAcceptUrl(String url) {
        url = Utils.replaceHttpWithHttps(url);
        return url.equals(FEATURED_API_URL) || (url.equals(RADIO_API_URL) || BandcampExtractorHelper.isRadioUrl(url));
    }
}
