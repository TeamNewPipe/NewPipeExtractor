package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItemCollector;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class PlaylistInfoItemCollector extends InfoItemCollector {
    public PlaylistInfoItemCollector(int serviceId) {
        super(serviceId);
    }

    public PlaylistInfoItem extract(PlaylistInfoItemExtractor extractor) throws ParsingException {
        final PlaylistInfoItem resultItem = new PlaylistInfoItem();

        resultItem.name = extractor.getPlaylistName();
        resultItem.serviceId = getServiceId();
        resultItem.webPageUrl = extractor.getWebPageUrl();
        try {
            resultItem.thumbnailUrl = extractor.getThumbnailUrl();
        } catch (Exception e) {
            addError(e);
        }
        return resultItem;
    }

    public void commit(PlaylistInfoItemExtractor extractor) throws ParsingException {
        try {
            addItem(extract(extractor));
        } catch (Exception e) {
            addError(e);
        }
    }
}
