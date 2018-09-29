package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.utils.Localization;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/**
 * Test for {@link SuggestionExtractor}
 */
public class SoundcloudSuggestionExtractorTest {
    private static SuggestionExtractor suggestionExtractor;

    @BeforeClass
    public static void setUp() {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
        suggestionExtractor = SoundCloud.getSuggestionExtractor();
    }

    @Test
    public void testIfSuggestions() throws IOException, ExtractionException {
        assertFalse(suggestionExtractor.suggestionList("lil uzi vert").isEmpty());
    }
}
