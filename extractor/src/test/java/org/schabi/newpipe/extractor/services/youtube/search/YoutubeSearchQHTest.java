package org.schabi.newpipe.extractor.services.youtube.search;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.CHANNELS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_SONGS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.PLAYLISTS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.VIDEOS;

import org.junit.jupiter.api.Test;

import java.util.List;

public class YoutubeSearchQHTest {

    private static final List<String> L_MUSIC_SONGS = List.of(MUSIC_SONGS);

    @Test
    public void testRegularValues() throws Exception {
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=8AEB", YouTube.getSearchQHFactory().fromQuery("asdf").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=hans&sp=8AEB", YouTube.getSearchQHFactory().fromQuery("hans").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=Poifj%26jaijf&sp=8AEB", YouTube.getSearchQHFactory().fromQuery("Poifj&jaijf").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=G%C3%BCl%C3%BCm&sp=8AEB", YouTube.getSearchQHFactory().fromQuery("Gülüm").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=%3Fj%24%29H%C2%A7B&sp=8AEB", YouTube.getSearchQHFactory().fromQuery("?j$)H§B").getUrl());

        assertEquals("https://music.youtube.com/search?q=asdf", YouTube.getSearchQHFactory().fromQuery("asdf", L_MUSIC_SONGS, "").getUrl());
        assertEquals("https://music.youtube.com/search?q=hans", YouTube.getSearchQHFactory().fromQuery("hans", L_MUSIC_SONGS, "").getUrl());
        assertEquals("https://music.youtube.com/search?q=Poifj%26jaijf", YouTube.getSearchQHFactory().fromQuery("Poifj&jaijf", L_MUSIC_SONGS, "").getUrl());
        assertEquals("https://music.youtube.com/search?q=G%C3%BCl%C3%BCm", YouTube.getSearchQHFactory().fromQuery("Gülüm", L_MUSIC_SONGS, "").getUrl());
        assertEquals("https://music.youtube.com/search?q=%3Fj%24%29H%C2%A7B", YouTube.getSearchQHFactory().fromQuery("?j$)H§B", L_MUSIC_SONGS, "").getUrl());
    }

    @Test
    public void testGetContentFilter() throws Exception {
        assertEquals(VIDEOS, YouTube.getSearchQHFactory()
            .fromQuery("", List.of(VIDEOS), "").getContentFilters().get(0));
        assertEquals(CHANNELS, YouTube.getSearchQHFactory()
            .fromQuery("asdf", List.of(CHANNELS), "").getContentFilters().get(0));

        assertEquals(MUSIC_SONGS, YouTube.getSearchQHFactory()
            .fromQuery("asdf", L_MUSIC_SONGS, "").getContentFilters().get(0));
    }

    @Test
    public void testWithContentFilter() throws Exception {
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAfABAQ%253D%253D", YouTube.getSearchQHFactory()
            .fromQuery("asdf", List.of(VIDEOS), "").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAvABAQ%253D%253D", YouTube.getSearchQHFactory()
            .fromQuery("asdf", List.of(CHANNELS), "").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQA_ABAQ%253D%253D", YouTube.getSearchQHFactory()
            .fromQuery("asdf", List.of(PLAYLISTS), "").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=8AEB", YouTube.getSearchQHFactory()
            .fromQuery("asdf", List.of("fjiijie"), "").getUrl());

        assertEquals("https://music.youtube.com/search?q=asdf", YouTube.getSearchQHFactory()
            .fromQuery("asdf", L_MUSIC_SONGS, "").getUrl());
    }

    @Test
    public void testGetAvailableContentFilter() {
        assertArrayEquals(
            new String[]{
                "all",
                "videos",
                "channels",
                "playlists",
                "music_songs",
                "music_videos",
                "music_albums",
                "music_playlists"
            },
            YouTube.getSearchQHFactory().getAvailableContentFilter());
    }

    @Test
    public void testGetAvailableSortFilter() {
        final String[] contentFilter = YouTube.getSearchQHFactory().getAvailableSortFilter();
        assertEquals(0, contentFilter.length);
    }
}
