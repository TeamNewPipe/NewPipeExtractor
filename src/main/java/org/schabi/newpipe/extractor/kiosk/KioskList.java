package org.schabi.newpipe.extractor.kiosk;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOError;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public  class KioskList {
    public interface KioskExtractorFactory {
        KioskExtractor createNewKiosk(final StreamingService streamingService,
                                             final String url,
                                             final String nextStreamUrl)
            throws ExtractionException, IOException;
    }

    private int service_id;
    private HashMap<String, KioskEntry> kioskList = new HashMap<>();
    private String defaultKiosk = null;

    private class KioskEntry {
        public KioskEntry(KioskExtractorFactory ef, UrlIdHandler h) {
            extractorFactory = ef;
            handler = h;
        }
        KioskExtractorFactory extractorFactory;
        UrlIdHandler handler;
    }

    public KioskList(int service_id) {
        this.service_id = service_id;
    }

    public void addKioskEntry(KioskExtractorFactory extractorFactory, UrlIdHandler handler)
        throws Exception {
        KioskExtractor extractor =
                extractorFactory.createNewKiosk(NewPipe.getService(service_id), "", "");
        if(kioskList.get(extractor.getType()) != null) {
            throw new Exception("Kiosk with type " + extractor.getType() + " already exists.");
        }
        kioskList.put(extractor.getType(), new KioskEntry(extractorFactory, handler));
    }

    public void setDefaultKiosk(String kioskType) {
        defaultKiosk = kioskType;
    }

    public KioskExtractor getDefaultKioskExtractor(String nextStreamUrl)
            throws ExtractionException, IOException {
        if(defaultKiosk != null && !defaultKiosk.equals("")) {
            return getExtractorByType(defaultKiosk, nextStreamUrl);
        } else {
            if(!kioskList.isEmpty()) {
                // if not set get any entry
                Object[] keySet = kioskList.keySet().toArray();
                return getExtractorByType(keySet[0].toString(), nextStreamUrl);
            } else {
                return null;
            }
        }
    }

    public KioskExtractor getExtractorByType(String kioskType, String nextStreamsUrl)
            throws ExtractionException, IOException {
        KioskEntry ke = kioskList.get(kioskType);
        if(ke == null) {
            throw new ExtractionException("No kiosk found with the type: " + kioskType);
        } else {
            return ke.extractorFactory.createNewKiosk(NewPipe.getService(service_id),
                    ke.handler.getUrl(""),
                    nextStreamsUrl);
        }
    }

    public Set<String> getAvailableKisokTypes() {
        return kioskList.keySet();
    }

    public KioskExtractor getExtryctorByUrl(String url, String nextStreamsUrl)
            throws ExtractionException, IOException {
        for(Map.Entry<String, KioskEntry> e : kioskList.entrySet()) {
            KioskEntry ke = e.getValue();
            if(ke.handler.acceptUrl(url)) {
                return getExtractorByType(e.getKey(), nextStreamsUrl);
            }
        }
        throw new ExtractionException("Could not find a kiosk that fits to the url: " + url);
    }
}
