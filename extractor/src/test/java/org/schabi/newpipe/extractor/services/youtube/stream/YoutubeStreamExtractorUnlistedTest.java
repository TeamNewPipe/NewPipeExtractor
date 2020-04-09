package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.BeforeClass;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeStreamExtractorUnlistedTest extends DefaultStreamExtractorTest {
    static final String ID = "udsB8KnIJTg";
    static final String URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID;
    private static StreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = YouTube.getStreamExtractor(URL);
        extractor.fetchPage();
    }

    @Override public StreamExtractor extractor() { return extractor; }
    @Override public StreamingService expectedService() { return YouTube; }
    @Override public String expectedName() { return "Praise the Casual: Ein Neuling trifft Dark Souls - Folge 5"; }
    @Override public String expectedId() { return ID; }
    @Override public String expectedUrlContains() { return URL; }
    @Override public String expectedOriginalUrlContains() { return URL; }

    @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
    @Override public String expectedUploaderName() { return "Hooked"; }
    @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UCPysfiuOv4VKBeXFFPhKXyw"; }
    @Override public List<String> expectedDescriptionContains() {
        return Arrays.asList("https://www.youtube.com/user/Roccowschiptune",
                "https://www.facebook.com/HookedMagazinDE");
    }
    @Override public long expectedLength() { return 2488; }
    @Override public long expectedViewCountAtLeast() { return 1500; }
    @Nullable @Override public String expectedUploadDate() { return "2017-09-22 00:00:00.000"; }
    @Nullable @Override public String expectedTextualUploadDate() { return "2017-09-22"; }
    @Override public long expectedLikeCountAtLeast() { return 110; }
    @Override public long expectedDislikeCountAtLeast() { return 0; }
}
