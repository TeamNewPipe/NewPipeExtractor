package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabHandler;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class SoundcloudChannelTabExtractor extends ChannelTabExtractor {
    private final String userId;
    private static final String USERS_ENDPOINT = SOUNDCLOUD_API_V2_URL + "users/";

    public SoundcloudChannelTabExtractor(final StreamingService service,
                                         final ChannelTabHandler linkHandler) {
        super(service, linkHandler);
        userId = getLinkHandler().getId();
    }

    private String getEndpoint() {
        switch (getTab()) {
            case Playlists:
                return "/playlists_without_albums";
            case Albums:
                return "/albums";
        }
        throw new IllegalArgumentException("unsupported tab: " + getTab().name());
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
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
        final String nextPageUrl = SoundcloudParsingHelper.getInfoItemsFromApi(collector,
                page.getUrl());

        return new InfoItemsPage<>(collector, new Page(nextPageUrl));
    }
}
