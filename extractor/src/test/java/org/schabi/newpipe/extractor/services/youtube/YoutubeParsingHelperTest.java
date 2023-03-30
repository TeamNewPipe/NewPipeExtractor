package org.schabi.newpipe.extractor.services.youtube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.AudioTrackType;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class YoutubeParsingHelperTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/";

    @BeforeAll
    public static void setUp() throws IOException {
        YoutubeTestsUtils.ensureStateless();
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "youtubeParsingHelper"));
    }

    @Test
    void testAreHardcodedClientVersionAndKeyValid() throws IOException, ExtractionException {
        assertTrue(YoutubeParsingHelper.areHardcodedClientVersionAndKeyValid(),
                "Hardcoded client version and key are not valid anymore");
    }

    @Test
    void testAreHardcodedYoutubeMusicKeysValid() throws IOException, ExtractionException {
        assertTrue(YoutubeParsingHelper.isHardcodedYoutubeMusicKeyValid(),
                "Hardcoded YouTube Music keys are not valid anymore");
    }

    @Test
    void testParseDurationString() throws ParsingException {
        assertEquals(1162567, YoutubeParsingHelper.parseDurationString("12:34:56:07"));
        assertEquals(4445767, YoutubeParsingHelper.parseDurationString("1,234:56:07"));
        assertEquals(754, YoutubeParsingHelper.parseDurationString("12:34 "));
    }

    @Test
    void testConvertFromGoogleCacheUrl() {
        assertEquals("https://mohfw.gov.in/",
                YoutubeParsingHelper.extractCachedUrlIfNeeded("https://webcache.googleusercontent.com/search?q=cache:https://mohfw.gov.in/"));
        assertEquals("https://www.infektionsschutz.de/coronavirus-sars-cov-2.html",
                YoutubeParsingHelper.extractCachedUrlIfNeeded("https://www.infektionsschutz.de/coronavirus-sars-cov-2.html"));
    }

    @Test
    void extractAudioTrackType() {
        final String originalUrl = "https://rr2---sn-4g5lzned.googlevideo.com/videoplayback?expire=1679429648&ei=sLsZZKrICIuR1gLSnYbgAg&ip=127.0.0.1&id=o-ALWn2ZwDxUXEZKzlsT_X9iuDjRMSi__SgRXVrVjKZEhc&itag=251&source=youtube&requiressl=yes&mh=nU&mm=31%2C29&mn=sn-4g5lzned%2Csn-4g5edndz&ms=au%2Crdu&mv=m&mvi=2&pl=40&initcwndbps=1740000&spc=H3gIhgXQzBxvKu2MOEmFaaEenC4DKdVUwudTeu3dtKwmq-Xv5g&vprv=1&xtags=acont%3Doriginal%3Alang%3Den&mime=audio%2Fwebm&ns=-lg0OQZL1LZRQO-dzE0W4E4L&gir=yes&clen=3513412&dur=303.681&lmt=1679342942566207&mt=1679407764&fvip=1&keepalive=yes&fexp=24007246&c=WEB&txp=5532434&n=gDLP5pImH9Vr7v&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Cxtags%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRAIgPFQ1yX8aoc35sz2eV2-wzNIhTQeOHGCsOmIonmo776kCIFo5k6HZ5kAQ6DycRCAG8jJgk9jNyncILGPrGZMZUuuo&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRQIhANODPaBuc32MWI9gF3Bn1iz3byEn7EwUiXpNLuCcQqW9AiBB88Qrrz2fJCzYKg14_nnGxGQH1Uoi7i31OSrHK6_dGw%3D%3D";
        final String dubbedUrl = "https://rr2---sn-4g5lzned.googlevideo.com/videoplayback?expire=1679429648&ei=sLsZZKrICIuR1gLSnYbgAg&ip=127.0.0.1&id=o-ALWn2ZwDxUXEZKzlsT_X9iuDjRMSi__SgRXVrVjKZEhc&itag=251&source=youtube&requiressl=yes&mh=nU&mm=31%2C29&mn=sn-4g5lzned%2Csn-4g5edndz&ms=au%2Crdu&mv=m&mvi=2&pl=40&initcwndbps=1740000&spc=H3gIhgXQzBxvKu2MOEmFaaEenC4DKdVUwudTeu3dtKwmq-Xv5g&vprv=1&xtags=acont%3Ddubbed%3Alang%3Den&mime=audio%2Fwebm&ns=-lg0OQZL1LZRQO-dzE0W4E4L&gir=yes&clen=3884070&dur=303.721&lmt=1679342946044954&mt=1679407764&fvip=1&keepalive=yes&fexp=24007246&c=WEB&txp=5532434&n=gDLP5pImH9Vr7v&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Cxtags%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRQIhAKEMLB8yLZJf2jXAu4P1Q8AVEciYsmjjr2syYAWZfJg6AiAfu-XI11zYpCLqljw_MCegh26pJHYyfatgfFGWfpL-6Q%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRQIhANODPaBuc32MWI9gF3Bn1iz3byEn7EwUiXpNLuCcQqW9AiBB88Qrrz2fJCzYKg14_nnGxGQH1Uoi7i31OSrHK6_dGw%3D%3D";
        final String descriptiveUrl = "https://rr2---sn-4g5lzned.googlevideo.com/videoplayback?expire=1679429648&ei=sLsZZKrICIuR1gLSnYbgAg&ip=127.0.0.1&id=o-ALWn2ZwDxUXEZKzlsT_X9iuDjRMSi__SgRXVrVjKZEhc&itag=251&source=youtube&requiressl=yes&mh=nU&mm=31%2C29&mn=sn-4g5lzned%2Csn-4g5edndz&ms=au%2Crdu&mv=m&mvi=2&pl=40&initcwndbps=1740000&spc=H3gIhgXQzBxvKu2MOEmFaaEenC4DKdVUwudTeu3dtKwmq-Xv5g&vprv=1&xtags=acont%3Ddescriptive%3Alang%3Den&mime=audio%2Fwebm&ns=-lg0OQZL1LZRQO-dzE0W4E4L&gir=yes&clen=4061711&dur=303.721&lmt=1679342946800120&mt=1679407764&fvip=1&keepalive=yes&fexp=24007246&c=WEB&txp=5532434&n=gDLP5pImH9Vr7v&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Cxtags%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRgIhAKFUzoNscV1hbNcPwcnQO3vOy47q69szj7BdLhFYS52pAiEA2oPhLZIZsrUQrx62iH4dHvTBlCloC3NieJw6edo7LL8%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRQIhANODPaBuc32MWI9gF3Bn1iz3byEn7EwUiXpNLuCcQqW9AiBB88Qrrz2fJCzYKg14_nnGxGQH1Uoi7i31OSrHK6_dGw%3D%3D";
        final String noTrackUrl = "https://rr2---sn-4g5ednz7.googlevideo.com/videoplayback?expire=1679430240&ei=AL4ZZKiXJefYx_APj_6ECA&ip=127.0.0.1&id=o-ALKVh9uHVEvurL3bZOZCEMzFod9ZmJJd6GszA6UEIuKy&itag=251&source=youtube&requiressl=yes&mh=8L&mm=31%2C26&mn=sn-4g5ednz7%2Csn-i5heen7z&ms=au%2Conr&mv=m&mvi=2&pl=40&initcwndbps=1793750&spc=H3gIhh2s06nxQJg3zEgY9pw84syUasRiagYDsQ5UHHfcu5bfTA&vprv=1&mime=audio%2Fwebm&ns=VumObYcnTZNicexX7Ek2WakL&gir=yes&clen=3711099&dur=299.201&lmt=1679334484198077&mt=1679408487&fvip=2&keepalive=yes&fexp=24007246&c=WEB&txp=3318224&n=10c-m6ZvG6C7rC&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRQIhAODS0aHRBgdrHm5qwquqGC6zq3rU81W59y4BtV0Y9KStAiAPT8ykXXj_7GzAyZbLPgYKs-B1HWT-4bY0CppmZ2rReg%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRQIhAL8fS6T-V9BNqrx55mdMvve5be2gcjIY8pYfxlUMPY6pAiAgiCMbqR4eSS_HvLu9KBe6cCFZeMcSTc7vzWtL9y0xvw%3D%3D";

        assertEquals(AudioTrackType.ORIGINAL, YoutubeParsingHelper.extractAudioTrackType(originalUrl));
        assertEquals(AudioTrackType.DUBBED, YoutubeParsingHelper.extractAudioTrackType(dubbedUrl));
        assertEquals(AudioTrackType.DESCRIPTIVE, YoutubeParsingHelper.extractAudioTrackType(descriptiveUrl));
        assertNull(YoutubeParsingHelper.extractAudioTrackType(noTrackUrl));
    }
}
