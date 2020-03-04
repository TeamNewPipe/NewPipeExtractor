package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.MixedInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

public class SoundcloudChannelTracksExtractor extends ChannelTabExtractor {
    private MixedInfoItemsCollector streamInfoItemsCollector = null;
    private String nextPageUrl = null;

    public SoundcloudChannelTracksExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {}

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "Tracks";
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws ExtractionException {
        if (streamInfoItemsCollector == null) {
            computeNextPageAndGetStreams();
        }
        return new InfoItemsPage<>(streamInfoItemsCollector, getNextPageUrl());
    }

    @Override
    public String getNextPageUrl() throws ExtractionException {
        if (nextPageUrl == null) {
            computeNextPageAndGetStreams();
        }
        return nextPageUrl;
    }

    private void computeNextPageAndGetStreams() throws ExtractionException {
        try {
            streamInfoItemsCollector = new MixedInfoItemsCollector(getServiceId());

            String apiUrl = "https://api-v2.soundcloud.com/users/" + getId() + "/tracks"
                    + "?client_id=" + SoundcloudParsingHelper.clientId()
                    + "&limit=20"
                    + "&linked_partitioning=1";

            nextPageUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, streamInfoItemsCollector, apiUrl);
        } catch (Exception e) {
            throw new ExtractionException("Could not get next page", e);
        }
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        MixedInfoItemsCollector collector = new MixedInfoItemsCollector(getServiceId());
        String nextPageUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, collector, pageUrl);

        return new InfoItemsPage<>(collector, nextPageUrl);
    }
}
