package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import static org.schabi.newpipe.extractor.utils.Utils.encodeUrlUtf8;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.util.List;

import javax.annotation.Nonnull;

public final class YoutubeSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    private static final YoutubeSearchQueryHandlerFactory INSTANCE =
            new YoutubeSearchQueryHandlerFactory();

    public static final String ALL = "all";
    public static final String EXACT = "exact";
    public static final String VIDEOS = "videos";
    public static final String CHANNELS = "channels";
    public static final String PLAYLISTS = "playlists";

    public static final String MUSIC_SONGS = "music_songs";
    public static final String MUSIC_VIDEOS = "music_videos";
    public static final String MUSIC_ALBUMS = "music_albums";
    public static final String MUSIC_PLAYLISTS = "music_playlists";
    public static final String MUSIC_ARTISTS = "music_artists";

    private static final String SEARCH_URL = "https://www.youtube.com/results?search_query=";
    private static final String MUSIC_SEARCH_URL = "https://music.youtube.com/search?q=";

    @Nonnull
    public static YoutubeSearchQueryHandlerFactory getInstance() {
        return INSTANCE;
    }
    @Override
    public String getUrl(final String searchString,
                         @Nonnull final List<String> contentFilters,
                         final String sortFilter)
            throws ParsingException, UnsupportedOperationException {
        final String contentFilter = !contentFilters.isEmpty() ? contentFilters.get(0) : "";
        final boolean isExactSearch = !contentFilters.isEmpty() && contentFilter.contains(EXACT);

        switch (contentFilter) {
            case EXACT:
                return SEARCH_URL + encodeUrlUtf8(searchString) + "&sp=QgIIAQ%253D%253D";
            case VIDEOS:
                return SEARCH_URL + encodeUrlUtf8(searchString) + (isExactSearch ? "&sp=EgIQAUICCAE%253D" : "&sp=EgIQAfABAQ%253D%253D");
            case CHANNELS:
                return SEARCH_URL + encodeUrlUtf8(searchString) + (isExactSearch ? "&sp=EgIQAkICCAE%253D" : "&sp=EgIQAvABAQ%253D%253D");
            case PLAYLISTS:
                return SEARCH_URL + encodeUrlUtf8(searchString) + (isExactSearch ? "&sp=EgIQA0ICCAE%253D" : "&sp=EgIQA_ABAQ%253D%253D");
            case MUSIC_SONGS:
            case MUSIC_VIDEOS:
            case MUSIC_ALBUMS:
            case MUSIC_PLAYLISTS:
            case MUSIC_ARTISTS:
                return MUSIC_SEARCH_URL + encodeUrlUtf8(searchString);
            default:
                return SEARCH_URL + encodeUrlUtf8(searchString) + "&sp=8AEB";
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{
                ALL,
                //EXACT, Not a separate content filter (yet)
                VIDEOS,
                CHANNELS,
                PLAYLISTS,
                MUSIC_SONGS,
                MUSIC_VIDEOS,
                MUSIC_ALBUMS,
                MUSIC_PLAYLISTS
                // MUSIC_ARTISTS
        };
    }

    @Nonnull
    public static String getSearchParameter(final String contentFilter, final boolean isExactSearch) {
        if (isNullOrEmpty(contentFilter)) {
            return "8AEB";
        }

        switch (contentFilter) {
            case EXACT:
                return "QgIIAQ%3D%3D";
            case VIDEOS:
                return isExactSearch ? "EgIQAUICCAE%3D" : "EgIQAfABAQ%3D%3D";
            case CHANNELS:
                return isExactSearch ? "EgIQAkICCAE%3D" : "EgIQAvABAQ%3D%3D";
            case PLAYLISTS:
                return isExactSearch ? "EgIQA0ICCAE%3D" : "EgIQA_ABAQ%3D%3D";
            case MUSIC_SONGS:
            case MUSIC_VIDEOS:
            case MUSIC_ALBUMS:
            case MUSIC_PLAYLISTS:
            case MUSIC_ARTISTS:
                return "";
            default:
                return "8AEB";
        }
    }
}
