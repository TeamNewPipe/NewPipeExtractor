package org.schabi.newpipe.extractor.services.media_ccc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;

public class MediaCCCLiveStreamListExtractorTest extends DefaultSimpleExtractorTest<KioskExtractor> {

    @Override
    protected KioskExtractor createExtractor() throws Exception {
        return MediaCCC.getKioskList().getExtractorById("live", null);
    }

    @Test
    public void getConferencesListTest() {
        assertDoesNotThrow(() -> extractor().getInitialPage().getItems());
    }

}
