/*
 * Created by Christian Schabesberger on 28.02.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * StreamInfoItemsCollector.java is part of NewPipe Extractor.
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
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.Comparator;

public class StreamInfoItemsCollector
        extends InfoItemsCollector<StreamInfoItem, StreamInfoItemExtractor> {

    public StreamInfoItemsCollector(final int serviceId) {
        super(serviceId);
    }

    public StreamInfoItemsCollector(final int serviceId,
                                    final Comparator<StreamInfoItem> comparator) {
        super(serviceId, comparator);
    }

    @Override
    public StreamInfoItem extract(final StreamInfoItemExtractor extractor) throws ParsingException {
        if (extractor.isAd()) {
            throw new FoundAdException("Found ad");
        }

        final var resultItem = new StreamInfoItem(getServiceId(), extractor.getUrl(),
                extractor.getName(), extractor.getStreamType());

        // optional information
        try {
            resultItem.setDurationObject(extractor.getDurationObject());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploaderName(extractor.getUploaderName());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setTextualUploadDate(extractor.getTextualUploadDate());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploadDate(extractor.getUploadDate());
        } catch (final ParsingException e) {
            addError(e);
        }
        try {
            resultItem.setViewCount(extractor.getViewCount());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setThumbnails(extractor.getThumbnails());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploaderUrl(extractor.getUploaderUrl());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploaderAvatars(extractor.getUploaderAvatars());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploaderVerified(extractor.isUploaderVerified());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setShortDescription(extractor.getShortDescription());
        } catch (final Exception e) {
            addError(e);
        }
        try {
            resultItem.setShortFormContent(extractor.isShortFormContent());
        } catch (final Exception e) {
            addError(e);
        }

        return resultItem;
    }

    @Override
    public void commit(final StreamInfoItemExtractor extractor) {
        try {
            addItem(extract(extractor));
        } catch (final FoundAdException ignored) {
        } catch (final Exception e) {
            addError(e);
        }
    }
}
