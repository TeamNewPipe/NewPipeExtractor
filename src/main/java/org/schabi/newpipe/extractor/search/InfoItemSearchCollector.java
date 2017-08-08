package org.schabi.newpipe.extractor.search;

import org.schabi.newpipe.extractor.InfoItemCollector;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.user.UserInfoItemCollector;
import org.schabi.newpipe.extractor.user.UserInfoItemExtractor;

/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * InfoItemSearchCollector.java is part of NewPipe.
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

public class InfoItemSearchCollector extends InfoItemCollector {
    private String suggestion = "";
    private StreamInfoItemCollector streamCollector;
    private UserInfoItemCollector userCollector;

    private SearchResult result = new SearchResult();

    InfoItemSearchCollector(int serviceId) {
        super(serviceId);
        streamCollector = new StreamInfoItemCollector(serviceId);
        userCollector = new UserInfoItemCollector(serviceId);
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public SearchResult getSearchResult() throws ExtractionException {

        addFromCollector(userCollector);
        addFromCollector(streamCollector);

        result.suggestion = suggestion;
        result.errors = getErrors();
        return result;
    }

    public void commit(StreamInfoItemExtractor extractor) {
        try {
            result.resultList.add(streamCollector.extract(extractor));
        } catch (FoundAdException ae) {
            System.err.println("Found ad");
        } catch (Exception e) {
            addError(e);
        }
    }

    public void commit(UserInfoItemExtractor extractor) {
        try {
            result.resultList.add(userCollector.extract(extractor));
        } catch (FoundAdException ae) {
            System.err.println("Found ad");
        } catch (Exception e) {
            addError(e);
        }
    }
}
