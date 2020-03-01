package org.schabi.newpipe.extractor.services.peertube.extractors;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

public class PeertubePlaylistExtractor extends PlaylistExtractor {
    public PeertubePlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return null;
    }

    @Override
    public List<Image> getBanners() throws ParsingException {
        return null;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return null;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return null;
    }

    @Override
    public List<Image> getUploaderAvatars() throws ParsingException {
        return null;
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return 0;
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {

    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return null;
    }
}
