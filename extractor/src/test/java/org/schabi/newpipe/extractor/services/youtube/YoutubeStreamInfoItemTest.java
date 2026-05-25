package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.localization.TimeAgoPatternsManager;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamInfoItemLockupExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.downloader.DownloaderFactory.getMockPath;

class YoutubeStreamInfoItemTest {
    @Test
    void videoRendererPremiere() throws FileNotFoundException, JsonParserException {
        final var json = JsonParser.object().from(new FileInputStream(getMockPath(
                YoutubeStreamInfoItemTest.class, "videoRendererPremiere") + ".json"));
        final var timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        final var extractor = new YoutubeStreamInfoItemExtractor(json, timeAgoParser);
        assertAll(
        () -> assertEquals(StreamType.VIDEO_STREAM, extractor.getStreamType()),
        () -> assertFalse(extractor.isAd()),
        () -> assertEquals("https://www.youtube.com/watch?v=M_8QNw_JM4I", extractor.getUrl()),
        () -> assertEquals("This video will premiere in 6 months.", extractor.getName()),
        () -> assertEquals(33, extractor.getDuration()),
        () -> assertEquals("Blunt Brothers Productions", extractor.getUploaderName()),
        () -> assertEquals("https://www.youtube.com/channel/UCUPrbbdnot-aPgNM65svgOg", extractor.getUploaderUrl()),
        () -> assertFalse(extractor.getUploaderAvatars().isEmpty()),
        () -> assertTrue(extractor.isUploaderVerified()),
        () -> {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            assertEquals("2026-03-15 13:12", extractor.getTextualUploadDate());
        },
        () -> {
            assertNotNull(extractor.getUploadDate());
            final var expected = LocalDateTime.of(2026, 3, 15, 13, 12).atOffset(ZoneOffset.UTC);
            assertEquals(expected, extractor.getUploadDate().offsetDateTime());
        },
        () -> assertEquals(-1, extractor.getViewCount()),
        () -> assertFalse(extractor.getThumbnails().isEmpty()),
        () -> assertEquals("Patience is key… MERCH SHOP    : https://www.bluntbrosproductions.com Follow us on Instagram for early updates: ...", extractor.getShortDescription()),
        () -> assertFalse(extractor.isShortFormContent())
        );
    }

    @Test
    void lockupViewModelPremiere()
            throws FileNotFoundException, JsonParserException {
        final var json = JsonParser.object().from(new FileInputStream(getMockPath(
                YoutubeStreamInfoItemTest.class, "lockupViewModelPremiere") + ".json"));
        final var timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        final var extractor = new YoutubeStreamInfoItemLockupExtractor(json, timeAgoParser);
        assertAll(
        () -> assertEquals(StreamType.VIDEO_STREAM, extractor.getStreamType()),
        () -> assertFalse(extractor.isAd()),
        () -> assertEquals("https://www.youtube.com/watch?v=VIDEO_ID", extractor.getUrl()),
        () -> assertEquals("VIDEO_TITLE", extractor.getName()),
        () -> assertEquals(-1, extractor.getDuration()),
        () -> assertEquals("VIDEO_CHANNEL_NAME", extractor.getUploaderName()),
        () -> assertEquals("https://www.youtube.com/channel/UCD_on7-zu7Zuc3zissQvrgw", extractor.getUploaderUrl()),
        () -> assertFalse(extractor.getUploaderAvatars().isEmpty()),
        () -> assertFalse(extractor.isUploaderVerified()),
        () -> assertEquals("14/08/2025, 13:00", extractor.getTextualUploadDate()),
        () -> {
            assertNotNull(extractor.getUploadDate());
            assertEquals(OffsetDateTime.of(2025, 8, 14, 13, 0, 0, 0, ZoneOffset.UTC), extractor.getUploadDate().offsetDateTime());
        },
        () -> assertEquals(-1, extractor.getViewCount()),
        () -> assertFalse(extractor.getThumbnails().isEmpty()),
        () -> assertNull(extractor.getShortDescription()),
        () -> assertFalse(extractor.isShortFormContent())
        );
    }

    @Test
    void lockupViewModelVideo()
            throws FileNotFoundException, JsonParserException {
        final var json = JsonParser.object().from(new FileInputStream(getMockPath(
                YoutubeStreamInfoItemTest.class, "lockupViewModelVideo") + ".json"));
        final var timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        final var extractor = new YoutubeStreamInfoItemLockupExtractor(json, timeAgoParser);
        assertAll(
        () -> assertEquals(StreamType.VIDEO_STREAM, extractor.getStreamType()),
        () -> assertFalse(extractor.isAd()),
        () -> assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", extractor.getUrl()),
        () -> assertEquals("VIDEO_TITLE", extractor.getName()),
        () -> assertEquals(974, extractor.getDuration()),
        () -> assertFalse(extractor.getThumbnails().isEmpty())
        );
    }

