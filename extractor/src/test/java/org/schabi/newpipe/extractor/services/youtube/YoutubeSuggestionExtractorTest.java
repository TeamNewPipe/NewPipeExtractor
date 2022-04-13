package org.schabi.newpipe.extractor.services.youtube;

/*
 * Created by Christian Schabesberger on 18.11.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * YoutubeSuggestionExtractorTest.java is part of NewPipe.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.io.IOException;

/**
 * Test for {@link SuggestionExtractor}
 */
public class YoutubeSuggestionExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/suggestions/";

    private static SuggestionExtractor suggestionExtractor;

    @BeforeAll
    public static void setUp() throws Exception {
        YoutubeTestsUtils.ensureStateless();
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + ""), new Localization("de", "DE"));
        suggestionExtractor = YouTube.getSuggestionExtractor();
    }

    @Test
    public void testIfSuggestions() throws IOException, ExtractionException {
        assertFalse(suggestionExtractor.suggestionList("hello").isEmpty());
    }
}
