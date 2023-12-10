package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;

import java.util.List;

/**
 * Like in {@link BandcampStreamLinkHandlerFactory}, tracks have no meaningful IDs except for
 * their URLs
 */
public class BandcampCommentsLinkHandlerFactory extends ListLinkHandlerFactory {

    @Override
    public String getId(final String url) throws ParsingException {
        return url;
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        if (BandcampExtractorHelper.isRadioUrl(url)) {
            return true;
        }

        // Don't accept URLs that don't point to a track
        if (!url.toLowerCase().matches("https?://.+\\..+/(track|album)/.+")) {
            return false;
        }

        // Test whether domain is supported
        return BandcampExtractorHelper.isSupportedDomain(url);
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilter,
                         final String sortFilter) throws ParsingException {
        return id;
    }
}
