package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.CHANNELS;
import static org.schabi.newpipe.extractor.services.youtube.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_SONGS;
import static org.schabi.newpipe.extractor.services.youtube.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.PLAYLISTS;
import static org.schabi.newpipe.extractor.services.youtube.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.VIDEOS;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;

public class YoutubeSearchQHTest {

    @Test
    public void testRegularValues() throws Exception {
        assertEquals("https://www.youtube.com/results?search_query=asdf", YouTube.getSearchQHFactory().fromQuery("asdf").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=hans", YouTube.getSearchQHFactory().fromQuery("hans").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=Poifj%26jaijf", YouTube.getSearchQHFactory().fromQuery("Poifj&jaijf").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=G%C3%BCl%C3%BCm", YouTube.getSearchQHFactory().fromQuery("Gülüm").getUrl());
        assertEquals("https://www.youtube.com/results?search_query=%3Fj%24%29H%C2%A7B", YouTube.getSearchQHFactory().fromQuery("?j$)H§B").getUrl());

        assertEquals("https://music.youtube.com/search?q=asdf", YouTube.getSearchQHFactory().fromQuery("asdf", asList(new String[]{MUSIC_SONGS}), EMPTY_STRING).getUrl());
        assertEquals("https://music.youtube.com/search?q=hans", YouTube.getSearchQHFactory().fromQuery("hans", asList(new String[]{MUSIC_SONGS}), EMPTY_STRING).getUrl());
        assertEquals("https://music.youtube.com/search?q=Poifj%26jaijf", YouTube.getSearchQHFactory().fromQuery("Poifj&jaijf", asList(new String[]{MUSIC_SONGS}), EMPTY_STRING).getUrl());
        assertEquals("https://music.youtube.com/search?q=G%C3%BCl%C3%BCm", YouTube.getSearchQHFactory().fromQuery("Gülüm", asList(new String[]{MUSIC_SONGS}), EMPTY_STRING).getUrl());
        assertEquals("https://music.youtube.com/search?q=%3Fj%24%29H%C2%A7B", YouTube.getSearchQHFactory().fromQuery("?j$)H§B", asList(new String[]{MUSIC_SONGS}), EMPTY_STRING).getUrl());
    }

    @Test
    public void testGetContentFilter() throws Exception {
        assertEquals(VIDEOS, YouTube.getSearchQHFactory()
                .fromQuery(EMPTY_STRING, asList(new String[]{VIDEOS}), EMPTY_STRING).getContentFilters().get(0));
        assertEquals(CHANNELS, YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{CHANNELS}), EMPTY_STRING).getContentFilters().get(0));

        assertEquals(MUSIC_SONGS, YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{MUSIC_SONGS}), EMPTY_STRING).getContentFilters().get(0));
    }

    @Test
    public void testWithContentfilter() throws Exception {
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAQ%253D%253D", YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{VIDEOS}), EMPTY_STRING).getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAg%253D%253D", YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{CHANNELS}), EMPTY_STRING).getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAw%253D%253D", YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{PLAYLISTS}), EMPTY_STRING).getUrl());
        assertEquals("https://www.youtube.com/results?search_query=asdf", YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{"fjiijie"}), EMPTY_STRING).getUrl());

        assertEquals("https://music.youtube.com/search?q=asdf", YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{MUSIC_SONGS}), EMPTY_STRING).getUrl());
    }

    @Test
    public void testGetAvailableContentFilter() {
        final String[] contentFilter = YouTube.getSearchQHFactory().getAvailableContentFilter();
        assertEquals(8, contentFilter.length);
        assertEquals("all", contentFilter[0]);
        assertEquals("videos", contentFilter[1]);
        assertEquals("channels", contentFilter[2]);
        assertEquals("playlists", contentFilter[3]);
        assertEquals("music_songs", contentFilter[4]);
        assertEquals("music_videos", contentFilter[5]);
        assertEquals("music_albums", contentFilter[6]);
        assertEquals("music_playlists", contentFilter[7]);
    }

    @Test
    public void testGetAvailableSortFilter() {
        final String[] contentFilter = YouTube.getSearchQHFactory().getAvailableSortFilter();
        assertEquals(0, contentFilter.length);
    }
}
