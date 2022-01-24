package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.search.SearchExtractor;


import javax.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public abstract class DefaultSearchExtractorTest extends DefaultListExtractorTest<SearchExtractor>
        implements BaseSearchExtractorTest {

    public abstract String expectedSearchString();
    @Nullable public abstract String expectedSearchSuggestion();

    public boolean isCorrectedSearch() {
        return false;
    }

    public List<MetaInfo> expectedMetaInfo() throws MalformedURLException {
        return Collections.emptyList();
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

    /**
     * @see DefaultStreamExtractorTest#testMetaInfo()
     */
    @Test
    public void testMetaInfo() throws Exception {
        final List<MetaInfo> metaInfoList = extractor().getMetaInfo();
        final List<MetaInfo> expectedMetaInfoList = expectedMetaInfo();

        for (final MetaInfo expectedMetaInfo : expectedMetaInfoList) {
            final List<String> texts = metaInfoList.stream()
                    .map(metaInfo -> metaInfo.getContent().getContent())
                    .collect(Collectors.toList());
            final List<String> titles = metaInfoList.stream().map(MetaInfo::getTitle).collect(Collectors.toList());
            final List<URL> urls = metaInfoList.stream().flatMap(info -> info.getUrls().stream())
                    .collect(Collectors.toList());
            final List<String> urlTexts = metaInfoList.stream().flatMap(info -> info.getUrlTexts().stream())
                    .collect(Collectors.toList());

            assertTrue(texts.contains(expectedMetaInfo.getContent().getContent()));
            assertTrue(titles.contains(expectedMetaInfo.getTitle()));

            for (final String expectedUrlText : expectedMetaInfo.getUrlTexts()) {
                assertTrue(urlTexts.contains(expectedUrlText));
            }
            for (final URL expectedUrl : expectedMetaInfo.getUrls()) {
                assertTrue(urls.contains(expectedUrl));
            }
        }
    }
}
