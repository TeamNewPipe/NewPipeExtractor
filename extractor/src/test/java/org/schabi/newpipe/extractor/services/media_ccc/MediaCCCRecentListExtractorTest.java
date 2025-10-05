package org.schabi.newpipe.extractor.services.media_ccc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreater;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

public class MediaCCCRecentListExtractorTest extends DefaultSimpleExtractorTest<KioskExtractor>
    implements InitNewPipeTest {

    @Override
    protected KioskExtractor createExtractor() throws Exception {
        return MediaCCC.getKioskList().getExtractorById("recent", null);
    }

    @Test
    void testStreamList() throws Exception {
        final List<StreamInfoItem> items = extractor().getInitialPage().getItems();
        assertFalse(items.isEmpty(), "No items returned");

        assertAll(items.stream().flatMap(this::getAllConditionsForItem));
    }

    private Stream<Executable> getAllConditionsForItem(final StreamInfoItem item) {
        return Stream.of(
                () -> assertFalse(
                        isNullOrEmpty(item.getName()),
                        "Name=[" + item.getName() + "] of " + item + " is empty or null"
                ),
                () -> assertGreater(Duration.ZERO,
                        item.getDurationObject(),
                        "Duration[=" + item.getDurationObject() + "] of " + item + " is <= 0"
                )
        );
    }
}
