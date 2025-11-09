package org.schabi.newpipe.extractor.services.youtube.stream;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertNotEmpty;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestImageCollection;

import org.junit.jupiter.api.Disabled;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.InitYoutubeTest;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class YoutubeStreamExtractorCollaboratorsTest extends DefaultStreamExtractorTest
    implements InitYoutubeTest {
    private static final String ID = "3sbYbckT1VY";
    private static final String URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID;

    @Override
    protected StreamExtractor createExtractor() throws Exception {
        return YouTube.getStreamExtractor(URL);
    }

    @Override public StreamingService expectedService() { return YouTube; }
    @Override public String expectedName() { return "Engineers vs Pumpkin Carving 2.0"; }
    @Override public String expectedId() { return ID; }
    @Override public String expectedUrlContains() { return YoutubeStreamExtractorDefaultTest.BASE_URL + ID; }
    @Override public String expectedOriginalUrlContains() { return URL; }

    @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
    @Override public String expectedUploaderName() { return "CrunchLabs"; }
    @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UC513PdAP2-jWkJunTh5kXRw"; }
    @Override public long expectedUploaderSubscriberCountAtLeast() { return 227_0000; }
    @Override public boolean expectedUploaderVerified() { return true; }
    @Override public boolean expectedDescriptionIsEmpty() { return false; }
    @Override public List<String> expectedDescriptionContains() { return Collections.emptyList(); }
    @Override public long expectedLength() { return 696; }
    @Override public long expectedViewCountAtLeast() { return 1_400_000; }
    @Nullable @Override public String expectedUploadDate() { return "2025-10-25 15:33:05.000"; }
    @Nullable @Override public String expectedTextualUploadDate() { return "2025-10-25T08:33:05-07:00"; }
    @Override public long expectedLikeCountAtLeast() { return 20_000; }
    @Override public long expectedDislikeCountAtLeast() { return -1; }
    @Override public boolean expectedHasSubtitles() { return true; }
    @Override public boolean expectedHasFrames() { return true; }

    @Override public String expectedCategory() { return "Science & Technology"; }

    @Override public String expectedLicence() { return "YouTube licence"; }
    @Override
    public List<String> expectedTags() {
        return Collections.emptyList();
    }

    @Override
    public void testUploaderAvatars() throws Exception {
       List<Image> avatars = extractor().getUploaderAvatars();
       assertNotEmpty(avatars);
       defaultTestImageCollection(avatars);
    }
}
