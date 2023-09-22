package org.schabi.newpipe.extractor.kiosk;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * KioskExtractor.java is part of NewPipe Extractor.
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

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import javax.annotation.Nonnull;

public abstract class KioskExtractor<T extends InfoItem> extends ListExtractor<T> {
    private final String id;

    public KioskExtractor(final StreamingService streamingService,
                          final ListLinkHandler linkHandler,
                          final String kioskId) {
        super(streamingService, linkHandler);
        this.id = kioskId;
    }

    @Nonnull
    @Override
    public String getId() {
        return id;
    }

    /**
     * Id should be the name of the kiosk, tho Id is used for identifying it in the frontend,
     * so id should be kept in english.
     * In order to get the name of the kiosk in the desired language we have to
     * crawl if from the website.
     * @return the translated version of id
     */
    @Nonnull
    @Override
    public abstract String getName() throws ParsingException;
}
