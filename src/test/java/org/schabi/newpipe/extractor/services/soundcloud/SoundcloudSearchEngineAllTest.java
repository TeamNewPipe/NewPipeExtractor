package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.search.SearchResult;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link SearchEngine}
 */
public class SoundcloudSearchEngineAllTest {
    private static SearchResult result;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        SearchEngine engine = SoundCloud.getService().getSearchEngine();

        // SoundCloud will suggest "lil uzi vert" instead of "lill uzi vert"
        // keep in mind that the suggestions can NOT change by country (the parameter "de")
        result = engine.search("lill uzi vert", 0, "de", SearchEngine.Filter.ANY)
                .getSearchResult();
    }

    @Test
    public void testResultList() {
        assertFalse(result.resultList.isEmpty());
    }

    @Test
    public void testResultErrors() {
        assertNotNull(result.errors);
        if (!result.errors.isEmpty()) for (Throwable error : result.errors) error.printStackTrace();
        assertTrue(result.errors.isEmpty());
    }

    @Ignore
    @Test
    public void testSuggestion() {
        //todo write a real test
        assertTrue(result.suggestion != null);
    }
}
