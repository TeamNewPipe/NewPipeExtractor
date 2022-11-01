package org.schabi.newpipe.extractor.channel.tabs;

import static org.schabi.newpipe.extractor.search.filter.LibraryStringIds.CHANNEL_TAB_ALBUMS;
import static org.schabi.newpipe.extractor.search.filter.LibraryStringIds.CHANNEL_TAB_CHANNELS;
import static org.schabi.newpipe.extractor.search.filter.LibraryStringIds.CHANNEL_TAB_LIVESTREAMS;
import static org.schabi.newpipe.extractor.search.filter.LibraryStringIds.CHANNEL_TAB_PLAYLISTS;
import static org.schabi.newpipe.extractor.search.filter.LibraryStringIds.CHANNEL_TAB_SHORTS;
import static org.schabi.newpipe.extractor.search.filter.LibraryStringIds.CHANNEL_TAB_TRACKS;
import static org.schabi.newpipe.extractor.search.filter.LibraryStringIds.CHANNEL_TAB_VIDEOS;

import org.schabi.newpipe.extractor.search.filter.FilterItem;

/**
 * Constants of channel tabs supported by the extractor.
 */
public final class ChannelTabs {
    public static final int ID_VIDEOS = 0;
    public static final int ID_TRACKS = 1;
    public static final int ID_SHORTS = 2;
    public static final int ID_LIVESTREAMS = 3;
    public static final int ID_CHANNELS = 4;
    public static final int ID_PLAYLISTS = 5;
    public static final int ID_ALBUMS = 6;

    public static final FilterItem VIDEOS = new FilterItem(ID_VIDEOS, CHANNEL_TAB_VIDEOS);
    public static final FilterItem TRACKS = new FilterItem(ID_TRACKS, CHANNEL_TAB_TRACKS);
    public static final FilterItem SHORTS = new FilterItem(ID_SHORTS, CHANNEL_TAB_SHORTS);
    public static final FilterItem LIVESTREAMS =
            new FilterItem(ID_LIVESTREAMS, CHANNEL_TAB_LIVESTREAMS);
    public static final FilterItem CHANNELS = new FilterItem(ID_CHANNELS, CHANNEL_TAB_CHANNELS);
    public static final FilterItem PLAYLISTS = new FilterItem(ID_PLAYLISTS, CHANNEL_TAB_PLAYLISTS);
    public static final FilterItem ALBUMS = new FilterItem(ID_ALBUMS, CHANNEL_TAB_ALBUMS);

    private ChannelTabs() {
    }
}
