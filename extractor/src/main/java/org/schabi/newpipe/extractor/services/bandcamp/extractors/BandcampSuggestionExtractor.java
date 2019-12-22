// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampSearchQueryHandlerFactory.CHARSET_UTF_8;

public class BandcampSuggestionExtractor extends SuggestionExtractor {

    private static final String AUTOCOMPLETE_URL = "https://bandcamp.com/api/fuzzysearch/1/autocomplete?q=";
    public BandcampSuggestionExtractor(StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(String query) throws IOException, ExtractionException {
        Downloader downloader = NewPipe.getDownloader();

        JSONObject fuzzyResults = new JSONObject(
                downloader.get(AUTOCOMPLETE_URL + URLEncoder.encode(query, CHARSET_UTF_8)).responseBody()
        );

        JSONArray jsonArray = fuzzyResults.getJSONObject("auto")
                .getJSONArray("results");

        ArrayList<String> suggestions = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject fuzzyResult = jsonArray.getJSONObject(i);
            String res = fuzzyResult.getString("name");

            if (!suggestions.contains(res)) suggestions.add(res);
        }



        return suggestions;
    }
}
