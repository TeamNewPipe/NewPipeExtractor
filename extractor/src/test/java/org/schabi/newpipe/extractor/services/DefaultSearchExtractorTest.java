package org.schabi.newpipe.extractor.services;

import org.junit.Test;
import org.schabi.newpipe.extractor.search.SearchExtractor;


import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public abstract class DefaultSearchExtractorTest extends DefaultListExtractorTest<SearchExtractor>
        implements BaseSearchExtractorTest {

    public abstract String expectedSearchString();
    @Nullable public abstract String expectedSearchSuggestion();

    public boolean isCorrectedSearch() {
        return false;
    }

    @Test
    @Override
    public void testSearchString() throws Exception {
        assertEquals(expectedSearchString(), extractor().getSearchString());
    }

    @Test
    @Override
    public void testSearchSuggestion() throws Exception {
        final String expectedSearchSuggestion = expectedSearchSuggestion();
        if (isNullOrEmpty(expectedSearchSuggestion)) {
            assertEmpty("Suggestion was expected to be empty", extractor().getSearchSuggestion());
        } else {
            assertEquals(expectedSearchSuggestion, extractor().getSearchSuggestion());
        }
    }

    @Test
    public void testSearchCorrected() throws Exception {
        assertEquals(isCorrectedSearch(), extractor().isCorrectedSearch());
    }
}
