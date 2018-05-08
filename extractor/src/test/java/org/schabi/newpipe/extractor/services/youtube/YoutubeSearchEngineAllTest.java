package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSearchEngine;

import static org.junit.Assert.assertEquals;
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
public class YoutubeSearchEngineAllTest extends BaseYoutubeSearchTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(Downloader.getInstance());
        YoutubeSearchEngine engine = new YoutubeSearchEngine(1);

        result = engine.search("pewdiepie", 0, "de", SearchEngine.Filter.ANY)
                .getSearchResult();
    }

    @Test
    public void testResultList_FirstElement() {
        InfoItem firstInfoItem = result.getResults().get(0);

        // THe channel should be the first item
        assertTrue(firstInfoItem instanceof ChannelInfoItem);
        assertEquals("name", "PewDiePie", firstInfoItem.getName());
        assertEquals("url","https://www.youtube.com/user/PewDiePie", firstInfoItem.getUrl());
    }

    @Ignore
    @Test
    public void testSuggestion() {
        //todo write a real test
        assertTrue(result.getSuggestion() != null);
    }
}
