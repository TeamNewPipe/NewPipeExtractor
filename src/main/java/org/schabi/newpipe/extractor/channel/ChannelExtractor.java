package org.schabi.newpipe.extractor.channel;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * ChannelExtractor.java is part of NewPipe.
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

public abstract class ChannelExtractor extends ListExtractor {

    public ChannelExtractor(StreamingService service, String url) {
        super(service, url);
    }

    @Nonnull
    @Override
    protected UrlIdHandler getUrlIdHandler() {
        return getService().getChannelUrlIdHandler();
    }

    @NonNull
    @Override
    public InfoItemsCollector getInfoItems()
        throws IOException, ExtractionException {
        return getStreams();
    }

    public abstract StreamInfoItemsCollector getStreams() throws IOException, ExtractionException;
    public abstract String getAvatarUrl() throws ParsingException;
    public abstract String getBannerUrl() throws ParsingException;
    public abstract String getFeedUrl() throws ParsingException;
    public abstract long getSubscriberCount() throws ParsingException;
    public abstract String getDescription() throws ParsingException;
}
