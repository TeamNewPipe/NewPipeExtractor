package org.schabi.newpipe.extractor.services.youtube;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOException;

public class YoutubeDashManifestCreatorTest {
    @Before
    public void setup() throws IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Ignore
    @Test
    public void testOtfStream() throws ExtractionException,
            YoutubeDashManifestCreator.YoutubeDashManifestCreationException {
        final String otfUrl = "";
        final ItagItem itagItem1 = ItagItem.getItag(247);
        final String mimeType = "video/webm; codecs=\"vp9\"";
        itagItem1.setCodec(mimeType.split("\"")[1]);
        itagItem1.setBitrate(1505280);
        itagItem1.setQuality("hd720");
        itagItem1.fps = 25;
        itagItem1.setWidth(1280);
        itagItem1.setHeight(720);
        System.out.println(YoutubeDashManifestCreator.createDashManifestFromOtfStreamingUrl(otfUrl, itagItem1));

        final String postLiveDvrUrl = "";
        final ItagItem itagItem2 = ItagItem.getItag(137);
        final String mimeType2 = "video/mp4; codecs=\"avc1.640028\"";
        itagItem2.setCodec(mimeType2.split("\"")[1]);
        itagItem2.setBitrate(5018593);
        itagItem2.setQuality("hd1080");
        itagItem2.fps = 30;
        itagItem2.setWidth(1920);
        itagItem2.setHeight(1080);
        System.out.println("\n" + YoutubeDashManifestCreator.createDashManifestFromPostLiveStreamDvrStreamingUrl(postLiveDvrUrl, itagItem2, 5));
    }
}
