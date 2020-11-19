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

public class YoutubeStreamExtractorAgeRestrictedTest extends DefaultStreamExtractorTest {
    private static final String ID = "MmBeUZqv1QA";
    private static final int TIMESTAMP = 196;
    private static final String URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID + "&t=" + TIMESTAMP;
    private static StreamExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = YouTube.getStreamExtractor(URL);
        extractor.fetchPage();
    }

    @Override public StreamExtractor extractor() { return extractor; }
    @Override public StreamingService expectedService() { return YouTube; }
    @Override public String expectedName() { return "FINGERING PORNSTARS @ AVN Expo 2017 In Las Vegas!"; }
    @Override public String expectedId() { return ID; }
    @Override public String expectedUrlContains() { return YoutubeStreamExtractorDefaultTest.BASE_URL + ID; }
    @Override public String expectedOriginalUrlContains() { return URL; }

    @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
    @Override public String expectedUploaderName() { return "EpicFiveTV"; }
    @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UCuPUHlLP5POZphOIrjrNxiw"; }
    @Override public List<String> expectedDescriptionContains() { return Arrays.asList("http://instagram.com/Ruben_Sole", "AVN"); }
    @Override public long expectedLength() { return 1790; }
    @Override public long expectedTimestamp() { return TIMESTAMP; }
    @Override public long expectedViewCountAtLeast() { return 28500000; }
    @Nullable @Override public String expectedUploadDate() { return "2017-01-25 00:00:00.000"; }
    @Nullable @Override public String expectedTextualUploadDate() { return "2017-01-25"; }
    @Override public long expectedLikeCountAtLeast() { return 149000; }
    @Override public long expectedDislikeCountAtLeast() { return 38000; }
    @Override public boolean expectedHasRelatedStreams() { return false; } // no related videos (!)
    @Override public int expectedAgeLimit() { return 18; }
    @Nullable @Override public String expectedErrorMessage() { return "Sign in to confirm your age"; }
    @Override public boolean expectedHasSubtitles() { return false; }
}
