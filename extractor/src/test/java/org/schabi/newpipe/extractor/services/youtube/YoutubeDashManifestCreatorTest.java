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
        final ItagItem itagItem = ItagItem.getItag(247);
        final String mimeType = "video/webm; codecs=\"vp9\"";
        itagItem.setCodec(mimeType.split("\"")[1]);
        itagItem.setBitrate(1505280);
        itagItem.setQuality("hd720");
        itagItem.fps = 25;
        itagItem.setWidth(1280);
        itagItem.setHeight(720);
        YoutubeDashManifestCreator.createDashManifestFromOtfStreamingUrl(otfUrl, itagItem);
    }
}
