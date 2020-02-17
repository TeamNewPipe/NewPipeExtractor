package org.schabi.newpipe.extractor.services.youtube;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.localization.AbbreviationHelper.abbreviationSubscribersCount;
import static org.schabi.newpipe.extractor.utils.Utils.cleanWhiteSpaces;
import static org.schabi.newpipe.extractor.utils.Utils.removeNumber;

/**
 * A class that tests abbreviations and subscriber counts for all the languages YouTube supports.
 */
@Ignore("Should be ran manually from time to time, as it's too time consuming.")
public class YoutubeSubcriberTest {

    private static final String url = "https://www.youtube.com/feed/guide_builder";
    private static final int PAUSE_DURATION_EXTRACTORS = 250;
    private static final int PAUSE_DURATION_ABBREVIATIONS = 125;

    public static String getAbbreviation(String count) {
        return removeNumber(cleanWhiteSpaces(count));
    }

    public static void assertEqualsWithEnglish(String channelUrl) throws ExtractionException, IOException, InterruptedException {
        NewPipe.init(DownloaderTestImpl.getInstance(), new Localization("en"));
        YoutubeChannelExtractor extractorEnglish = (YoutubeChannelExtractor) YouTube
                .getChannelExtractor(channelUrl);
        extractorEnglish.fetchPage();
        long englishSubCount = extractorEnglish.getSubscriberCount();
        Localization localization;
        for (int z = 0; z < YouTube.getSupportedLocalizations().size(); z++) {
            localization = YouTube.getSupportedLocalizations().get(z);
            System.out.println("Current localization: " + localization);
            NewPipe.init(DownloaderTestImpl.getInstance(), localization);
            YoutubeChannelExtractor extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor(channelUrl);
            extractor.fetchPage();

            long subcriberCount = extractor.getSubscriberCount();
            assertEquals("language that failed:" + localization.toString() + ".\nWe", englishSubCount, subcriberCount);
            Thread.sleep(PAUSE_DURATION_EXTRACTORS);
        }
    }

    public static void assertEqualsWithEnglish(String channelUrl, Localization loc) throws ExtractionException, IOException {
        //for only one language
        NewPipe.init(DownloaderTestImpl.getInstance(), new Localization("en"));
        YoutubeChannelExtractor extractorEnglish = (YoutubeChannelExtractor) YouTube
                .getChannelExtractor(channelUrl);
        extractorEnglish.fetchPage();
        long englishSubCount = extractorEnglish.getSubscriberCount();

        NewPipe.init(DownloaderTestImpl.getInstance(), loc);
        YoutubeChannelExtractor extractor = (YoutubeChannelExtractor) YouTube
                .getChannelExtractor(channelUrl);
        extractor.fetchPage();
        assertEquals(englishSubCount, extractor.getSubscriberCount());
    }

    public static void assertEqualsWithEnglish(String channelUrl, String languageCode) throws ExtractionException, IOException {
        assertEqualsWithEnglish(channelUrl, new Localization(languageCode));

    }

    public void runTest(Document doc, Localization localisation) throws ParsingException {
        String currentSubscriberCountString;
        String currentChannelName;

        Elements elements = doc.select(".yt-subscriber-count");
        for (int i = 0; i < elements.size(); i++) {
            currentSubscriberCountString = doc.select(".yt-subscriber-count").get(i).attr("title");
            currentChannelName = doc.select(".yt-ui-ellipsis.yt-ui-ellipsis-2.yt-uix-sessionlink").get(i).attr("title");
            String abbreviation = getAbbreviation(currentSubscriberCountString);
            try {
                abbreviation = abbreviation.replace(abbreviation, abbreviationSubscribersCount.get(abbreviation));
            } catch (NullPointerException e) {
                if (!abbreviation.isEmpty()) {
                    throw new ParsingException("This should be a real failed test. Abbreviation=\"" + abbreviation + "\"" +
                            "\nLocalization : " + localisation +
                            "\nOriginal string gathered from YouTube =\"" + currentSubscriberCountString + "\"" +
                            "\nTitle of the channel (probably wrong):" + currentChannelName);
                } else {
                    //see if it's not one of the languages giving a number directly
                    try { //special cases where it gives a number directly for some languages, or with a dot or a comma
                        String maybeAlreadyNumber = currentSubscriberCountString.replaceAll("([ ., ])", "");
                        long count = Long.parseLong(maybeAlreadyNumber);
                    } catch (NumberFormatException x) {
                        System.err.println("The abbreviation is empty, this is probably a failed request" +
                                "Localization :" + localisation);
                    }
                }
            }
        }
    }

