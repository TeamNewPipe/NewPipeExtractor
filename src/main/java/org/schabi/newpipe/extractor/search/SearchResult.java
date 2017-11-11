package org.schabi.newpipe.extractor.search;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*
 * Created by Christian Schabesberger on 29.02.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * SearchResult.java is part of NewPipe.
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

public class SearchResult {
    private final int serviceId;
    public final String suggestion;
    public final List<InfoItem> resultList;
    public final List<Throwable> errors;

    public SearchResult(int serviceId, String suggestion, List<InfoItem> results, List<Throwable> errors) {
        this.serviceId = serviceId;
        this.suggestion = suggestion;
        this.resultList = Collections.unmodifiableList(new ArrayList<>(results));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public static SearchResult getSearchResult(SearchEngine engine, String query, int page, String languageCode, SearchEngine.Filter filter)
            throws IOException, ExtractionException {

        SearchResult result = engine
                .search(query, page, languageCode, filter)
                .getSearchResult();
        if (result.resultList.isEmpty()) {
            if (result.suggestion.isEmpty()) {
                if (result.errors.isEmpty()) {
                    throw new ExtractionException("Empty result despite no error");
                }
            } else {
                // This is used as a fallback. Do not relay on it !!!
                throw new SearchEngine.NothingFoundException(result.suggestion);
            }
        }
        return result;
    }

    public String getSuggestion() {
        return suggestion;
    }


    public List<InfoItem> getResults() {
        return Collections.unmodifiableList(resultList);
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    public int getServiceId() {
        return serviceId;
    }
}
