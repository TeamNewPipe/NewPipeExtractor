package org.schabi.newpipe.extractor.kiosk;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KioskList {

    public interface KioskExtractorFactory {
        KioskExtractor createNewKiosk(final StreamingService streamingService,
                                      final String url,
                                      final String kioskId)
                throws ExtractionException, IOException;
    }

    private final StreamingService service;
    private final HashMap<String, KioskEntry> kioskList = new HashMap<>();
    private String defaultKiosk = null;

    @Nullable
    private Localization forcedLocalization;
    @Nullable
    private ContentCountry forcedContentCountry;

    private class KioskEntry {
        public KioskEntry(KioskExtractorFactory ef, ListLinkHandlerFactory h) {
            extractorFactory = ef;
            handlerFactory = h;
        }

        final KioskExtractorFactory extractorFactory;
        final ListLinkHandlerFactory handlerFactory;
    }

    public KioskList(StreamingService service) {
        this.service = service;
    }

    public void addKioskEntry(KioskExtractorFactory extractorFactory, ListLinkHandlerFactory handlerFactory, String id)
            throws Exception {
        if (kioskList.get(id) != null) {
            throw new Exception("Kiosk with type " + id + " already exists.");
        }
        kioskList.put(id, new KioskEntry(extractorFactory, handlerFactory));
    }

    public void setDefaultKiosk(String kioskType) {
        defaultKiosk = kioskType;
    }

    public KioskExtractor getDefaultKioskExtractor()
            throws ExtractionException, IOException {
        return getDefaultKioskExtractor(null);
    }

    public KioskExtractor getDefaultKioskExtractor(Page nextPage)
            throws ExtractionException, IOException {
        return getDefaultKioskExtractor(nextPage, NewPipe.getPreferredLocalization());
    }

    public KioskExtractor getDefaultKioskExtractor(Page nextPage, Localization localization)
            throws ExtractionException, IOException {
        if (defaultKiosk != null && !defaultKiosk.equals("")) {
            return getExtractorById(defaultKiosk, nextPage, localization);
        } else {
            if (!kioskList.isEmpty()) {
                // if not set get any entry
                Object[] keySet = kioskList.keySet().toArray();
                return getExtractorById(keySet[0].toString(), nextPage, localization);
            } else {
                return null;
            }
        }
    }

    public String getDefaultKioskId() {
        return defaultKiosk;
    }

    public KioskExtractor getExtractorById(String kioskId, Page nextPage)
            throws ExtractionException, IOException {
        return getExtractorById(kioskId, nextPage, NewPipe.getPreferredLocalization());
    }

    public KioskExtractor getExtractorById(String kioskId, Page nextPage, Localization localization)
            throws ExtractionException, IOException {
        KioskEntry ke = kioskList.get(kioskId);
        if (ke == null) {
            throw new ExtractionException("No kiosk found with the type: " + kioskId);
        } else {
            final KioskExtractor kioskExtractor = ke.extractorFactory.createNewKiosk(service,
                    ke.handlerFactory.fromId(kioskId).getUrl(), kioskId);

            if (forcedLocalization != null) kioskExtractor.forceLocalization(forcedLocalization);
            if (forcedContentCountry != null) kioskExtractor.forceContentCountry(forcedContentCountry);

            return kioskExtractor;
        }
    }

    public Set<String> getAvailableKiosks() {
        return kioskList.keySet();
    }

    public KioskExtractor getExtractorByUrl(String url, Page nextPage)
            throws ExtractionException, IOException {
        return getExtractorByUrl(url, nextPage, NewPipe.getPreferredLocalization());
    }

    public KioskExtractor getExtractorByUrl(String url, Page nextPage, Localization localization)
            throws ExtractionException, IOException {
        for (Map.Entry<String, KioskEntry> e : kioskList.entrySet()) {
            KioskEntry ke = e.getValue();
            if (ke.handlerFactory.acceptUrl(url)) {
                return getExtractorById(ke.handlerFactory.getId(url), nextPage, localization);
            }
        }
        throw new ExtractionException("Could not find a kiosk that fits to the url: " + url);
    }

    public ListLinkHandlerFactory getListLinkHandlerFactoryByType(String type) {
        return kioskList.get(type).handlerFactory;
    }

    public void forceLocalization(@Nullable Localization localization) {
        this.forcedLocalization = localization;
    }

    public void forceContentCountry(@Nullable ContentCountry contentCountry) {
        this.forcedContentCountry = contentCountry;
    }
}
