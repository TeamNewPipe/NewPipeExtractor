// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BandcampSuggestionExtractor extends SuggestionExtractor {

    private static final String AUTOCOMPLETE_URL = "https://bandcamp.com/api/fuzzysearch/1/autocomplete?q=";
    public BandcampSuggestionExtractor(StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(String query) throws IOException, ExtractionException {
        Downloader downloader = NewPipe.getDownloader();

        try {
            JsonObject fuzzyResults = JsonParser.object().from(
                    downloader.get(AUTOCOMPLETE_URL + URLEncoder.encode(query, "UTF-8")).responseBody()
            );

            JsonArray jsonArray = fuzzyResults.getObject("auto")
                    .getArray("results");

            ArrayList<String> suggestions = new ArrayList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject fuzzyResult = jsonArray.getObject(i);
                String res = fuzzyResult.getString("name");

                if (!suggestions.contains(res)) suggestions.add(res);
            }

            return suggestions;
        } catch (JsonParserException e) {
            e.printStackTrace();

            return new ArrayList<>();
        }


    }
}
