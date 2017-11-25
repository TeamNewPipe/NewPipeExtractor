package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.search.SearchResult;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
 * Created by Christian Schabesberger on 29.12.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * YoutubeSearchEngineStreamTest.java is part of NewPipe.
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

/**
 * Test for {@link SearchEngine}
 */
public class YoutubeSearchEngineAllTest {
    private static SearchResult result;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(Downloader.getInstance());
        YoutubeSearchEngine engine = new YoutubeSearchEngine(1);

        // Youtube will suggest "asdf" instead of "asdgff"
        // keep in mind that the suggestions can change by country (the parameter "de")
        result = engine.search("asdgff", 0, "de", SearchEngine.Filter.ANY)
                .getSearchResult();
    }

    @Test
    public void testResultList() {
        System.out.println("Results: " + result.getResults());
        assertFalse("Results are empty: " + result.resultList, result.resultList.isEmpty());
    }

    @Test
    public void testResultErrors() {
        for (Throwable error : result.getErrors()) {
            error.printStackTrace();
        }
        assertTrue(result.getErrors().isEmpty());
    }

    @Ignore
    @Test
    public void testSuggestion() {
        //todo write a real test
        assertTrue(result.getSuggestion() != null);
    }
}
