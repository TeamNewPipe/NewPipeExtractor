package org.schabi.newpipe.extractor.services.youtube;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.utils.Utils.removeNumber;

/**
 * Test for {@link YoutubeChannelExtractor}
 * and specifically YoutubeChannelExtractor.getSubscriberCount()
 * in all the languages supported by YouTube.
 * Takes a long time because we need to test make 146 requests to YouTube
 * DON'T RUN ON MOBILE DATA
 * <p>
 * pattern for functions name:
 * testlangcodeRegionabbreviation (Region is optional)
 * eg:
 * testenk = english thousand
 * testfrCam = French (Canada) million
 * testzhTwk = Chinese (Taiwan) thousand
 */

/*
Ignoring the test because otherwise it will slow down too much the CI test.
And also, often one up to three tests fail if you launch the whole tests, because some requests fail
(it could be reCAPTCHAs) but they, as of today (2020-02-16) success if you run each one.

To run the test (to investigate maybe future problems),
You should temporarily create « public Document getDoc() { return this.doc;} » method in YoutubeChannelExtractor.
 */

@Ignore
public class YoutubeSubscriberTest {

    private static final String channelThousand = "https://www.youtube.com/channel/UC_Fh8kvtkVPkeihBs42jGcA";
    private static final String channelMillion = "https://www.youtube.com/channel/UC-J-KZfRV8c13fOCkhXdLiQ";
    private static long countMillion;
    private static long countThousand;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance(), Localization.DEFAULT);

        YoutubeChannelExtractor extractorMillion = (YoutubeChannelExtractor) YouTube
                .getChannelExtractor(channelMillion);
        extractorMillion.fetchPage();
        countMillion = extractorMillion.getSubscriberCount();

        YoutubeChannelExtractor extractorThousand = (YoutubeChannelExtractor) YouTube
                .getChannelExtractor(channelThousand);
        extractorThousand.fetchPage();
        countThousand = extractorThousand.getSubscriberCount();
    }

    public static String getSubscriberCount(YoutubeChannelExtractor extractor) {
        //fetches and return number abbreviation
        //eg 26,8 k
        Document doc = extractor.getDoc();
        Element el = doc.select("span[class*=\"yt-subscription-button-subscriber-count\"]").first();
        return el.attr("title");
    }

    public static String getAbbreviation(String count) {
        count = count.replaceAll("(\\s| | )", "");
        return removeNumber(count);
    }

    public static String getAbbreviation(YoutubeChannelExtractor extractor) {
        return getAbbreviation(getSubscriberCount(extractor));
    }

    public YoutubeChannelExtractor getExtractor(Localization loc, String channelUrl) throws ExtractionException, IOException {
        NewPipe.init(DownloaderTestImpl.getInstance(), loc);
        YoutubeChannelExtractor extractor = (YoutubeChannelExtractor) YouTube
                .getChannelExtractor(channelUrl);
        extractor.fetchPage();
        return extractor;
    }

    public YoutubeChannelExtractor getExtractor(String type, Localization loc) throws ExtractionException, IOException {
        if (type.equals("k")) {
            return getExtractorThousand(loc);
        } else if (type.equals("m")) {
            return getExtractorMillion(loc);
        }
        return null;
    }

    public YoutubeChannelExtractor getExtractorMillion(Localization loc) throws ExtractionException, IOException {
        return getExtractor(loc, channelMillion);
    }

    public YoutubeChannelExtractor getExtractorThousand(Localization loc) throws ExtractionException, IOException {
        return getExtractor(loc, channelThousand);
    }

    public void ut(YoutubeChannelExtractor extractor) {
        String subscriberCount = getSubscriberCount(extractor);
        System.out.println(extractor.getExtractorLocalization() + ": " + subscriberCount);
        System.out.println(getAbbreviation(subscriberCount));
//        System.out.println("abbreviation =\"" + getAbbreviation(getSubscriberCount(extractor)) + "\"");
    }

    public void buildthousand(Localization loc) throws IOException, ExtractionException {
        String languageCode = loc.getLanguageCode();
        System.out.println();
        YoutubeChannelExtractor current = getExtractor("k", loc);
        ut(current);
        String abr = getAbbreviation(getSubscriberCount(current));
        System.out.println("        abbreviationSubscribersCount.put(\"" + abr + "\", englishThousandAbbreviation); //" +
                languageCode);
        String s = "    @Test\n" +
                "    public void test" + languageCode + "k() throws IOException, ExtractionException {\n" +
                "        YoutubeChannelExtractor extractor = getExtractor(\"k\", new Localization(\"" + languageCode +
                "\"));\n" + "        ut(extractor);\n" +
                "        assertEquals(countThousand, extractor.getSubscriberCount());\n" +
                "    }";
        System.out.println(s + "\n");
    }

    public void buildmillion(Localization loc) throws IOException, ExtractionException {
        String languageCode = loc.getLanguageCode();
        String s = "    @Test\n" +
                "    public void test" + languageCode + "m() throws IOException, ExtractionException {\n" +
                "        YoutubeChannelExtractor extractor = getExtractor(\"m\", new Localization(\"" + languageCode +
                "\"));\n" + "        ut(extractor);\n" +
                "        assertEquals(countMillion, extractor.getSubscriberCount());\n" +
                "    }";
        System.out.println(s);
        System.out.println();
        YoutubeChannelExtractor current = getExtractor("m", loc);
        ut(current);
        String abr = getAbbreviation(getSubscriberCount(current));
        System.out.println("        abbreviationSubscribersCount.put(\"" + abr + "\", englishMillionAbbreviation); //" +
                languageCode);
    }

    @Test
    public void testafk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("af"));
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testafm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("af"));
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testamk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("am"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testamm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("am"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testark() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("ar"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testarm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("ar"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testazk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("az"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testazm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("az"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testbek() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("be"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testbem() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("be"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testbgk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("bg"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testbgm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("bg"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testbnk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("bn"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testbnm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("bn"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testbsk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("bs"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testbsm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("bs"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testcak() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("ca"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testcam() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("ca"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testcsk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("cs"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testcsm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("cs"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testdak() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("da"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testdam() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("da"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testdek() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("de"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testdem() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("de"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testelk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("el"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testelm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("el"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testes419k() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("es", "419"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testesUSk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("es", "US"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testesUSm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("es", "US"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testes419m() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("es", "419"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }


    @Test
    public void testesk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("es"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testesm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("es"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testetk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("et"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testetm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("et"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testeuk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("eu"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testeum() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("eu"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testfak() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("fa"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testfam() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("fa"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testfik() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("fi"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testfim() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("fi"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testfrk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("fr"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testfrm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("fr"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testfrCak() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("fr", "CA"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testfrCam() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("fr", "CA"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testglk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("gl"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testglm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("gl"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testguk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("gu"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testgum() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("gu"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testhik() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("hi"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testhim() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("hi"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testhrk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("hr"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testhrm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("hr"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testhuk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("hu"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testhum() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("hu"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testhyk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("hy"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testhym() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("hy"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testidk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("id"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testidm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("id"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testisk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("is"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testism() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("is"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testitk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("it"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testitm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("it"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testiwk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("iw"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testiwm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("iw"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testjak() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("ja"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testjam() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("ja"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testkak() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("ka"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testkam() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("ka"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testkmk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("km"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testkmm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("km"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testknk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("kn"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testknm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("kn"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testkok() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("ko"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testkom() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("ko"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testkyk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("ky"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testkym() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("ky"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testlok() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("lo"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testlom() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("lo"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testltk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("lt"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testltm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("lt"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testlvk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("lv"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testlvm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("lv"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testmkk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("mk"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testmkm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("mk"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testmnk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("mn"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testmnm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("mn"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testmrk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("mr"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testmrm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("mr"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testmyk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("my"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testmym() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("my"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testnek() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("ne"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testnem() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("ne"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testnlk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("nl"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testnlm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("nl"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testnok() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("no"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testnom() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("no"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testpak() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("pa"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testpam() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("pa"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testplk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("pl"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testplm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("pl"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testptk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("pt"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testptm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("pt"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testrok() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("ro"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testrom() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("ro"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testruk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("ru"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testrum() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("ru"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testsik() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("si"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testsim() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("si"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testskk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("sk"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testskm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("sk"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testslk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("sl"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testslm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("sl"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testsqk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("sq"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testsqm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("sq"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testsrk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("sr"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testsrm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("sr"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testsrLatnk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("sr", "Latn"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testsrLatnm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("sr", "Latn"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testsvk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("sv"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testsvm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("sv"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testswk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("sw"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testswm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("sw"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testtak() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("ta"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testtam() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("ta"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testtek() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("te"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testtem() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("te"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testthk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("th"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testthm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("th"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testtrk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("tr"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testtrm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("tr"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testukk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("uk"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testukm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("uk"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testurk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("ur"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testurm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("ur"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testuzk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("uz"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testuzm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("uz"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testvik() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("vi"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testvim() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("vi"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testzhCnk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("zh", "CN"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testzhCnm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("zh", "CN"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testzhHkk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("zh", "HK"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testzhHkm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("zh", "HK"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testzhTwk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("zh", "TW"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testzhTwm() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("zh", "TW"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Test
    public void testzuk() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("k", new Localization("zu"));
        ut(extractor);
        assertEquals(countThousand, extractor.getSubscriberCount());
    }

    @Test
    public void testzum() throws IOException, ExtractionException {
        YoutubeChannelExtractor extractor = getExtractor("m", new Localization("zu"));
        ut(extractor);
        assertEquals(countMillion, extractor.getSubscriberCount());
    }

    @Ignore
    @Test
    public void build() throws IOException, ExtractionException, InterruptedException {
        Localization current = YouTube.getSupportedLocalizations().get(79);
        buildthousand(current);
        buildmillion(current);
        System.out.println();
        Thread.sleep(500);
        System.out.println();
    }
}
