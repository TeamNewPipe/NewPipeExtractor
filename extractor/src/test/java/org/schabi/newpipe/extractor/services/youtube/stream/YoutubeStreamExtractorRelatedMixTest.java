package org.schabi.newpipe.extractor.services.youtube.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertContains;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.stream.YoutubeStreamExtractorDefaultTest.YOUTUBE_LICENCE;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
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
    static final String ID = "K4DyBUG242c";
    static final String URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID;
    static final String TITLE = "Cartoon - On & On (feat. Daniel Levi) [NCS Release]";
    private static StreamExtractor extractor;

    @BeforeAll
    public static void setUp() throws Exception {
        YoutubeTestsUtils.ensureStateless();
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
    @Override public long expectedTimestamp() { return 0; }
    @Override public long expectedViewCountAtLeast() { return 449_000_000; }
    @Nullable @Override public String expectedUploadDate() { return "2015-07-09 00:00:00.000"; }
    @Nullable @Override public String expectedTextualUploadDate() { return "2015-07-09"; }
    @Override public long expectedLikeCountAtLeast() { return 6_400_000; }
    @Override public long expectedDislikeCountAtLeast() { return -1; }
    @Override public boolean expectedHasSubtitles() { return true; }
    @Override public int expectedStreamSegmentsCount() { return 0; }
    @Override public String expectedLicence() { return YOUTUBE_LICENCE; }
    @Override public String expectedCategory() { return "Music"; }
    @Override public List<String> expectedTags() {
        return Arrays.asList("Cartoon", "Cartoon - On & On", "Cartoon Baboon",
                "Cartoon NCS Release", "Cartoon On & On (feat. Daniel Levi)", "Cartoon best songs",
                "Copyright Free Music", "Daniel Levi", "NCS", "NCS Best Songs",
                "NCS Cartoon Daniel Levi", "NCS Cartoon On & On", "NCS On & On", "NCS On and On",
                "NCS Release", "NCS Release Daniel Levi", "NCS release Cartoon", "Official",
                "On & On", "On & On NCS", "On and on", "Royalty Free Cartoon", "Royalty Free Music",
                "electronic", "no copyright sounds", "nocopyrightsounds", "on & on lyrics",
                "on and on lyrics");
    }
    // @formatter:on

    @Test
    @Disabled("Mixes are not available in related items anymore, see https://github.com/TeamNewPipe/NewPipeExtractor/issues/820")
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
        assertEquals(1, streamMixes.size(), "Not found exactly one stream mix in related items");

        final PlaylistInfoItem streamMix = streamMixes.get(0);
        assertSame(InfoItem.InfoType.PLAYLIST, streamMix.getInfoType());
        assertEquals(YouTube.getServiceId(), streamMix.getServiceId());
        assertContains(URL, streamMix.getUrl());
        assertContains("list=RD" + ID, streamMix.getUrl());
        assertEquals("Mix – " + TITLE, streamMix.getName());
        assertIsSecureUrl(streamMix.getThumbnailUrl());

        final List<PlaylistInfoItem> musicMixes = playlists.stream()
                .filter(item -> item.getPlaylistType().equals(PlaylistType.MIX_MUSIC))
                .collect(Collectors.toList());
        assertEquals(1, musicMixes.size(), "Not found exactly one music mix in related items");

        final PlaylistInfoItem musicMix = musicMixes.get(0);
        assertSame(InfoItem.InfoType.PLAYLIST, musicMix.getInfoType());
        assertEquals(YouTube.getServiceId(), musicMix.getServiceId());
        assertContains("list=RDCLAK", musicMix.getUrl());
        assertEquals("Hip Hop Essentials", musicMix.getName());
        assertIsSecureUrl(musicMix.getThumbnailUrl());
    }
}
