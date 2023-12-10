package org.schabi.newpipe.extractor.kiosk;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public class KioskList {

    public interface KioskExtractorFactory {
        KioskExtractor createNewKiosk(StreamingService streamingService,
                                      String url,
                                      String kioskId)
                throws ExtractionException, IOException;
    }

    private final StreamingService service;
    private final HashMap<String, KioskEntry> kioskList = new HashMap<>();
    private String defaultKiosk = null;

    @Nullable
    private Localization forcedLocalization;
    @Nullable
    private ContentCountry forcedContentCountry;

    private static class KioskEntry {
        KioskEntry(final KioskExtractorFactory ef, final ListLinkHandlerFactory h) {
            extractorFactory = ef;
            handlerFactory = h;
        }

        final KioskExtractorFactory extractorFactory;
        final ListLinkHandlerFactory handlerFactory;
    }

    public KioskList(final StreamingService service) {
        this.service = service;
    }

    public void addKioskEntry(final KioskExtractorFactory extractorFactory,
                              final ListLinkHandlerFactory handlerFactory,
                              final String id)
            throws Exception {
        if (kioskList.get(id) != null) {
            throw new Exception("Kiosk with type " + id + " already exists.");
        }
        kioskList.put(id, new KioskEntry(extractorFactory, handlerFactory));
    }

    public void setDefaultKiosk(final String kioskType) {
        defaultKiosk = kioskType;
    }

    public KioskExtractor getDefaultKioskExtractor()
            throws ExtractionException, IOException {
        return getDefaultKioskExtractor(null);
    }

    public KioskExtractor getDefaultKioskExtractor(final Page nextPage)
            throws ExtractionException, IOException {
        return getDefaultKioskExtractor(nextPage, NewPipe.getPreferredLocalization());
    }

    public KioskExtractor getDefaultKioskExtractor(final Page nextPage,
                                                   final Localization localization)
            throws ExtractionException, IOException {
        if (!isNullOrEmpty(defaultKiosk)) {
            return getExtractorById(defaultKiosk, nextPage, localization);
        } else {
            final String first = kioskList.keySet().stream().findAny().orElse(null);
            if (first != null) {
                // if not set get any entry
                return getExtractorById(first, nextPage, localization);
            } else {
                return null;
            }
        }
    }

    public String getDefaultKioskId() {
        return defaultKiosk;
    }

    public KioskExtractor getExtractorById(final String kioskId, final Page nextPage)
            throws ExtractionException, IOException {
        return getExtractorById(kioskId, nextPage, NewPipe.getPreferredLocalization());
    }

    public KioskExtractor getExtractorById(final String kioskId,
                                           final Page nextPage,
                                           final Localization localization)
            throws ExtractionException, IOException {
        final KioskEntry ke = kioskList.get(kioskId);
        if (ke == null) {
            throw new ExtractionException("No kiosk found with the type: " + kioskId);
        } else {
            final KioskExtractor kioskExtractor = ke.extractorFactory.createNewKiosk(service,
                    ke.handlerFactory.fromId(kioskId).getUrl(), kioskId);

            if (forcedLocalization != null) {
                kioskExtractor.forceLocalization(forcedLocalization);
            }
            if (forcedContentCountry != null) {
                kioskExtractor.forceContentCountry(forcedContentCountry);
            }

            return kioskExtractor;
        }
    }

    public Set<String> getAvailableKiosks() {
        return kioskList.keySet();
    }

    public KioskExtractor getExtractorByUrl(final String url, final Page nextPage)
            throws ExtractionException, IOException {
        return getExtractorByUrl(url, nextPage, NewPipe.getPreferredLocalization());
    }

    public KioskExtractor getExtractorByUrl(final String url,
                                            final Page nextPage,
                                            final Localization localization)
            throws ExtractionException, IOException {
        for (final Map.Entry<String, KioskEntry> e : kioskList.entrySet()) {
            final KioskEntry ke = e.getValue();
            if (ke.handlerFactory.acceptUrl(url)) {
                return getExtractorById(ke.handlerFactory.getId(url), nextPage, localization);
            }
        }
        throw new ExtractionException("Could not find a kiosk that fits to the url: " + url);
    }

    public ListLinkHandlerFactory getListLinkHandlerFactoryByType(final String type) {
        return kioskList.get(type).handlerFactory;
    }

    public void forceLocalization(@Nullable final Localization localization) {
        this.forcedLocalization = localization;
    }

    public void forceContentCountry(@Nullable final ContentCountry contentCountry) {
        this.forcedContentCountry = contentCountry;
    }
}
