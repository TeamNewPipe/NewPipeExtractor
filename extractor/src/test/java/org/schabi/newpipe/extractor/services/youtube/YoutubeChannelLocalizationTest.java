package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.fail;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that tests multiple channels and ranges of "time ago".
 */
public class YoutubeChannelLocalizationTest {
    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/channel/";
    private static final boolean DEBUG = false;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Test
    public void testAllSupportedLocalizations() throws Exception {
        YoutubeTestsUtils.ensureStateless();
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "localization"));

        testLocalizationsFor("https://www.youtube.com/user/NBCNews");
        testLocalizationsFor("https://www.youtube.com/channel/UCcmpeVbSSQlZRvHfdC-CRwg/videos");
        testLocalizationsFor("https://www.youtube.com/channel/UC65afEgL62PGFWXY7n6CUbA");
        testLocalizationsFor("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg");
    }

    private void testLocalizationsFor(final String channelUrl) throws Exception {

        final List<Localization> supportedLocalizations = YouTube.getSupportedLocalizations();
        // final List<Localization> supportedLocalizations = Arrays.asList(Localization.DEFAULT, new Localization("sr"));
        final Map<Localization, List<StreamInfoItem>> results = new LinkedHashMap<>();

        for (Localization currentLocalization : supportedLocalizations) {
            if (DEBUG) System.out.println("Testing localization = " + currentLocalization);

            ListExtractor.InfoItemsPage<StreamInfoItem> itemsPage;
            try {
                final ChannelExtractor extractor = YouTube.getChannelExtractor(channelUrl);
                extractor.forceLocalization(currentLocalization);
                extractor.fetchPage();
                itemsPage = defaultTestRelatedItems(extractor);
            } catch (final Throwable e) {
                System.out.println("[!] " + currentLocalization + " → failed");
                throw e;
            }

            final List<StreamInfoItem> items = itemsPage.getItems();
            for (int i = 0; i < items.size(); i++) {
                final StreamInfoItem item = items.get(i);

                String debugMessage = "[" + String.format("%02d", i) + "] "
                        + currentLocalization.getLocalizationCode() + " → " + item.getName()
                        + "\n:::: " + item.getStreamType() + ", views = " + item.getViewCount();
                final DateWrapper uploadDate = item.getUploadDate();
                if (uploadDate != null) {
                    String dateAsText = dateTimeFormatter.format(uploadDate.offsetDateTime());
                    debugMessage += "\n:::: " + item.getTextualUploadDate() +
                            "\n:::: " + dateAsText;
                }
                if (DEBUG) System.out.println(debugMessage + "\n");
            }
            results.put(currentLocalization, itemsPage.getItems());

            if (DEBUG) System.out.println("\n===============================\n");
        }


        // Check results
        final List<StreamInfoItem> referenceList = results.get(Localization.DEFAULT);
        boolean someFail = false;

        for (Map.Entry<Localization, List<StreamInfoItem>> currentResultEntry : results.entrySet()) {
            if (currentResultEntry.getKey().equals(Localization.DEFAULT)) {
                continue;
            }

            final String currentLocalizationCode = currentResultEntry.getKey().getLocalizationCode();
            final String referenceLocalizationCode = Localization.DEFAULT.getLocalizationCode();
            if (DEBUG) {
                System.out.println("Comparing " + referenceLocalizationCode + " with " +
                        currentLocalizationCode);
            }

            final List<StreamInfoItem> currentList = currentResultEntry.getValue();
            if (referenceList.size() != currentList.size()) {
                if (DEBUG) System.out.println("[!] " + currentLocalizationCode + " → Lists are not equal");
                someFail = true;
                continue;
            }

            for (int i = 0; i < referenceList.size() - 1; i++) {
                final StreamInfoItem referenceItem = referenceList.get(i);
                final StreamInfoItem currentItem = currentList.get(i);

                final DateWrapper referenceUploadDate = referenceItem.getUploadDate();
                final DateWrapper currentUploadDate = currentItem.getUploadDate();

                final String referenceDateString = referenceUploadDate == null ? "null" :
                        dateTimeFormatter.format(referenceUploadDate.offsetDateTime());
                final String currentDateString = currentUploadDate == null ? "null" :
                        dateTimeFormatter.format(currentUploadDate.offsetDateTime());

                long difference = -1;
                if (referenceUploadDate != null && currentUploadDate != null) {
                    difference = ChronoUnit.MILLIS.between(referenceUploadDate.offsetDateTime(), currentUploadDate.offsetDateTime());
                }

                final boolean areTimeEquals = difference < 5 * 60 * 1000L;

                if (!areTimeEquals) {
                    System.out.println("" +
                            "      [!] " + currentLocalizationCode + " → [" + i + "] dates are not equal\n" +
                            "          " + referenceLocalizationCode + ": " +
                            referenceDateString + " → " + referenceItem.getTextualUploadDate() +
                            "\n          " + currentLocalizationCode + ": " +
                            currentDateString + " → " + currentItem.getTextualUploadDate());
                }

            }
        }

        if (someFail) {
            fail("Some localization failed");
        } else {
            if (DEBUG) System.out.print("All tests passed" +
                    "\n\n===============================\n\n");
        }
    }
}
