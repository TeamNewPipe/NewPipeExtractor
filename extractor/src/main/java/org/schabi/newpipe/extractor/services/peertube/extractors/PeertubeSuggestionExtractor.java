package org.schabi.newpipe.extractor.services.peertube.extractors;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.util.Collections;
import java.util.List;

public class PeertubeSuggestionExtractor extends SuggestionExtractor {
    public PeertubeSuggestionExtractor(final StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(final String query) {
        return Collections.emptyList();
    }
}
