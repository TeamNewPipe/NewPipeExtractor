package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.io.IOException;
import java.util.List;

public class InvidiousSuggestionExtractor extends SuggestionExtractor {

    private final String baseUrl;

    public InvidiousSuggestionExtractor(final InvidiousService service) {
        super(service);
        this.baseUrl = service.getInstance().getUrl();
    }

    @Override
    public List<String> suggestionList(final String query) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/search/suggestions?q=" + query;
        final Downloader dl = NewPipe.getDownloader();
        final Response response = dl.get(apiUrl);

        final JsonObject json =
                InvidiousParsingHelper.getValidJsonObjectFromResponse(response, apiUrl);

        return JsonUtils.getStringListFromJsonArray(json.getArray("suggestions"));
    }
}
