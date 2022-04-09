package org.schabi.newpipe.extractor.services.youtube.stream;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import org.junit.jupiter.api.BeforeAll;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.YoutubeTestsUtils;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class YoutubeStreamExtractorAgeRestrictedTest extends DefaultStreamExtractorTest {
    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/stream/";
    private static final String ID = "rwcfPqbAx-0";
    private static final int TIMESTAMP = 196;
    private static final String URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID + "&t=" + TIMESTAMP;
    private static StreamExtractor extractor;

    @BeforeAll
    public static void setUp() throws Exception {
        YoutubeTestsUtils.ensureStateless();
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "ageRestricted"));
        extractor = YouTube.getStreamExtractor(URL);
        extractor.fetchPage();
    }

    @Override public StreamExtractor extractor() { return extractor; }
    @Override public StreamingService expectedService() { return YouTube; }
    @Override public String expectedName() { return "Russian   Daft Punk"; }
    @Override public String expectedId() { return ID; }
    @Override public String expectedUrlContains() { return YoutubeStreamExtractorDefaultTest.BASE_URL + ID; }
    @Override public String expectedOriginalUrlContains() { return URL; }

    @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
    @Override public String expectedUploaderName() { return "DAN TV"; }
    @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UCcQHIVL83g5BEQe2IJFb-6w"; }
    @Override public long expectedUploaderSubscriberCountAtLeast() { return 50; }
    @Override public boolean expectedUploaderVerified() { return false; }
    @Override public boolean expectedDescriptionIsEmpty() { return true; }
    @Override public List<String> expectedDescriptionContains() { return Collections.emptyList(); }
    @Override public long expectedLength() { return 10; }
    @Override public long expectedTimestamp() { return TIMESTAMP; }
    @Override public long expectedViewCountAtLeast() { return 232_000; }
    @Nullable @Override public String expectedUploadDate() { return "2018-03-11 00:00:00.000"; }
    @Nullable @Override public String expectedTextualUploadDate() { return "2018-03-11"; }
    @Override public long expectedLikeCountAtLeast() { return 3_700; }
    @Override public long expectedDislikeCountAtLeast() { return -1; }
    @Override public boolean expectedHasRelatedItems() { return false; } // no related videos (!)
    @Override public int expectedAgeLimit() { return 18; }
    @Override public boolean expectedHasSubtitles() { return false; }
    @Override public boolean expectedHasFrames() { return false; }

    @Override public String expectedCategory() { return "People & Blogs"; }

    @Override public String expectedLicence() { return "YouTube licence"; }
    @Override
    public List<String> expectedTags() {
        return Collections.emptyList();
    }
}