    @Test
    void lockupViewModelLiveStream()
            throws FileNotFoundException, JsonParserException {
        final var json = JsonParser.object().from(new FileInputStream(getMockPath(
                YoutubeStreamInfoItemTest.class, "lockupViewModelLiveStream") + ".json"));
        final var timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        final var extractor = new YoutubeStreamInfoItemLockupExtractor(json, timeAgoParser);
        assertAll(
        () -> assertEquals(StreamType.LIVE_STREAM, extractor.getStreamType()),
        () -> assertFalse(extractor.isAd()),
        () -> assertEquals("https://www.youtube.com/watch?v=LIVE_VIDEO_ID", extractor.getUrl()),
        () -> assertEquals("LIVE_VIDEO_TITLE", extractor.getName()),
        () -> assertEquals(-1, extractor.getDuration()),
        () -> assertNull(extractor.getTextualUploadDate()),
        () -> assertNull(extractor.getUploadDate()),
        () -> assertEquals(0, extractor.getViewCount()),
        () -> assertFalse(extractor.getThumbnails().isEmpty())
        );
    }

    @Test
    void lockupViewModelNoDuration()
            throws FileNotFoundException, JsonParserException {
        final var json = JsonParser.object().from(new FileInputStream(getMockPath(
                YoutubeStreamInfoItemTest.class, "lockupViewModelNoDuration") + ".json"));
        final var timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        final var extractor = new YoutubeStreamInfoItemLockupExtractor(json, timeAgoParser);
        assertAll(
        () -> assertEquals(StreamType.VIDEO_STREAM, extractor.getStreamType()),
        () -> assertFalse(extractor.isAd()),
        () -> assertEquals(-1, extractor.getDuration()),
        () -> assertFalse(extractor.getThumbnails().isEmpty())
        );
    }

    /**
     * Tests that the info row search correctly extracts date and view count
     * from the 1-row channel format where parts are in normal order: [views, date].
     */
    @Test
    void lockupViewModelOneRowNormal()
            throws FileNotFoundException, JsonParserException {
        final var json = JsonParser.object().from(new FileInputStream(getMockPath(
                YoutubeStreamInfoItemTest.class, "lockupViewModelOneRowNormal") + ".json"));
        final var timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        final var extractor = new YoutubeStreamInfoItemLockupExtractor(json, timeAgoParser) {
            // Channel tabs use 1-row format at index 0
            @Override
            protected int getInfoMetadataRowIndex() {
                return 0;
            }
        };
        assertAll(
        () -> assertEquals(StreamType.VIDEO_STREAM, extractor.getStreamType()),
        () -> assertEquals("Test Video One Row Normal", extractor.getName()),
        () -> assertEquals("2 hours ago", extractor.getTextualUploadDate()),
        () -> assertNotNull(extractor.getUploadDate()),
        () -> assertEquals(3600000, extractor.getViewCount()), // 3.6m views
        () -> assertEquals(630, extractor.getDuration()) // 10:30
        );
    }

    /**
     * Tests that the info row search correctly extracts date and view count
     * from the 1-row channel format where parts are in reversed order: [date, views].
     */
    @Test
    void lockupViewModelOneRowReversed()
            throws FileNotFoundException, JsonParserException {
        final var json = JsonParser.object().from(new FileInputStream(getMockPath(
                YoutubeStreamInfoItemTest.class, "lockupViewModelOneRowReversed") + ".json"));
        final var timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        final var extractor = new YoutubeStreamInfoItemLockupExtractor(json, timeAgoParser) {
            // Channel tabs use 1-row format at index 0
            @Override
            protected int getInfoMetadataRowIndex() {
                return 0;
            }
        };
        assertAll(
        () -> assertEquals(StreamType.VIDEO_STREAM, extractor.getStreamType()),
        () -> assertEquals("Test Video One Row Reversed", extractor.getName()),
        () -> assertEquals("1 day ago", extractor.getTextualUploadDate()),
        () -> assertNotNull(extractor.getUploadDate()),
        () -> assertEquals(1200, extractor.getViewCount()), // 1.2K views
        () -> assertEquals(300, extractor.getDuration()) // 5:00
        );
    }

