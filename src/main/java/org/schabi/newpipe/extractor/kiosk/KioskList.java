package org.schabi.newpipe.extractor.kiosk;

import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public  class KioskList {
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

    public void addKioskEntry(KioskExtractor extractor, UrlIdHandler handler)
        throws Exception {
        if(kioskList.get(extractor.getType()) != null) {
            throw new Exception("Kiosk with type " + extractor.getType() + " already exists.");
        }
        kioskList.put(extractor.getType(), new KioskEntry(extractor, handler));
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
