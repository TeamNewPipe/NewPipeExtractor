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
import java.util.HashSet;
import java.util.Set;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class SoundcloudChannelTabExtractor extends ChannelTabExtractor {

    private static final String USERS_ENDPOINT = SOUNDCLOUD_API_V2_URL + "users/";

    /** Empty page cap against infinite pagination loops. */
    private static final int MAX_EMPTY_PAGES = 3;

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
            case ChannelTabs.LIKES:
                return "/likes";
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

        final var collector = new MultiInfoItemsCollector(getServiceId());
        final Set<String> visitedPages = new HashSet<>();

        String currentPageUrl = page.getUrl();
        String nextPageUrl = "";
        int emptyPageCount = 0;

        while (!isNullOrEmpty(currentPageUrl)) {
            if (!visitedPages.add(currentPageUrl)) {
                // Prevent infinite loops when the API points back to an already visited page.
                nextPageUrl = "";
                break;
            }

            final int itemsBefore = collector.getItems().size();
            final String candidateNextPage = SoundcloudParsingHelper
                    .getInfoItemsFromApi(collector, currentPageUrl);
            final boolean hasNewItems = collector.getItems().size() > itemsBefore;

            if (hasNewItems) {
                nextPageUrl = candidateNextPage;
                break;
            }

            emptyPageCount++;
            if (emptyPageCount >= MAX_EMPTY_PAGES || isNullOrEmpty(candidateNextPage)) {
                // Give up after too many empty responses or when SoundCloud stops providing tokens.
                nextPageUrl = "";
                break;
            }

            currentPageUrl = candidateNextPage;
        }

        final Page nextPage = isNullOrEmpty(nextPageUrl) ? null : new Page(nextPageUrl);
        return new InfoItemsPage<>(collector, nextPage);
    }
}
