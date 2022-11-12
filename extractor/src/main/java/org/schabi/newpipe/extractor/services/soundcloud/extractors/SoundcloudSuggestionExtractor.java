package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;

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
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SoundcloudSuggestionExtractor extends SuggestionExtractor {

    public SoundcloudSuggestionExtractor(final StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(final String query) throws IOException,
            ExtractionException {
        final List<String> suggestions = new ArrayList<>();
        final Downloader dl = NewPipe.getDownloader();
        final String url = SOUNDCLOUD_API_V2_URL + "search/queries?q="
                + Utils.encodeUrlUtf8(query) + "&client_id=" + SoundcloudParsingHelper.clientId()
                + "&limit=10";
        final String response = dl.get(url, getExtractorLocalization()).responseBody();

        try {
            final JsonArray collection = JsonParser.object().from(response).getArray("collection");
            for (final Object suggestion : collection) {
                if (suggestion instanceof JsonObject) {
                    suggestions.add(((JsonObject) suggestion).getString("query"));
                }
            }

            return suggestions;
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }
    }
}
