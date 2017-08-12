package org.schabi.newpipe.extractor.kiosk;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * KioskInfo.java is part of NewPipe.
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

import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import java.io.IOException;

public class KioskInfo extends ListInfo {
    public String type;

    public static KioskInfo getInfo(String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static KioskInfo getInfo(ServiceList serviceItem, String url) throws IOException, ExtractionException {
        return getInfo(serviceItem.getService(), url);
    }

    public static KioskInfo getInfo(StreamingService service, String url) throws IOException, ExtractionException {
        KioskList kl = service.getKioskList();
        KioskExtractor extractor = kl.getExtryctorByUrl(url);
        return getInfo(extractor);
    }

    public static KioskInfo getInfo(KioskExtractor extractor) throws ParsingException {
        KioskInfo info = new KioskInfo();
        info.type = extractor.getType();
        info.name = extractor.getName();
        info.id = extractor.getId();

        try {
            StreamInfoItemCollector c = extractor.getStreams();
            info.related_streams = c.getItemList();
            info.errors.addAll(c.getErrors());
        } catch (Exception e) {
            info.errors.add(e);
        }

        return info;
    }
}
