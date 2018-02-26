package org.schabi.newpipe.extractor.kiosk;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public  class KioskList {
    public interface KioskExtractorFactory {
        KioskExtractor createNewKiosk(final StreamingService streamingService,
                                             final String url,
                                             final String kioskId)
            throws ExtractionException, IOException;
    }

    private final int service_id;
    private final HashMap<String, KioskEntry> kioskList = new HashMap<>();
    private String defaultKiosk = null;

    private class KioskEntry {
        public KioskEntry(KioskExtractorFactory ef, UrlIdHandler h) {
            extractorFactory = ef;
            handler = h;
        }
        final KioskExtractorFactory extractorFactory;
        final UrlIdHandler handler;
    }

    public KioskList(int service_id) {
        this.service_id = service_id;
    }

    public void addKioskEntry(KioskExtractorFactory extractorFactory, UrlIdHandler handler, String id)
        throws Exception {
        if(kioskList.get(id) != null) {
            throw new Exception("Kiosk with type " + id + " already exists.");
        }
        kioskList.put(id, new KioskEntry(extractorFactory, handler));
    }

    public void setDefaultKiosk(String kioskType) {
        defaultKiosk = kioskType;
    }

    public KioskExtractor getDefaultKioskExtractor(String nextPageUrl)
            throws ExtractionException, IOException {
        if(defaultKiosk != null && !defaultKiosk.equals("")) {
            return getExtractorById(defaultKiosk, nextPageUrl);
        } else {
            if(!kioskList.isEmpty()) {
                // if not set get any entry
                Object[] keySet = kioskList.keySet().toArray();
                return getExtractorById(keySet[0].toString(), nextPageUrl);
            } else {
                return null;
            }
        }
    }

    public String getDefaultKioskId() {
        return defaultKiosk;
    }

    public KioskExtractor getExtractorById(String kioskId, String nextPageUrl)
            throws ExtractionException, IOException {
        KioskEntry ke = kioskList.get(kioskId);
        if(ke == null) {
            throw new ExtractionException("No kiosk found with the type: " + kioskId);
        } else {
            return ke.extractorFactory.createNewKiosk(NewPipe.getService(service_id),
                    ke.handler.getUrl(kioskId), kioskId);
        }
    }

    public Set<String> getAvailableKiosks() {
        return kioskList.keySet();
    }

    public KioskExtractor getExtractorByUrl(String url, String nextPageUrl)
            throws ExtractionException, IOException {
        for(Map.Entry<String, KioskEntry> e : kioskList.entrySet()) {
            KioskEntry ke = e.getValue();
            if(ke.handler.acceptUrl(url)) {
                return getExtractorById(e.getKey(), nextPageUrl);
            }
        }
        throw new ExtractionException("Could not find a kiosk that fits to the url: " + url);
    }

    public UrlIdHandler getUrlIdHandlerByType(String type) {
        return kioskList.get(type).handler;
    }
}
