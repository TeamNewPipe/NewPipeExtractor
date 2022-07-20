package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.List;

public abstract class PlaylistExtractor extends ListExtractor<StreamInfoItem> {

    public PlaylistExtractor(final StreamingService service, final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    public abstract String getUploaderUrl() throws ParsingException;
    public abstract String getUploaderName() throws ParsingException;
    @Nonnull
    public abstract List<Image> getUploaderAvatars() throws ParsingException;
    public abstract boolean isUploaderVerified() throws ParsingException;

    public abstract long getStreamCount() throws ParsingException;

    @Nonnull
    public abstract Description getDescription() throws ParsingException;

    @Nonnull
    public List<Image> getThumbnails() throws ParsingException {
        return Collections.emptyList();
    }

    @Nonnull
    public List<Image> getBanners() throws ParsingException {
        return List.of();
    }

    @Nonnull
    public String getSubChannelName() throws ParsingException {
        return "";
    }

    @Nonnull
    public String getSubChannelUrl() throws ParsingException {
        return "";
    }

    @Nonnull
    public List<Image> getSubChannelAvatars() throws ParsingException {
        return List.of();
    }

    public PlaylistInfo.PlaylistType getPlaylistType() throws ParsingException {
        return PlaylistInfo.PlaylistType.NORMAL;
    }
}
