package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class SoundcloudChannelTabExtractor extends ChannelTabExtractor {

    private static final String USERS_ENDPOINT = SOUNDCLOUD_API_V2_URL + "users/";

    private final String userId;

    public SoundcloudChannelTabExtractor(final StreamingService service,
                                         final ListLinkHandler linkHandler) {
        super(service, linkHandler);
        userId = getLinkHandler().getId();
    }

    @Nonnull
    private String getEndpoint() throws ParsingException {
        switch (getName()) {
            case ChannelTabs.TRACKS:
                return "/tracks";
            case ChannelTabs.PLAYLISTS:
                return "/playlists_without_albums";
            case ChannelTabs.ALBUMS:
                return "/albums";
        }
        throw new ParsingException("Unsupported tab: " + getName());
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) {
    }

    @Nonnull
    @Override
    public String getId() {
        return userId;
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(new Page(USERS_ENDPOINT + userId + getEndpoint() + "?client_id="
                + SoundcloudParsingHelper.clientId() + "&limit=20" + "&linked_partitioning=1"));
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());
        final String nextPageUrl = SoundcloudParsingHelper.getInfoItemsFromApi(
                collector, page.getUrl());

        return new InfoItemsPage<>(collector, new Page(nextPageUrl));
    }
}
