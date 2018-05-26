package org.schabi.newpipe.extractor.search;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
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
    @Nonnull
    public final List<InfoItem> resultList;
    @Nonnull
    public final List<Throwable> errors;

    public SearchResult(int serviceId, String suggestion, List<InfoItem> results, List<Throwable> errors) {
        this.serviceId = serviceId;
        this.suggestion = suggestion;
        this.resultList = Collections.unmodifiableList(new ArrayList<>(results));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public static SearchResult getSearchResult(@Nonnull final SearchEngine engine, final String query, final int page,
                                               final String languageCode, final SearchEngine.Filter filter)
            throws IOException, ExtractionException {
        return null;
    }

    public String getSuggestion() {
        return suggestion;
    }


    @Nonnull
    public List<InfoItem> getResults() {
        return Collections.unmodifiableList(resultList);
    }

    @Nonnull
    public List<Throwable> getErrors() {
        return errors;
    }

    public int getServiceId() {
        return serviceId;
    }
}
