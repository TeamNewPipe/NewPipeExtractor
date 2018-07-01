package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeSearchQUHTest {

    @Test
    public void testRegularValues() throws Exception {
        assertEquals("https://www.youtube.com/results?q=asdf", YouTube.getSearchQIHFactory().fromQuery("asdf").getUrl());
        assertEquals("https://www.youtube.com/results?q=hans",YouTube.getSearchQIHFactory().fromQuery("hans").getUrl());
        assertEquals("https://www.youtube.com/results?q=Poifj%26jaijf", YouTube.getSearchQIHFactory().fromQuery("Poifj&jaijf").getUrl());
        assertEquals("https://www.youtube.com/results?q=G%C3%BCl%C3%BCm", YouTube.getSearchQIHFactory().fromQuery("Gülüm").getUrl());
        assertEquals("https://www.youtube.com/results?q=%3Fj%24%29H%C2%A7B", YouTube.getSearchQIHFactory().fromQuery("?j$)H§B").getUrl());
    }

    @Test
    public void testGetContentFilter() throws Exception {
        assertEquals("stream", YouTube.getSearchQIHFactory()
                .fromQuery("", asList(new String[]{"stream"}), "").getContentFilters().get(0));
        assertEquals("channel", YouTube.getSearchQIHFactory()
                .fromQuery("asdf", asList(new String[]{"channel"}), "").getContentFilters().get(0));
    }

    @Test
    public void testWithContentfilter() throws Exception {
        assertEquals("https://www.youtube.com/results?q=asdf&sp=EgIQAVAU", YouTube.getSearchQIHFactory()
                .fromQuery("asdf", asList(new String[]{"stream"}), "").getUrl());
        assertEquals("https://www.youtube.com/results?q=asdf&sp=EgIQAlAU", YouTube.getSearchQIHFactory()
                .fromQuery("asdf", asList(new String[]{"channel"}), "").getUrl());
        assertEquals("https://www.youtube.com/results?q=asdf&sp=EgIQA1AU", YouTube.getSearchQIHFactory()
                .fromQuery("asdf", asList(new String[]{"playlist"}), "").getUrl());
        assertEquals("https://www.youtube.com/results?q=asdf", YouTube.getSearchQIHFactory()
                .fromQuery("asdf", asList(new String[]{"fjiijie"}), "").getUrl());
    }

    @Test
    public void testGetAvailableContentFilter() {
        final String[] contentFilter = YouTube.getSearchQIHFactory().getAvailableContentFilter();
        assertEquals(4, contentFilter.length);
        assertEquals("stream", contentFilter[0]);
        assertEquals("channel", contentFilter[1]);
        assertEquals("playlist", contentFilter[2]);
        assertEquals("any", contentFilter[3]);
    }

    @Test
    public void testGetAvailableSortFilter() {
        final String[] contentFilter = YouTube.getSearchQIHFactory().getAvailableSortFilter();
        assertEquals(0, contentFilter.length);
    }
}
