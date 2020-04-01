package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class YoutubeSearchQueryHandlerFactory extends SearchQueryHandlerFactory {
    public static final String CHARSET_UTF_8 = "UTF-8";

    public static final String ALL = "all";
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

    public static YoutubeSearchQueryHandlerFactory getInstance() {
        return new YoutubeSearchQueryHandlerFactory();
    }

    @Override
    public String getUrl(String searchString, List<String> contentFilters, String sortFilter) throws ParsingException {
        try {
            if (contentFilters.size() > 0) {
                switch (contentFilters.get(0)) {
                    case ALL:
                    default:
                        break;
                    case VIDEOS:
                        return SEARCH_URL + URLEncoder.encode(searchString, CHARSET_UTF_8) + "&sp=EgIQAQ%253D%253D";
                    case CHANNELS:
                        return SEARCH_URL + URLEncoder.encode(searchString, CHARSET_UTF_8) + "&sp=EgIQAg%253D%253D";
                    case PLAYLISTS:
                        return SEARCH_URL + URLEncoder.encode(searchString, CHARSET_UTF_8) + "&sp=EgIQAw%253D%253D";
                    case MUSIC_SONGS:
                    case MUSIC_VIDEOS:
                    case MUSIC_ALBUMS:
                    case MUSIC_PLAYLISTS:
                    case MUSIC_ARTISTS:
                        return MUSIC_SEARCH_URL + URLEncoder.encode(searchString, CHARSET_UTF_8);
                }
            }

            return SEARCH_URL + URLEncoder.encode(searchString, CHARSET_UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{
                ALL,
                VIDEOS,
                CHANNELS,
                PLAYLISTS,
                MUSIC_SONGS,
                MUSIC_VIDEOS,
                MUSIC_ALBUMS,
                MUSIC_PLAYLISTS
//                MUSIC_ARTISTS
        };
    }
}
