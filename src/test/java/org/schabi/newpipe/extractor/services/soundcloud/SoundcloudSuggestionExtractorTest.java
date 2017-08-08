package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.Before;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link SuggestionExtractor}
 */
public class SoundcloudSuggestionExtractorTest {
    private SuggestionExtractor suggestionExtractor;

    @Before
    public void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        suggestionExtractor = SoundCloud.getService().getSuggestionExtractor();
    }

    @Test
    public void testIfSuggestions() throws IOException, ExtractionException {
        assertFalse(suggestionExtractor.suggestionList("lil uzi vert", "de").isEmpty());
    }
}
