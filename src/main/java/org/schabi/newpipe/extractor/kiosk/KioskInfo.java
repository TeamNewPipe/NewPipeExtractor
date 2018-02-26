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

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;

public class KioskInfo extends ListInfo {

    private KioskInfo(int serviceId, String id, String url, String name) {
        super(serviceId, id, url, name);
    }

    public static ListExtractor.InfoItemPage getMoreItems(StreamingService service,
                                                             String url,
                                                             String pageUrl,
                                                             String contentCountry) throws IOException, ExtractionException {
        KioskList kl = service.getKioskList();
        KioskExtractor extractor = kl.getExtractorByUrl(url, pageUrl);
        extractor.setContentCountry(contentCountry);
        return extractor.getPage(pageUrl);
    }

    public static KioskInfo getInfo(String url,
                                    String contentCountry) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url, contentCountry);
    }

    public static KioskInfo getInfo(StreamingService service,
                                    String url,
                                    String contentCountry) throws IOException, ExtractionException {
        KioskList kl = service.getKioskList();
        KioskExtractor extractor = kl.getExtractorByUrl(url, null);
        extractor.setContentCountry(contentCountry);
        extractor.fetchPage();
        return getInfo(extractor);
    }

    /**
     * Get KioskInfo from KioskExtractor
     *
     * @param extractor an extractor where fetchPage() was already got called on.
     */
    public static KioskInfo getInfo(KioskExtractor extractor) throws ExtractionException {

        int serviceId = extractor.getServiceId();
        String name = extractor.getName();
        String id = extractor.getId();
        String url = extractor.getCleanUrl();

        KioskInfo info = new KioskInfo(serviceId, id, name, url);

        info.related_streams = ExtractorHelper.getInfoItemsOrLogError(info, extractor);

        return info;
    }
}
