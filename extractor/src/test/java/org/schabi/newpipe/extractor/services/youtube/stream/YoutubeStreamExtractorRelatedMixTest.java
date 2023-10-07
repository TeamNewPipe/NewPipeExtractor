package org.schabi.newpipe.extractor.services.youtube.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertContains;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreaterOrEqual;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.stream.YoutubeStreamExtractorDefaultTest.YOUTUBE_LICENCE;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.YoutubeTestsUtils;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class YoutubeStreamExtractorRelatedMixTest extends DefaultStreamExtractorTest {
    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/stream/";
    private static final String ID = "K4DyBUG242c";
    private static final String URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID;
    private static final String TITLE = "Cartoon - On & On (feat. Daniel Levi) | Electronic Pop | NCS - Copyright Free Music";
    private static StreamExtractor extractor;

    @BeforeAll
    public static void setUp() throws Exception {
        YoutubeTestsUtils.ensureStateless();
        YoutubeParsingHelper.setConsentAccepted(true);
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "relatedMix"));
        extractor = YouTube.getStreamExtractor(URL);
        extractor.fetchPage();
    }

    // @formatter:off
    @Override public StreamExtractor extractor() { return extractor; }
    @Override public StreamingService expectedService() { return YouTube; }
    @Override public String expectedName() { return TITLE; }
    @Override public String expectedId() { return ID; }
    @Override public String expectedUrlContains() { return URL; }
    @Override public String expectedOriginalUrlContains() { return URL; }

    @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
    @Override public String expectedUploaderName() { return "NoCopyrightSounds"; }
    @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UC_aEa8K-EOJ3D6gOs7HcyNg"; }
    @Override public List<String> expectedDescriptionContains() {
        return Arrays.asList("https://www.youtube.com/user/danielleviband/", "©");
    }
    @Override public boolean expectedUploaderVerified() { return true; }
    @Override public long expectedUploaderSubscriberCountAtLeast() { return 32_000_000; }
    @Override public long expectedLength() { return 208; }
    @Override public long expectedViewCountAtLeast() { return 449_000_000; }
    @Nullable @Override public String expectedUploadDate() { return "2015-07-09 16:34:35.000"; }
    @Nullable @Override public String expectedTextualUploadDate() { return "2015-07-09T09:34:35-07:00"; }
    @Override public long expectedLikeCountAtLeast() { return 6_400_000; }
    @Override public long expectedDislikeCountAtLeast() { return -1; }
    @Override public int expectedStreamSegmentsCount() { return 0; }
    @Override public String expectedLicence() { return YOUTUBE_LICENCE; }
    @Override public String expectedCategory() { return "Music"; }
    @Override public List<String> expectedTags() {
        return Arrays.asList("Cartoon On & On (feat. Daniel Levi)", "Cartoon - On & On", "Cartoon",
                "On & On", "NCS", "nocopyrightsounds", "no copyright sounds", "NCS release Cartoon",
                "NCS Release Daniel Levi", "Daniel Levi", "NCS Release", "NCS Cartoon On & On",
                "NCS On and On", "NCS On & On", "NCS Best Songs", "NCS Cartoon Daniel Levi",
                "music", "songs", "ncs", "edm", "best music", "top music", "free music",
                "club music", "dance music", "no copyright music", "electronic music",
                "royalty free music", "copyright free music", "gaming music", "electronic pop");
    }
    // @formatter:on

    @Test
    @Override
    public void testRelatedItems() throws Exception {
        super.testRelatedItems();

        final List<PlaylistInfoItem> playlists = Objects.requireNonNull(extractor.getRelatedItems())
                .getItems()
                .stream()
                .filter(PlaylistInfoItem.class::isInstance)
                .map(PlaylistInfoItem.class::cast)
                .collect(Collectors.toList());
        playlists.forEach(item -> assertNotEquals(PlaylistType.NORMAL, item.getPlaylistType(),
                "Unexpected normal playlist in related items"));

        final List<PlaylistInfoItem> streamMixes = playlists.stream()
                .filter(item -> item.getPlaylistType().equals(PlaylistType.MIX_STREAM))
                .collect(Collectors.toList());
        assertGreaterOrEqual(1, streamMixes.size(), "Not found one or more stream mix in related items");

        final PlaylistInfoItem streamMix = streamMixes.get(0);
        assertSame(InfoItem.InfoType.PLAYLIST, streamMix.getInfoType());
        assertEquals(YouTube.getServiceId(), streamMix.getServiceId());
        assertContains(URL, streamMix.getUrl());
        assertContains("list=RD" + ID, streamMix.getUrl());
        assertEquals("Mix – " + TITLE, streamMix.getName());
        YoutubeTestsUtils.testImages(streamMix.getThumbnails());
    }
}
