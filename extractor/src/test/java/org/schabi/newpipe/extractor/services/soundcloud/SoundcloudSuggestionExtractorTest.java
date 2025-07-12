package org.schabi.newpipe.extractor.services.soundcloud;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.DefaultSimpleUntypedExtractorTest;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.io.IOException;

/**
 * Test for {@link SuggestionExtractor}
 */
public class SoundcloudSuggestionExtractorTest extends DefaultSimpleUntypedExtractorTest<SuggestionExtractor> {

    @Override
    protected SuggestionExtractor createExtractor() throws Exception {
        return SoundCloud.getSuggestionExtractor();
    }

    @Test
    public void testIfSuggestions() throws IOException, ExtractionException {
        assertFalse(extractor().suggestionList("lil uzi vert").isEmpty());
    }
}
