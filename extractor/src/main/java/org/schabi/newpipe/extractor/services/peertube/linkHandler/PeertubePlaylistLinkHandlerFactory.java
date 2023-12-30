package org.schabi.newpipe.extractor.services.peertube.linkHandler;


import org.schabi.newpipe.extractor.search.filter.FilterItem;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PeertubePlaylistLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final PeertubePlaylistLinkHandlerFactory INSTANCE
            = new PeertubePlaylistLinkHandlerFactory();
    private static final String ID_PATTERN = "(/videos/watch/playlist/|/w/p/)([^/?&#]*)";
    private static final String API_ID_PATTERN = "/video-playlists/([^/?&#]*)";

    private PeertubePlaylistLinkHandlerFactory() {
    }

    public static PeertubePlaylistLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String id,
                         @Nonnull final List<FilterItem> contentFilters,
                         @Nullable final List<FilterItem> sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return getUrl(id, contentFilters, sortFilter, ServiceList.PeerTube.getBaseUrl());
    }

    @Override
    public String getUrl(final String id,
                         final List<FilterItem> contentFilters,
                         final List<FilterItem> sortFilter,
                         final String baseUrl)
            throws ParsingException, UnsupportedOperationException {
        return baseUrl + "/api/v1/video-playlists/" + id;
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        try {
            return Parser.matchGroup(ID_PATTERN, url, 2);
        } catch (final ParsingException ignored) {
            // might also be an API url, no reason to throw an exception here
        }
        return Parser.matchGroup1(API_ID_PATTERN, url);
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        try {
            new URL(url);
            getId(url);
            return true;
        } catch (final ParsingException | MalformedURLException e) {
            return false;
        }
    }
}
