package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getCookieHeader;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by Christian Schabesberger on 28.09.16.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * YoutubeSuggestionExtractor.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class YoutubeSuggestionExtractor extends SuggestionExtractor {

    public YoutubeSuggestionExtractor(final StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(final String query) throws IOException, ExtractionException {
        final Downloader dl = NewPipe.getDownloader();
        final List<String> suggestions = new ArrayList<>();

        final String url = "https://suggestqueries.google.com/complete/search"
                + "?client=" + "youtube" //"firefox" for JSON, 'toolbar' for xml
                + "&jsonp=" + "JP"
                + "&ds=" + "yt"
                + "&gl=" + Utils.encodeUrlUtf8(getExtractorContentCountry().getCountryCode())
                + "&q=" + Utils.encodeUrlUtf8(query);

        String response = dl.get(url, getCookieHeader(), getExtractorLocalization()).responseBody();
        // trim JSONP part "JP(...)"
        response = response.substring(3, response.length() - 1);
        try {
            final JsonArray collection = JsonParser.array().from(response).getArray(1);
            for (final Object suggestion : collection) {
                if (!(suggestion instanceof JsonArray)) {
                    continue;
                }
                final String suggestionStr = ((JsonArray) suggestion).getString(0);
                if (suggestionStr == null) {
                    continue;
                }
                suggestions.add(suggestionStr);
            }

            return suggestions;
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }
    }
}
