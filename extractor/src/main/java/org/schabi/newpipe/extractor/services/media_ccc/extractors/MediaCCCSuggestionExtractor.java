package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaCCCSuggestionExtractor extends SuggestionExtractor {

    public MediaCCCSuggestionExtractor(StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(String query) throws IOException, ExtractionException {
        return new ArrayList<>(0);
    }
}
