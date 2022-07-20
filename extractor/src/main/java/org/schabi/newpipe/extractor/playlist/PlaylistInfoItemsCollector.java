package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class PlaylistInfoItemsCollector
        extends InfoItemsCollector<PlaylistInfoItem, PlaylistInfoItemExtractor> {

    public PlaylistInfoItemsCollector(final int serviceId) {
        super(serviceId);
    }

    @Override
    public PlaylistInfoItem extract(final PlaylistInfoItemExtractor extractor)
            throws ParsingException {
        final PlaylistInfoItem resultItem = new PlaylistInfoItem(
                getServiceId(), extractor.getUrl(), extractor.getName());

        try {
            resultItem.setUploaderName(extractor.getUploaderName());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploaderUrl(extractor.getUploaderUrl());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploaderVerified(extractor.isUploaderVerified());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setThumbnails(extractor.getThumbnails());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setStreamCount(extractor.getStreamCount());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setDescription(extractor.getDescription());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setPlaylistType(extractor.getPlaylistType());
        } catch (final Exception e) {
            addError(e);
        }
        return resultItem;
    }
}
