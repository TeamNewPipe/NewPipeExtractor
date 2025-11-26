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
}
