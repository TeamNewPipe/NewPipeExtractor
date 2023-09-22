package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemsCollector;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * InfoItemsSearchCollector.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * A collector that can handle many extractor types, to be used when a list contains items of
 * different types (e.g. search)
 * <p>
 * This collector can handle the following extractor types:
 * <ul>
 *     <li>{@link StreamInfoItemExtractor}</li>
 *     <li>{@link ChannelInfoItemExtractor}</li>
 *     <li>{@link PlaylistInfoItemExtractor}</li>
 * </ul>
 * Calling {@link #extract(InfoItemExtractor)} or {@link #commit(InfoItemExtractor)} with any
 * other extractor type will raise an exception.
 */
public class MultiInfoItemsCollector extends InfoItemsCollector<InfoItem, InfoItemExtractor> {
    private final StreamInfoItemsCollector streamCollector;
    private final ChannelInfoItemsCollector userCollector;
    private final PlaylistInfoItemsCollector playlistCollector;

    public MultiInfoItemsCollector(final int serviceId) {
        super(serviceId);
        streamCollector = new StreamInfoItemsCollector(serviceId);
        userCollector = new ChannelInfoItemsCollector(serviceId);
        playlistCollector = new PlaylistInfoItemsCollector(serviceId);
    }

    @Override
    public List<Throwable> getErrors() {
        final List<Throwable> errors = new ArrayList<>(super.getErrors());
        errors.addAll(streamCollector.getErrors());
        errors.addAll(userCollector.getErrors());
        errors.addAll(playlistCollector.getErrors());

        return Collections.unmodifiableList(errors);
    }

    @Override
    public void reset() {
        super.reset();
        streamCollector.reset();
        userCollector.reset();
        playlistCollector.reset();
    }

    @Override
    public InfoItem extract(final InfoItemExtractor extractor) throws ParsingException {
        // Use the corresponding collector for each item extractor type
        if (extractor instanceof StreamInfoItemExtractor) {
            return streamCollector.extract((StreamInfoItemExtractor) extractor);
        } else if (extractor instanceof ChannelInfoItemExtractor) {
            return userCollector.extract((ChannelInfoItemExtractor) extractor);
        } else if (extractor instanceof PlaylistInfoItemExtractor) {
            return playlistCollector.extract((PlaylistInfoItemExtractor) extractor);
        } else {
            throw new IllegalArgumentException("Invalid extractor type: " + extractor);
        }
    }
}
