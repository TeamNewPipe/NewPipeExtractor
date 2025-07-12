package org.schabi.newpipe.extractor.services.youtube.stream;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.InitYoutubeTest;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public class YoutubeStreamExtractorLivestreamTest extends DefaultStreamExtractorTest
    implements InitYoutubeTest {
    private static final String ID = "jfKfPfyJRdk";
    private static final int TIMESTAMP = 1737;
    private static final String URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID + "&t=" + TIMESTAMP;

    @Override
    protected StreamExtractor createExtractor() throws Exception {
        return YouTube.getStreamExtractor(URL);
    }

    @Override
    @Test
    public void testUploaderName() throws Exception {
        super.testUploaderName();
    }

    @Override public StreamingService expectedService() { return YouTube; }
    @Override public String expectedName() { return "lofi hip hop radio \uD83D\uDCDA beats to relax/study to"; }
    @Override public String expectedId() { return ID; }
    @Override public String expectedUrlContains() { return YoutubeStreamExtractorDefaultTest.BASE_URL + ID; }
    @Override public String expectedOriginalUrlContains() { return URL; }

    @Override public StreamType expectedStreamType() { return StreamType.LIVE_STREAM; }
    @Override public String expectedUploaderName() { return "Lofi Girl"; }
    @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UCSJ4gkVC6NrvII8umztf0Ow"; }
    @Override public long expectedUploaderSubscriberCountAtLeast() { return 9_800_000; }
    @Override public List<String> expectedDescriptionContains() {
        return Arrays.asList("Lofi Girl merch",
                "Thank you for listening, I hope you will have a good time here");
    }
    @Override public boolean expectedUploaderVerified() { return true; }
    @Override public long expectedLength() { return 0; }
    @Override public long expectedTimestamp() { return TIMESTAMP; }
    @Override public long expectedViewCountAtLeast() { return 0; }
    @Nullable @Override public String expectedUploadDate() { return "2022-07-12 12:12:29.000"; }
    @Nullable @Override public String expectedTextualUploadDate() { return "2022-07-12T05:12:29-07:00"; }
    @Override public long expectedLikeCountAtLeast() { return 340_000; }
    @Override public long expectedDislikeCountAtLeast() { return -1; }
    @Override public boolean expectedHasSubtitles() { return false; }
    @Nullable @Override public String expectedDashMpdUrlContains() { return "https://manifest.googlevideo.com/api/manifest/dash/"; }
    @Override public boolean expectedHasFrames() { return false; }
    @Override public String expectedLicence() { return "YouTube licence"; }
    @Override public String expectedCategory() { return "Music"; }
    @Override public List<String> expectedTags() {
        return Arrays.asList("beats to relax", "chilled cow", "chilled cow radio", "chilledcow", "chilledcow radio",
                "chilledcow station", "chillhop", "hip hop", "hiphop", "lo fi", "lo fi hip hop", "lo fi hip hop radio",
                "lo fi hiphop", "lo fi radio", "lo-fi", "lo-fi hip hop", "lo-fi hip hop radio", "lo-fi hiphop",
                "lo-fi radio", "lofi", "lofi hip hop", "lofi hip hop radio", "lofi hiphop", "lofi radio", "music",
                "lofi radio chilledcow", "music to study", "playlist", "radio", "relaxing music", "study music",
                "lofi hip hop radio - beats to relax\\/study to");
    }
}
