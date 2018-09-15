package org.schabi.newpipe.extractor.services.youtube.search;

import org.junit.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSearchExtractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/*
 * Created by Christian Schabesberger on 27.05.18
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * YoutubeSearchExtractorBaseTest.java is part of NewPipe.
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
 * Test for {@link YoutubeSearchExtractor}
 */
public abstract class YoutubeSearchExtractorBaseTest {

    protected static YoutubeSearchExtractor extractor;
    protected static ListExtractor.InfoItemsPage<InfoItem> itemsPage;


    @Test
    public void testResultListElementsLength() {
        assertTrue(Integer.toString(itemsPage.getItems().size()),
                itemsPage.getItems().size() > 10);
    }

    @Test
    public void testUrl() throws Exception {
        assertTrue(extractor.getUrl(), extractor.getUrl().startsWith("https://www.youtube.com"));
    }
}
