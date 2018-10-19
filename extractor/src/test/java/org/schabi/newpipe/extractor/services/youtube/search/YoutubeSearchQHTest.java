package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.Test;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

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
        assertEquals(YoutubeSearchQueryHandlerFactory.ContentFilter.videos.name(), YouTube.getSearchQHFactory()
                .fromQuery("", Collections.singletonList(YoutubeSearchQueryHandlerFactory.ContentFilter.videos.name()), "").getContentFilters().get(0));
        assertEquals(YoutubeSearchQueryHandlerFactory.ContentFilter.channels.name(), YouTube.getSearchQHFactory()
                .fromQuery("asdf", Collections.singletonList(YoutubeSearchQueryHandlerFactory.ContentFilter.channels.name()), "").getContentFilters().get(0));
    }

    @Test
    public void testWithGenuineContentfilter() throws Exception {
        // TODO: 19/10/18
    }

    @Test
    public void testWithGenuineSortfilter() throws Exception {
        // TODO: 19/10/18
    }

    @Test
    public void testWithGibbershContentFilter() throws Exception {
        assertEquals("https://www.youtube.com/results?q=asdf", YouTube.getSearchQHFactory()
                .fromQuery("asdf", Collections.singletonList("gibberish"), "").getUrl());
    }

    @Test
    public void testWithGibbershSortFilter() throws Exception {
        assertEquals("https://www.youtube.com/results?q=asdf", YouTube.getSearchQHFactory()
                .fromQuery("asdf", Collections.<String>emptyList(), "gibberish").getUrl());
    }

    @Test
    public void testGetAvailableContentFilter() {
        final String[] contentFilter = YouTube.getSearchQHFactory().getAvailableContentFilter();
        assertEquals(YoutubeSearchQueryHandlerFactory.ContentFilter.values().length, contentFilter.length);
    }

    @Test
    public void testGetAvailableSortFilter() {
        final String[] sortFilters = YouTube.getSearchQHFactory().getAvailableSortFilter();
        assertEquals(YoutubeSearchQueryHandlerFactory.SortFilter.values().length, sortFilters.length);
    }
}
