package org.schabi.newpipe.extractor.services.youtube;

import org.json.JSONArray;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;
import java.net.URLEncoder;
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

    public static final String CHARSET_UTF_8 = "UTF-8";

    public YoutubeSuggestionExtractor(int serviceId) {
        super(serviceId);
    }

    @Override
    public List<String> suggestionList(String query, String contentCountry) throws IOException, ExtractionException {
        Downloader dl = NewPipe.getDownloader();
        List<String> suggestions = new ArrayList<>();

        String url = "https://suggestqueries.google.com/complete/search"
                + "?client=" + "firefox" // 'toolbar' for xml
                + "&ds=" + "yt"
                + "&hl=" + URLEncoder.encode(contentCountry, CHARSET_UTF_8)
                + "&q=" + URLEncoder.encode(query, CHARSET_UTF_8);

        String response = dl.download(url);
        try {
            JSONArray suggestionsArray = new JSONArray(response).getJSONArray(1);
            for (Object suggestion : suggestionsArray) suggestions.add(suggestion.toString());
        } catch (Exception e) {
            throw new ParsingException("Could not parse suggestions response.", e);
        }

        return suggestions;
    }
}
