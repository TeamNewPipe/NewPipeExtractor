package org.schabi.newpipe.extractor.channel.tabs;

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
    
    public static final FilterItem VIDEOS = new FilterItem(ID_VIDEOS, "Videos");
    public static final FilterItem TRACKS = new FilterItem(ID_TRACKS, "Tracks");
    public static final FilterItem SHORTS = new FilterItem(ID_SHORTS, "Shorts");
    public static final FilterItem LIVESTREAMS = new FilterItem(ID_LIVESTREAMS, "Livestreams");
    public static final FilterItem CHANNELS = new FilterItem(ID_CHANNELS, "Channels");
    public static final FilterItem PLAYLISTS = new FilterItem(ID_PLAYLISTS, "Playlists");
    public static final FilterItem ALBUMS = new FilterItem(ID_ALBUMS, "Albums");

    private ChannelTabs() {
    }
}
