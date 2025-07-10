package org.schabi.newpipe.extractor.services.media_ccc.search;

import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;
import static org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory.CONFERENCES;
import static org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory.EVENTS;
import static java.util.Collections.singletonList;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;

import javax.annotation.Nullable;

public class MediaCCCSearchExtractorTest {
    public static class All extends DefaultSearchExtractorTest {
        private static final String QUERY = "kde";

        @Override
        protected SearchExtractor createExtractor() throws Exception {
            return MediaCCC.getSearchExtractor(QUERY);
        }

        @Override public StreamingService expectedService() { return MediaCCC; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Override public boolean expectedHasMoreItems() { return false; }
    }

    public static class Conferences extends DefaultSearchExtractorTest {
        private static final String QUERY = "c3";

        @Override
        protected SearchExtractor createExtractor() throws Exception {
            return MediaCCC.getSearchExtractor(QUERY, singletonList(CONFERENCES), "");
        }

        @Override public StreamingService expectedService() { return MediaCCC; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Nullable @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.CHANNEL; }
        @Override public boolean expectedHasMoreItems() { return false; }
    }

    public static class Events extends DefaultSearchExtractorTest {
        private static final String QUERY = "linux";

        @Override
        protected SearchExtractor createExtractor() throws Exception {
            return MediaCCC.getSearchExtractor(QUERY, singletonList(EVENTS), "");
        }

        @Override public StreamingService expectedService() { return MediaCCC; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "media.ccc.de/public/events/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }

        @Nullable @Override public InfoItem.InfoType expectedInfoItemType() { return InfoItem.InfoType.STREAM; }
        @Override public boolean expectedHasMoreItems() { return false; }
    }
}
