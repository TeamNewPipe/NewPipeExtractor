package org.schabi.newpipe.extractor.services.youtube;

import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/*
 * Created by Christian Schabesberger on 30.12.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * YoutubeVideoExtractorGema.java is part of NewPipe.
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
 * This exception is only thrown in Germany.
 * <p>
 * WARNING: Deactivate this Test Case before uploading it to Github, otherwise CI will fail.
 */
@Ignore
public class YoutubeStreamExtractorGemaTest {

    @Test
    public void testGemaError() throws IOException, ExtractionException {
        try {
            NewPipe.init(Downloader.getInstance());
            YouTube.getService().getStreamExtractor("https://www.youtube.com/watch?v=3O1_3zBUKM8");

            fail("GemaException should be thrown");
        } catch (YoutubeStreamExtractor.GemaException ignored) {
            // Exception was thrown, Gema error detection is working.
        }
    }
}
