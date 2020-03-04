package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MixedInfoItemsCollector;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.io.IOException;

import javax.annotation.Nonnull;

public class SoundcloudChannelRepostsExtractor extends ChannelTabExtractor {
    private MixedInfoItemsCollector mixedInfoItemsCollector = null;
    private String nextPageUrl = null;

    public SoundcloudChannelRepostsExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {}

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "Reposts";
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws ExtractionException {
        if (mixedInfoItemsCollector == null) {
            computeNextPageAndGetStreams();
        }
        return new InfoItemsPage<>(mixedInfoItemsCollector, getNextPageUrl());
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
            mixedInfoItemsCollector = new MixedInfoItemsCollector(getServiceId());

            String apiUrl = "https://api-v2.soundcloud.com/stream/users/" + getId() + "/reposts"
                    + "?client_id=" + SoundcloudParsingHelper.clientId()
                    + "&limit=20"
                    + "&linked_partitioning=1";

            nextPageUrl = SoundcloudParsingHelper.getMixedFromApiMinItems(15, mixedInfoItemsCollector, apiUrl);
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
        String nextPageUrl = SoundcloudParsingHelper.getMixedFromApiMinItems(15, collector, pageUrl);

        return new InfoItemsPage<>(collector, nextPageUrl);
    }
}
