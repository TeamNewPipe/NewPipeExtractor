package org.schabi.newpipe.extractor.services.peertube.extractors;

import java.io.IOException;
import java.util.List;

import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.utils.Localization;

public class PeertubeSuggestionExtractor extends SuggestionExtractor{

    public PeertubeSuggestionExtractor(int serviceId, Localization localization) {
        super(serviceId, localization);
        // TODO Auto-generated constructor stub
    }

    @Override
    public List<String> suggestionList(String query) throws IOException, ExtractionException {
        // TODO Auto-generated method stub
        return null;
    }

}
