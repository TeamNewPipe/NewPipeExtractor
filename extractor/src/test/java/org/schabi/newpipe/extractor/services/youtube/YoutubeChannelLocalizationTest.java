package org.schabi.newpipe.extractor.services.youtube;

import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.fail;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestRelatedItems;

/**
 * A class that tests multiple channels and ranges of "time ago".
 */
@Ignore("Should be ran manually from time to time, as it's too time consuming.")
public class YoutubeChannelLocalizationTest {
    private static final boolean DEBUG = true;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Test
    public void testAllSupportedLocalizations() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());

        testLocalizationsFor("https://www.youtube.com/user/NBCNews");
        testLocalizationsFor("https://www.youtube.com/channel/UCcmpeVbSSQlZRvHfdC-CRwg/videos");
        testLocalizationsFor("https://www.youtube.com/channel/UC65afEgL62PGFWXY7n6CUbA");
        testLocalizationsFor("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg");
    }

    private void testLocalizationsFor(String channelUrl) throws Exception {

        final List<Localization> supportedLocalizations = YouTube.getSupportedLocalizations();
//        final List<Localization> supportedLocalizations = Arrays.asList(Localization.DEFAULT, new Localization("sr"));
        final Map<Localization, List<StreamInfoItem>> results = new LinkedHashMap<>();

        for (Localization currentLocalization : supportedLocalizations) {
            if (DEBUG) System.out.println("Testing localization = " + currentLocalization);

            ListExtractor.InfoItemsPage<StreamInfoItem> itemsPage;
            try {
                final ChannelExtractor extractor = YouTube.getChannelExtractor(channelUrl);
                extractor.forceLocalization(currentLocalization);
                extractor.fetchPage();
                itemsPage = defaultTestRelatedItems(extractor);
            } catch (Throwable e) {
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
                    String dateAsText = dateFormat.format(uploadDate.date().getTime());
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
                        dateFormat.format(referenceUploadDate.date().getTime());
                final String currentDateString = currentUploadDate == null ? "null" :
                        dateFormat.format(currentUploadDate.date().getTime());

                long difference = -1;
                if (referenceUploadDate != null && currentUploadDate != null) {
                    difference = Math.abs(referenceUploadDate.date().getTimeInMillis() - currentUploadDate.date().getTimeInMillis());
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
