package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.search.SearchEngine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link SearchEngine}
 */
public class SoundcloudSearchEngineStreamTest extends BaseSoundcloudSearchTest {

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        SearchEngine engine = SoundCloud.getSearchEngine();

        // SoundCloud will suggest "lil uzi vert" instead of "lill uzi vert",
        // keep in mind that the suggestions can NOT change by country (the parameter "de")
        result = engine.search("lill uzi vert", 0, "de", SearchEngine.Filter.STREAM)
                .getSearchResult();
    }

    @Test
    public void testResultsItemType() {
        for (InfoItem infoItem : result.resultList) {
            assertEquals(InfoItem.InfoType.STREAM, infoItem.getInfoType());
        }
    }

    @Ignore
    @Test
    public void testSuggestion() {
        //todo write a real test
        assertTrue(result.suggestion != null);
    }
}
