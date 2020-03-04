package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelPlaylistsExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

public class SoundcloudChannelAlbumsExtractor extends ChannelPlaylistsExtractor {
    private PlaylistInfoItemsCollector playlistInfoItemsCollector = null;
    private String nextPageUrl = null;

    public SoundcloudChannelAlbumsExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {}

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "Albums";
    }

    @Nonnull
    @Override
    public InfoItemsPage<PlaylistInfoItem> getInitialPage() throws ExtractionException {
        if (playlistInfoItemsCollector == null) {
            computeNextPageAndGetPlaylists();
        }
        return new InfoItemsPage<>(playlistInfoItemsCollector, getNextPageUrl());
    }

    @Override
    public String getNextPageUrl() throws ExtractionException {
        if (nextPageUrl == null) {
            computeNextPageAndGetPlaylists();
        }
        return nextPageUrl;
    }

    private void computeNextPageAndGetPlaylists() throws ExtractionException {
        try {
            playlistInfoItemsCollector = new PlaylistInfoItemsCollector(getServiceId());

            String apiUrl = "https://api-v2.soundcloud.com/users/" + getId() + "/albums"
                    + "?client_id=" + SoundcloudParsingHelper.clientId()
                    + "&limit=20"
                    + "&linked_partitioning=1";

            nextPageUrl = SoundcloudParsingHelper.getPlaylistsFromApiMinItems(15, playlistInfoItemsCollector, apiUrl);
        } catch (Exception e) {
            throw new ExtractionException("Could not get next page", e);
        }
    }

    @Override
    public InfoItemsPage<PlaylistInfoItem> getPage(final String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        PlaylistInfoItemsCollector collector = new PlaylistInfoItemsCollector(getServiceId());
        String nextPageUrl = SoundcloudParsingHelper.getPlaylistsFromApiMinItems(15, collector, pageUrl);

        return new InfoItemsPage<>(collector, nextPageUrl);
    }
}
