package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;

public class SoundcloudSuggestionExtractor extends SuggestionExtractor {

    public SoundcloudSuggestionExtractor(StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(String query) throws IOException, ExtractionException {
        List<String> suggestions = new ArrayList<>();

        Downloader dl = NewPipe.getDownloader();

        String url = "https://api-v2.soundcloud.com/search/queries"
                + "?q=" + URLEncoder.encode(query, UTF_8)
                + "&client_id=" + SoundcloudParsingHelper.clientId()
                + "&limit=10";

        String response = dl.get(url, getExtractorLocalization()).responseBody();
        try {
            JsonArray collection = JsonParser.object().from(response).getArray("collection");
            for (Object suggestion : collection) {
                if (suggestion instanceof JsonObject) suggestions.add(((JsonObject) suggestion).getString("query"));
            }

            return suggestions;
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }
    }
}
