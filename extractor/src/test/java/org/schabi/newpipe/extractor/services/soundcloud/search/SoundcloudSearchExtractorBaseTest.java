package org.schabi.newpipe.extractor.services.soundcloud.search;

import org.junit.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudSearchExtractor;

import static org.junit.Assert.assertTrue;


/*
 * Created by Christian Schabesberger on 17.06.18
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * SoundcloudSearchExtractorBaseTest.java is part of NewPipe.
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
 * Test for {@link SoundcloudSearchExtractor}
 */
public abstract class SoundcloudSearchExtractorBaseTest {

    protected static SoundcloudSearchExtractor extractor;
    protected static ListExtractor.InfoItemsPage<InfoItem> itemsPage;


    protected static String removeClientId(String url) {
        String[] splitUrl = url.split("client_id=[a-zA-Z0-9]*&");
        return splitUrl[0] + splitUrl[1];
    }

    @Test
    public void testResultListElementsLength() {
        assertTrue(Integer.toString(itemsPage.getItems().size()),
                itemsPage.getItems().size() >= 3);
    }

    @Test
    public void testUrl() throws Exception {
        assertTrue(extractor.getUrl(), extractor.getUrl().startsWith("https://api-v2.soundcloud.com/search"));
    }
}
