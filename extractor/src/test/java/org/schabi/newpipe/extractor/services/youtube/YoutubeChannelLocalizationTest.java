package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A class that tests multiple channels and ranges of "time ago".
 */
@Disabled("There is currently only one localization supported for YT")
class YoutubeChannelLocalizationTest {
    private static final String RESOURCE_PATH =
            DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/channel/";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> CHANNEL_URLS = Arrays.asList(
            "https://www.youtube.com/user/NBCNews",
            "https://www.youtube.com/channel/UCcmpeVbSSQlZRvHfdC-CRwg/videos",
            "https://www.youtube.com/channel/UC65afEgL62PGFWXY7n6CUbA",
            "https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg");

    private static final Map<String, List<StreamInfoItem>> REFERENCES = new HashMap<>();


    @BeforeAll
    static void setUp() throws Exception {
        YoutubeTestsUtils.ensureStateless();
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "localization"));

        for (final String url : CHANNEL_URLS) {
            REFERENCES.put(url, getItemsPage(url, Localization.DEFAULT));
        }
    }

    static Stream<Arguments> provideDataForSupportedLocalizations() {
        final List<Localization> localizations =
                new ArrayList<>(YouTube.getSupportedLocalizations());
        // Will already be checked in the references
        localizations.remove(Localization.DEFAULT);

        return CHANNEL_URLS.stream()
                .flatMap(url -> localizations.stream().map(l -> Arguments.of(url, l)));
    }

    @ParameterizedTest
    @MethodSource("provideDataForSupportedLocalizations")
    void testSupportedLocalizations(
            final String channelUrl,
            final Localization localization
    ) throws Exception {
        final List<StreamInfoItem> currentItems = getItemsPage(channelUrl, localization);

        final List<StreamInfoItem> refItems = REFERENCES.get(channelUrl);

        assertAll(
                Stream.concat(
                        // Check if the lists match
                        Stream.of(() -> assertEquals(
                                refItems.size(),
                                currentItems.size(),
                                "Number of returned items doesn't match reference list")),
                        // Check all items
                        refItems.stream()
                                .map(refItem -> {
                                    final StreamInfoItem curItem =
                                            currentItems.get(refItems.indexOf(refItem));
                                    return checkItemAgainstReference(refItem, curItem);
                                })
                )
        );
    }

    private Executable checkItemAgainstReference(
            final StreamInfoItem refItem,
            final StreamInfoItem curItem
    ) {
        final DateWrapper refUploadDate = refItem.getUploadDate();
        final DateWrapper curUploadDate = curItem.getUploadDate();

        final long difference =
                refUploadDate == null || curUploadDate == null
                        ? -1
                        : ChronoUnit.MINUTES.between(
                        refUploadDate.offsetDateTime(),
                        curUploadDate.offsetDateTime());
        return () -> assertTrue(
                difference < 5,
                () -> {
                    final String refDateStr = refUploadDate == null
                            ? "null"
                            : DTF.format(refUploadDate.offsetDateTime());
                    final String curDateStr = curUploadDate == null
                            ? "null"
                            : DTF.format(curUploadDate.offsetDateTime());

                    return "Difference between reference '" + refDateStr
                            + "' and current '" + curDateStr + "' is too great";
                });
    }

    private static List<StreamInfoItem> getItemsPage(
            final String channelUrl,
            final Localization localization) throws Exception {
        final ChannelExtractor extractor = YouTube.getChannelExtractor(channelUrl);
        extractor.forceLocalization(localization);
        extractor.fetchPage();
        return defaultTestRelatedItems(extractor).getItems();
    }
}
