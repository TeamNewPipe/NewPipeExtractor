package org.schabi.newpipe.extractor.services.bitchute.extractor;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.util.Arrays;
import java.util.List;

public class BitchuteSuggestionExtractor extends SuggestionExtractor {

    private static final String AUTOCOMPLETE_URL
            = "https://search.bitchute.com/autocompletion?name=autocomplete&query=";

    public BitchuteSuggestionExtractor(StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(String query) {
        try {
            Response response = NewPipe.getDownloader().get(AUTOCOMPLETE_URL + query);
            return Arrays.asList(response.responseBody().split("\n", 0));
        } catch (Exception e) {
            return Collections.emptyList();;
        }
    }
}
