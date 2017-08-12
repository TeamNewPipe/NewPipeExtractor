package org.schabi.newpipe.extractor.kiosk;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * KioskExtractor.java is part of NewPipe.
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

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;

public abstract class KioskExtractor extends ListExtractor {
    public KioskExtractor(StreamingService streamingService, String url, String nextStreamsUrl)
        throws IOException, ExtractionException {
        super(streamingService, url, nextStreamsUrl);
    }

    /**
     * Returns the type of the kiosk.
     * eg. Trending, Top & Hot, Top last 24 hours
     * @return type of kiosk
     */
    public abstract String getType() throws ParsingException;

    @Override
    public String getId() throws ParsingException {
        return getType();
    }

    @Override
    public String getName() throws ParsingException {
        return getType();
    }
}