    /**
     * Tests that the info row search handles 1-row format with only view count
     * (no date text present) - e.g. for livestreams with watching count only.
     */
    @Test
    void lockupViewModelOneRowViewsOnly()
            throws FileNotFoundException, JsonParserException {
        final var json = JsonParser.object().from(new FileInputStream(getMockPath(
                YoutubeStreamInfoItemTest.class, "lockupViewModelOneRowViewsOnly") + ".json"));
        final var timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        final var extractor = new YoutubeStreamInfoItemLockupExtractor(json, timeAgoParser) {
            // Channel tabs use 1-row format at index 0
            @Override
            protected int getInfoMetadataRowIndex() {
                return 0;
            }
        };
        assertAll(
        () -> assertEquals(StreamType.LIVE_STREAM, extractor.getStreamType()),
        () -> assertEquals("Test Video One Row Views Only", extractor.getName()),
        () -> assertNull(extractor.getTextualUploadDate()),
        () -> assertNull(extractor.getUploadDate()),
        () -> assertEquals(500, extractor.getViewCount()), // 500 watching
        () -> assertEquals(-1, extractor.getDuration())
        );
    }

    /**
     * Tests that channel tab items with non-views text in the metadata row
     * (e.g. section headers) don't crash and return -1 for views.
     * Regression test for blind metadataPart(infoRowIndex, 0) fallback.
     */
    @Test
    void lockupViewModelChannelTabSectionHeader()
            throws FileNotFoundException, JsonParserException {
        final var json = JsonParser.object().from(new FileInputStream(getMockPath(
                YoutubeStreamInfoItemTest.class, "lockupViewModelChannelTabSectionHeader")
                + ".json"));
        final var timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(
                Localization.DEFAULT);
        final var extractor = new YoutubeStreamInfoItemLockupExtractor(json, timeAgoParser) {
            // Channel tabs use 1-row format at index 0
            @Override
            protected int getInfoMetadataRowIndex() {
                return 0;
            }
        };
        assertAll(
        () -> assertEquals(StreamType.VIDEO_STREAM, extractor.getStreamType()),
        () -> assertEquals("Section Header Video", extractor.getName()),
        () -> assertNull(extractor.getTextualUploadDate()),
        () -> assertNull(extractor.getUploadDate()),
        () -> assertEquals(-1, extractor.getViewCount()),
        () -> assertEquals(-1, extractor.getDuration())
        );
    }

    /**
     * Tests that live streams in channel tabs with only a channel name row
     * (no viewer count) don't crash and return 0 for views.
     * Regression test for blind metadataPart(infoRowIndex, 0) fallback.
     */
    @Test
    void lockupViewModelChannelTabLiveNoViewers()
            throws FileNotFoundException, JsonParserException {
        final var json = JsonParser.object().from(new FileInputStream(getMockPath(
                YoutubeStreamInfoItemTest.class, "lockupViewModelChannelTabLiveNoViewers")
                + ".json"));
        final var timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(
                Localization.DEFAULT);
        final var extractor = new YoutubeStreamInfoItemLockupExtractor(json, timeAgoParser) {
            // Channel tabs use 1-row format at index 0
            @Override
            protected int getInfoMetadataRowIndex() {
                return 0;
            }
        };
        assertAll(
        () -> assertEquals(StreamType.LIVE_STREAM, extractor.getStreamType()),
        () -> assertEquals("Live Stream No Viewers", extractor.getName()),
        () -> assertNull(extractor.getTextualUploadDate()),
        () -> assertNull(extractor.getUploadDate()),
        () -> assertEquals(0, extractor.getViewCount()),
        () -> assertEquals(-1, extractor.getDuration())
        );
    }

    @Test
    void emptyTitle() throws FileNotFoundException, JsonParserException {
        final var json = JsonParser.object().from(new FileInputStream(getMockPath(
                YoutubeStreamInfoItemTest.class, "emptyTitle") + ".json"));
        final var timeAgoParser = TimeAgoPatternsManager.getTimeAgoParserFor(Localization.DEFAULT);
        final var extractor = new YoutubeStreamInfoItemExtractor(json, timeAgoParser);
        assertAll(
                () -> assertEquals(StreamType.VIDEO_STREAM, extractor.getStreamType()),
                () -> assertFalse(extractor.isAd()),
                () -> assertEquals("https://www.youtube.com/watch?v=nc1kN8ZSfGQ", extractor.getUrl()),
                () -> assertEquals("", extractor.getName()),
                () -> assertEquals(39, extractor.getDuration()),
                () -> assertEquals("hyper", extractor.getUploaderName()),
                () -> assertEquals("https://www.youtube.com/channel/UCSezUnbvCLYBXuUlPcXU_QQ", extractor.getUploaderUrl()),
                () -> assertFalse(extractor.getUploaderAvatars().isEmpty()),
                () -> assertTrue(extractor.isUploaderVerified()),
                () -> assertEquals("8 years ago", extractor.getTextualUploadDate()),
                () -> assertNotNull(extractor.getUploadDate()),
                () -> assertTrue(extractor.getViewCount() >= 1318193),
                () -> assertFalse(extractor.getThumbnails().isEmpty()),
                () -> assertNull(extractor.getShortDescription()),
                () -> assertFalse(extractor.isShortFormContent())
        );
    }
}
