// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_API_URL;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BandcampSuggestionExtractor extends SuggestionExtractor {

    private static final String AUTOCOMPLETE_URL = BASE_API_URL
            + "/bcsearch_public_api/1/autocomplete_elastic";
    public BandcampSuggestionExtractor(final StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(final String query) throws IOException, ExtractionException {
        final Downloader downloader = NewPipe.getDownloader();

        try {
            final JsonObject fuzzyResults = JsonParser.object().from(downloader
                    .postWithContentTypeJson(
                            AUTOCOMPLETE_URL,
                            Collections.emptyMap(),
                            JsonWriter.string()
                                    .object()
                                    .value("fan_id", (String) null)
                                    .value("full_page", false)
                                    .value("search_filter", "")
                                    .value("search_text", query)
                                    .end()
                                    .done()
                                    .getBytes(StandardCharsets.UTF_8)).responseBody());

            return fuzzyResults.getObject("auto").getArray("results").stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(jsonObject -> jsonObject.getString("name"))
                    .distinct()
                    .collect(Collectors.toList());
        } catch (final JsonParserException e) {
            return Collections.emptyList();
        }
    }
}
