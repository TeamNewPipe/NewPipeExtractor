// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.linkHandler;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.util.List;

import okhttp3.HttpUrl;

public class BandcampSearchQueryHandlerFactory extends SearchQueryHandlerFactory {
    @Override
    public String getUrl(final String query,
                         final List<String> contentFilter,
                         final String sortFilter) throws ParsingException {
        return HttpUrl.get(BASE_URL).newBuilder()
                .addPathSegment("search")
                .addQueryParameter("q", query)
                .addQueryParameter("page", "1")
                .toString();
    }
}
