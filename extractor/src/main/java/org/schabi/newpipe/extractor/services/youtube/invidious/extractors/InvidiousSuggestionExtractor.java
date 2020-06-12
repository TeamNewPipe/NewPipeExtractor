package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.io.IOException;
import java.util.List;

import static org.schabi.newpipe.extractor.utils.JsonUtils.getListStringFromJsonArray;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousSuggestionExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

public class InvidiousSuggestionExtractor extends SuggestionExtractor {

    private final String baseUrl;

    public InvidiousSuggestionExtractor(StreamingService service) {
        super(service);
        this.baseUrl = service.getInstance().getUrl();
    }

    @Override
    public List<String> suggestionList(String query) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/search/suggestions?q=" + query;
        final Downloader dl = NewPipe.getDownloader();
        final Response response = dl.get(apiUrl);

        final JsonObject json = InvidiousParsingHelper.getValidJsonObjectFromResponse(response, apiUrl);

        return getListStringFromJsonArray(json.getArray("suggestions"));
    }
}
