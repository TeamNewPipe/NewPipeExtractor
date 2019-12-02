package org.schabi.newpipe.extractor.services.peertube.extractors;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

public class PeertubeSuggestionExtractor extends SuggestionExtractor{

    public PeertubeSuggestionExtractor(StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(String query) throws IOException, ExtractionException {
        return Collections.emptyList();
    }

}
