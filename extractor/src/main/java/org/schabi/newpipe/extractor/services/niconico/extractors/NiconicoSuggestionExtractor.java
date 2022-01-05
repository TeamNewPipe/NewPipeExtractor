package org.schabi.newpipe.extractor.services.niconico.extractors;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.niconico.NiconicoService;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

public class NiconicoSuggestionExtractor extends SuggestionExtractor {
    public NiconicoSuggestionExtractor(final StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(final String query)
            throws IOException, ExtractionException {
        final List<String> suggestions = new ArrayList<>();
        final String encoded = URLEncoder.encode(query, UTF_8);
        final String response = NewPipe.getDownloader()
                .get(NiconicoService.SUGGESTION_URL + encoded).responseBody();
        try {
            final JsonArray jsonArray = JsonParser.object().from(response).getArray("candidates");
            for (int i = 0; i < jsonArray.size(); i++) {
                suggestions.add(jsonArray.getString(i));
            }
        } catch (final JsonParserException e) {
            throw new ExtractionException("could not parse search suggestions.");
        }

        return suggestions;
    }
}
