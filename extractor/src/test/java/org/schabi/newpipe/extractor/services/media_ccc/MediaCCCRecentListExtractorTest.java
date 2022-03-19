package org.schabi.newpipe.extractor.services.media_ccc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreater;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;
import java.util.stream.Stream;

public class MediaCCCRecentListExtractorTest {
    private static KioskExtractor extractor;

    @BeforeAll
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = MediaCCC.getKioskList().getExtractorById("recent", null);
        extractor.fetchPage();
    }

    @Test
    void testStreamList() throws Exception {
        final List<StreamInfoItem> items = extractor.getInitialPage().getItems();
        assertFalse(items.isEmpty(), "No items returned");

        assertAll(items.stream().flatMap(this::getAllConditionsForItem));
    }

    private Stream<Executable> getAllConditionsForItem(final StreamInfoItem item) {
        return Stream.of(
                () -> assertFalse(
                        isNullOrEmpty(item.getName()),
                        "Name=[" + item.getName() + "] of " + item + " is empty or null"
                ),
                () -> assertGreater(0,
                        item.getDuration(),
                        "Duration[=" + item.getDuration() + "] of " + item + " is <= 0"
                )
        );
    }
}
