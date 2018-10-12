package org.schabi.newpipe.extractor.services.peertube.extractors;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.utils.Localization;

public class PeertubeSuggestionExtractor extends SuggestionExtractor{

    public PeertubeSuggestionExtractor(int serviceId, Localization localization) {
        super(serviceId, localization);
    }

    @Override
    public List<String> suggestionList(String query) throws IOException, ExtractionException {
        return Collections.emptyList();
    }

}
