package org.schabi.newpipe.extractor.kiosk;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * KioskList.java is part of NewPipe.
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

import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KioskList {
    private int service_id;
    private HashMap<String, KioskEntry> kioskList = new HashMap<>();

    private class KioskEntry {
        public KioskEntry(KioskExtractor e, UrlIdHandler h) {
            extractor = e;
            handler = h;
        }
        KioskExtractor extractor;
        UrlIdHandler handler;
    }

    public KioskList(int service_id) {
        this.service_id = service_id;
    }

    public void addKioskEntry(String kioskType, KioskExtractor extractor, UrlIdHandler handler)
        throws Exception {
        if(kioskList.get(kioskType) != null) {
            throw new Exception("Kiosk with type " + kioskType + " already exists.");
        }
        kioskList.put(kioskType, new KioskEntry(extractor, handler));
    }

    public KioskExtractor getExtractorByType(String kioskType) throws ExtractionException {
        KioskEntry ke = kioskList.get(kioskType);
        if(ke == null) {
            throw new ExtractionException("No kiosk found with the type: " + kioskType);
        } else {
            return ke.extractor;
        }
    }

    public Set<String> getAvailableKisokTypes() {
        return kioskList.keySet();
    }

    public KioskExtractor getExtryctorByUrl(String url) throws ExtractionException {
        for(Map.Entry<String, KioskEntry> e : kioskList.entrySet()) {
            KioskEntry ke = e.getValue();
            if(ke.handler.acceptUrl(url)) {
                return getExtractorByType(e.getKey());
            }
        }
        throw new ExtractionException("Could not find a kiosk that fits to the url: " + url);
    }
}
