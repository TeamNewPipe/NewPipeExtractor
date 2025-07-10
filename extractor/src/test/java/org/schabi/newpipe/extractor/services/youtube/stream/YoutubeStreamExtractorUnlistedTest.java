package org.schabi.newpipe.extractor.services.youtube.stream;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.stream.StreamExtractor.Privacy.UNLISTED;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.InitYoutubeTest;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public class YoutubeStreamExtractorUnlistedTest extends DefaultStreamExtractorTest
    implements InitYoutubeTest {
    static final String ID = "udsB8KnIJTg";
    static final String URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID;

    @Override
    protected StreamExtractor createExtractor() throws Exception {
        return YouTube.getStreamExtractor(URL);
    }

    @Override public StreamingService expectedService() { return YouTube; }
    @Override public String expectedName() { return "Praise the Casual: Ein Neuling trifft Dark Souls - Folge 5"; }
    @Override public String expectedId() { return ID; }
    @Override public String expectedUrlContains() { return URL; }
    @Override public String expectedOriginalUrlContains() { return URL; }

    @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
    @Override public String expectedUploaderName() { return "Hooked"; }
    @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UCPysfiuOv4VKBeXFFPhKXyw"; }
    @Override public long expectedUploaderSubscriberCountAtLeast() { return 24_300; }
    @Override public List<String> expectedDescriptionContains() {
        return Arrays.asList("https://www.youtube.com/user/Roccowschiptune",
                "https://www.facebook.com/HookedMagazinDE");
    }
    @Override public long expectedLength() { return 2488; }
    @Override public long expectedViewCountAtLeast() { return 1500; }
    @Nullable @Override public String expectedUploadDate() { return "2017-09-22 12:15:21.000"; }
    @Nullable @Override public String expectedTextualUploadDate() { return "2017-09-22T05:15:21-07:00"; }
    @Override public long expectedLikeCountAtLeast() { return 110; }
    @Override public long expectedDislikeCountAtLeast() { return -1; }
    @Override public StreamExtractor.Privacy expectedPrivacy() { return UNLISTED; }
    @Override public String expectedLicence() { return "YouTube licence"; }
    @Override public String expectedCategory() { return "Gaming"; }
    @Override public List<String> expectedTags() { return Arrays.asList("dark souls", "hooked", "praise the casual"); }
}
