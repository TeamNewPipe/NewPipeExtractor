package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;

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
import java.util.List;
import java.util.stream.Collectors;

public class SoundcloudSuggestionExtractor extends SuggestionExtractor {

    public SoundcloudSuggestionExtractor(final StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(final String query) throws IOException,
            ExtractionException {
        final Downloader dl = NewPipe.getDownloader();
        final var url = SOUNDCLOUD_API_V2_URL.newBuilder()
                .addPathSegments("search/queries")
                .addQueryParameter("q", query)
                .addQueryParameter("client_id", SoundcloudParsingHelper.clientId())
                .addQueryParameter("limit", "10")
                .toString();
        final String response = dl.get(url, getExtractorLocalization()).responseBody();

        try {
            return JsonParser.object().from(response).getArray("collection").stream()
                    .filter(JsonObject.class::isInstance)
                    .map(suggestion -> ((JsonObject) suggestion).getString("query"))
                    .collect(Collectors.toList());
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }
    }
}