    /*
    ========================
    TESTS FOR ABBREVIATIONS
    ========================
    */

    @Test
    public void testOneLanguageAbbreviations() throws IOException, ReCaptchaException, ParsingException, InterruptedException {
        Localization loc = new Localization("ms");
        //change the value of loc if you wanna test a specific language.

        NewPipe.init(DownloaderTestImpl.getInstance(), loc);
        Downloader dl = NewPipe.getDownloader();
        Response response = dl.get(url);
        Document doc = YoutubeParsingHelper.parseAndCheckPage(url, response);

        /*
        Uncomment this if you want to view the html file in your browser, search for the subscriber count given by the Exception.
        You'll get the real channel name (because the one given by Exception is often desynchronised).
        run with your browser, and you'll see how much subscribers the channel have
        then you can know the abbreviation given by the exception correspond to (eg a million, a thousand, 10 thousand…)
        add it in the AbbreviationHelper.java map.
         */
//        String pathToYTBTests = "src/test/java/org/schabi/newpipe/extractor/services/youtube/";
//        createFile(pathToYTBTests +"DELETEME_failTestYTBsubscriber" + loc.toString() + ".html", doc.toString());
        runTest(doc, loc);
    }

    @Test
    public void testAllLanguagesAbbreviations() throws IOException, ReCaptchaException, InterruptedException, ParsingException {
        List<Document> docs = new ArrayList<>();
        int totalCount = 0;
        Localization localization;

        for (int z = 0; z < YouTube.getSupportedLocalizations().size(); z++) {
            localization = YouTube.getSupportedLocalizations().get(z);
            System.out.println("Current localization: " + localization);
            NewPipe.init(DownloaderTestImpl.getInstance(), localization);
            Downloader dl = NewPipe.getDownloader();
            Response response = dl.get(url);
            Document doc = YoutubeParsingHelper.parseAndCheckPage(url, response);
            docs.add(doc);
            runTest(doc, localization);
            totalCount += doc.select(".yt-subscriber-count").size();
            Thread.sleep(PAUSE_DURATION_ABBREVIATIONS); //slowed down a bit to decrease reCAPTCHAs rate and false negatives
        }
        System.out.println("docs size: " + docs.size());
        System.out.println("total count (should be around 112*80=8960)" + totalCount);
    }

    /*
    ========================
    TESTS WITH THE EXTRACTOR
    There are often false positives (the test with all languages often fail, but if you try the failed language
    it will be ok. Increase PAUSE_DURATION_EXTRACTORS to prevent false positives.
    ========================
    */

    @Test
    public void testDisabled() throws IOException, ExtractionException, InterruptedException {
        //every languages should give -1
        Localization localization;
        for (int z = 0; z < YouTube.getSupportedLocalizations().size(); z++) {
            localization = YouTube.getSupportedLocalizations().get(z);
            System.out.println("Current localization: " + localization);
            NewPipe.init(DownloaderTestImpl.getInstance(), localization);
            YoutubeChannelExtractor extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("https://www.youtube.com/user/EminemVEVO/");
            extractor.fetchPage();

            long subcriberCount = extractor.getSubscriberCount();
            assertEquals("language that failed:" + localization.toString() + ".\nWe", -1, subcriberCount);
            Thread.sleep(PAUSE_DURATION_EXTRACTORS);
        }
    }

    @Test
    public void testOneLanguageExtractor() throws ExtractionException, IOException {
        assertEqualsWithEnglish("https://www.youtube.com/channel/UCPNxhDvTcytIdvwXWAm43cA", "ml");
    }

    @Test
    public void testHighestSubsOnYoutube() throws ExtractionException, IOException, InterruptedException {
        assertEqualsWithEnglish("https://www.youtube.com/user/tseries");
    }

    @Test
    public void testKurzgesagt() throws InterruptedException, ExtractionException, IOException {
        assertEqualsWithEnglish("https://www.youtube.com/user/Kurzgesagt");
    }

    @Test
    public void testSelenaGomez() throws InterruptedException, ExtractionException, IOException {
        assertEqualsWithEnglish("https://www.youtube.com/channel/UCPNxhDvTcytIdvwXWAm43cA");
    }
}
