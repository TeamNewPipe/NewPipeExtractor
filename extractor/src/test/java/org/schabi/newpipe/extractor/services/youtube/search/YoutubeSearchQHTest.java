package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.Test;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.CHANNELS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.PLAYLISTS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.VIDEOS;

public class YoutubeSearchQHTest {

    @Test
    public void testRegularValues() throws Exception {
        assertEquals("https://www.youtube.com/results?q=asdf", YouTube.getSearchQHFactory().fromQuery("asdf").getUrl());
        assertEquals("https://www.youtube.com/results?q=hans",YouTube.getSearchQHFactory().fromQuery("hans").getUrl());
        assertEquals("https://www.youtube.com/results?q=Poifj%26jaijf", YouTube.getSearchQHFactory().fromQuery("Poifj&jaijf").getUrl());
        assertEquals("https://www.youtube.com/results?q=G%C3%BCl%C3%BCm", YouTube.getSearchQHFactory().fromQuery("Gülüm").getUrl());
        assertEquals("https://www.youtube.com/results?q=%3Fj%24%29H%C2%A7B", YouTube.getSearchQHFactory().fromQuery("?j$)H§B").getUrl());
    }

    @Test
    public void testGetContentFilter() throws Exception {
        assertEquals(VIDEOS, YouTube.getSearchQHFactory()
                .fromQuery("", asList(new String[]{VIDEOS}), "").getContentFilters().get(0));
        assertEquals(CHANNELS, YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{CHANNELS}), "").getContentFilters().get(0));
    }

    @Test
    public void testWithContentfilter() throws Exception {
        assertEquals("https://www.youtube.com/results?q=asdf&sp=EgIQAVAU", YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{VIDEOS}), "").getUrl());
        assertEquals("https://www.youtube.com/results?q=asdf&sp=EgIQAlAU", YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{CHANNELS}), "").getUrl());
        assertEquals("https://www.youtube.com/results?q=asdf&sp=EgIQA1AU", YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{PLAYLISTS}), "").getUrl());
        assertEquals("https://www.youtube.com/results?q=asdf", YouTube.getSearchQHFactory()
                .fromQuery("asdf", asList(new String[]{"fjiijie"}), "").getUrl());
    }

    @Test
    public void testGetAvailableContentFilter() {
        final String[] contentFilter = YouTube.getSearchQHFactory().getAvailableContentFilter();
        assertEquals(4, contentFilter.length);
        assertEquals("all", contentFilter[0]);
        assertEquals("videos", contentFilter[1]);
        assertEquals("channels", contentFilter[2]);
        assertEquals("playlists", contentFilter[3]);
    }

    @Test
    public void testGetAvailableSortFilter() {
        final String[] contentFilter = YouTube.getSearchQHFactory().getAvailableSortFilter();
        assertEquals(0, contentFilter.length);
    }
}
