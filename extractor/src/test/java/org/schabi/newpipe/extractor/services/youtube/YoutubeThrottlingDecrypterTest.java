package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mozilla.javascript.EvaluatorException;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;

class YoutubeThrottlingDecrypterTest {

    @BeforeEach
    public void setup() throws IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    void testExtractFunction__success() throws ParsingException {
        final String[] videoIds = {"jE1USQrs1rw", "CqxjzfudGAc", "goH-9MfQI7w", "KYIdr_7H5Yw", "J1WeqmGbYeI"};

        final String encryptedUrl = "https://r6---sn-4g5ednek.googlevideo.com/videoplayback?expire=1626562120&ei=6AnzYO_YBpql1gLGkb_IBQ&ip=127.0.0.1&id=o-ANhBEf36Z5h-8U9DDddtPDqtS0ZNwf0XJAAigudKI2uI&itag=278&aitags=133%2C134%2C135%2C136%2C137%2C160%2C242%2C243%2C244%2C247%2C248%2C278&source=youtube&requiressl=yes&vprv=1&mime=video%2Fwebm&ns=TvecOReN0vPuXb3j_zq157IG&gir=yes&clen=2915100&dur=270.203&lmt=1608157174907785&keepalive=yes&fexp=24001373,24007246&c=WEB&txp=5535432&n=N9BWSTFT7vvBJrvQ&sparams=expire%2Cei%2Cip%2Cid%2Caitags%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&alr=yes&sig=AOq0QJ8wRQIgW6XnUDKPDSxiT0_KE_tDDMpcaCJl2Un5p0Fu9qZNQGkCIQDWxsDHi_s2BEmRqIbd1C5g_gzfihB7RZLsScKWNMwzzA%3D%3D&cpn=9r2yt3BqcYmeb2Yu&cver=2.20210716.00.00&redirect_counter=1&cm2rm=sn-4g5ezy7s&cms_redirect=yes&mh=Y5&mm=34&mn=sn-4g5ednek&ms=ltu&mt=1626540524&mv=m&mvi=6&pl=43&lsparams=mh,mm,mn,ms,mv,mvi,pl&lsig=AG3C_xAwRQIhAIUzxTn9Vw1-vm-_7OQ5-0h1M6AZsY9Bx1FlCCTeMICzAiADtGggbn4Znsrh2EnvyOsGnYdRGcbxn4mW9JMOQiInDQ%3D%3D&range=259165-480735&rn=11&rbuf=20190";

        for (final String videoId : videoIds) {
            try {
                final String decryptedUrl = YoutubeThrottlingDecrypter.apply(encryptedUrl, videoId);
                assertNotEquals(encryptedUrl, decryptedUrl);
            } catch (final EvaluatorException e) {
                fail("Failed to extract n param decrypt function for video " + videoId + "\n" + e);
            }
        }
    }

    @Test
    void testDecode__success() throws ParsingException {
        // URL extracted from browser with the dev tools
        final String encryptedUrl = "https://r6---sn-4g5ednek.googlevideo.com/videoplayback?expire=1626562120&ei=6AnzYO_YBpql1gLGkb_IBQ&ip=127.0.0.1&id=o-ANhBEf36Z5h-8U9DDddtPDqtS0ZNwf0XJAAigudKI2uI&itag=278&aitags=133%2C134%2C135%2C136%2C137%2C160%2C242%2C243%2C244%2C247%2C248%2C278&source=youtube&requiressl=yes&vprv=1&mime=video%2Fwebm&ns=TvecOReN0vPuXb3j_zq157IG&gir=yes&clen=2915100&dur=270.203&lmt=1608157174907785&keepalive=yes&fexp=24001373,24007246&c=WEB&txp=5535432&n=N9BWSTFT7vvBJrvQ&sparams=expire%2Cei%2Cip%2Cid%2Caitags%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&alr=yes&sig=AOq0QJ8wRQIgW6XnUDKPDSxiT0_KE_tDDMpcaCJl2Un5p0Fu9qZNQGkCIQDWxsDHi_s2BEmRqIbd1C5g_gzfihB7RZLsScKWNMwzzA%3D%3D&cpn=9r2yt3BqcYmeb2Yu&cver=2.20210716.00.00&redirect_counter=1&cm2rm=sn-4g5ezy7s&cms_redirect=yes&mh=Y5&mm=34&mn=sn-4g5ednek&ms=ltu&mt=1626540524&mv=m&mvi=6&pl=43&lsparams=mh,mm,mn,ms,mv,mvi,pl&lsig=AG3C_xAwRQIhAIUzxTn9Vw1-vm-_7OQ5-0h1M6AZsY9Bx1FlCCTeMICzAiADtGggbn4Znsrh2EnvyOsGnYdRGcbxn4mW9JMOQiInDQ%3D%3D&range=259165-480735&rn=11&rbuf=20190";
        final String decryptedUrl = YoutubeThrottlingDecrypter.apply(encryptedUrl, "jE1USQrs1rw");
        // The cipher function changes over time, so we just check if the n param changed.
        assertNotEquals(encryptedUrl, decryptedUrl);
    }

    @Test
    void testDecode__noNParam__success() throws ParsingException {
        final String noNParamUrl = "https://r5---sn-4g5ednsz.googlevideo.com/videoplayback?expire=1626553257&ei=SefyYPmIFoKT1wLtqbjgCQ&ip=127.0.0.1&id=o-AIT5xGifsaEAdEOAb5vd06J9VNtm-KHHolnaZRGPjHZi&itag=140&source=youtube&requiressl=yes&mh=xO&mm=31%2C29&mn=sn-4g5ednsz%2Csn-4g5e6nsr&ms=au%2Crdu&mv=m&mvi=5&pl=24&initcwndbps=1322500&vprv=1&mime=audio%2Fmp4&ns=cA2SS5atEe0mH8tMwGTO4RIG&gir=yes&clen=3009275&dur=185.898&lmt=1626356984653961&mt=1626531173&fvip=5&keepalive=yes&fexp=24001373%2C24007246&beids=23886212&c=WEB&txp=6411222&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRgIhAPueRlTutSlzPafxrqBmgZz5m7-Zfbw3QweDp3j4XO9SAiEA5tF7_ZCJFKmS-D6I1jlUURjpjoiTbsYyKuarV4u6E8Y%3D&sig=AOq0QJ8wRQIgRD_4WwkPeTEKGVSQqPsznMJGqq4nVJ8o1ChGBCgi4Y0CIQCZT3tI40YLKBWJCh2Q7AlvuUIpN0ficzdSElLeQpJdrw==";
        final String decrypted = YoutubeThrottlingDecrypter.apply(noNParamUrl, "jE1USQrs1rw");

        assertEquals(noNParamUrl, decrypted);
    }

}