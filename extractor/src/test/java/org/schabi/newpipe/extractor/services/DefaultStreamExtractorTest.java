package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreaterOrEqual;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEqualsOrderIndependent;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsValidUrl;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestListOfItems;
import static org.schabi.newpipe.extractor.stream.StreamExtractor.UNKNOWN_SUBSCRIBER_COUNT;

/**
 * Test for {@link StreamExtractor}
 */
public abstract class DefaultStreamExtractorTest extends DefaultExtractorTest<StreamExtractor>
        implements BaseStreamExtractorTest {

    public abstract StreamType expectedStreamType();
    public abstract String expectedUploaderName();
    public abstract String expectedUploaderUrl();
    public boolean expectedUploaderVerified() { return false; }
    public long expectedUploaderSubscriberCountAtLeast() { return UNKNOWN_SUBSCRIBER_COUNT; }
    public String expectedSubChannelName() { return ""; } // default: there is no subchannel
    public String expectedSubChannelUrl() { return ""; } // default: there is no subchannel
    public boolean expectedDescriptionIsEmpty() { return false; } // default: description is not empty
    public abstract List<String> expectedDescriptionContains(); // e.g. for full links
    public abstract long expectedLength();
    public long expectedTimestamp() { return 0; } // default: there is no timestamp
    public abstract long expectedViewCountAtLeast();
    @Nullable public abstract String expectedUploadDate(); // format: "yyyy-MM-dd HH:mm:ss.SSS"
    @Nullable public abstract String expectedTextualUploadDate();
    public abstract long expectedLikeCountAtLeast(); // return -1 if ratings are disabled
    public abstract long expectedDislikeCountAtLeast(); // return -1 if ratings are disabled
    public boolean expectedHasRelatedItems() { return true; } // default: there are related videos
    public int expectedAgeLimit() { return StreamExtractor.NO_AGE_LIMIT; } // default: no limit
    @Nullable public String expectedErrorMessage() { return null; } // default: no error message
    public boolean expectedHasVideoStreams() { return true; } // default: there are video streams
    public boolean expectedHasAudioStreams() { return true; } // default: there are audio streams
    public boolean expectedHasSubtitles() { return true; } // default: there are subtitles streams
    @Nullable public String expectedDashMpdUrlContains() { return null; } // default: no dash mpd
    public boolean expectedHasFrames() { return true; } // default: there are frames
    public String expectedHost() { return ""; } // default: no host for centralized platforms
    public StreamExtractor.Privacy expectedPrivacy() { return StreamExtractor.Privacy.PUBLIC; } // default: public
    public String expectedCategory() { return ""; } // default: no category
    public String expectedLicence() { return ""; } // default: no licence
    public Locale expectedLanguageInfo() { return null; } // default: no language info available
    public List<String> expectedTags() { return Collections.emptyList(); } // default: no tags
    public String expectedSupportInfo() { return ""; } // default: no support info available
    public int expectedStreamSegmentsCount() { return -1; } // return 0 or greater to test (default is -1 to ignore)
    public List<MetaInfo> expectedMetaInfo() throws MalformedURLException { return Collections.emptyList(); } // default: no metadata info available

    @Test
    @Override
    public void testStreamType() throws Exception {
        assertEquals(expectedStreamType(), extractor().getStreamType());
    }

    @Test
    @Override
    public void testUploaderName() throws Exception {
        assertEquals(expectedUploaderName(), extractor().getUploaderName());
    }

    @Test
    @Override
    public void testUploaderUrl() throws Exception {
        final String uploaderUrl = extractor().getUploaderUrl();
        assertIsSecureUrl(uploaderUrl);
        assertEquals(expectedUploaderUrl(), uploaderUrl);
    }

    @Test
    @Override
    public void testUploaderAvatarUrl() throws Exception {
        assertIsSecureUrl(extractor().getUploaderAvatarUrl());
    }

    @Test
    public void testUploaderVerified() throws Exception {
        assertEquals(expectedUploaderVerified(), extractor().isUploaderVerified());
    }

    @Test
    @Override
    public void testSubscriberCount() throws Exception {
        if (expectedUploaderSubscriberCountAtLeast() == UNKNOWN_SUBSCRIBER_COUNT) {
            assertEquals(UNKNOWN_SUBSCRIBER_COUNT, extractor().getUploaderSubscriberCount());
        } else {
            assertGreaterOrEqual(expectedUploaderSubscriberCountAtLeast(), extractor().getUploaderSubscriberCount());
        }
    }

    @Test
    @Override
    public void testSubChannelName() throws Exception {
        assertEquals(expectedSubChannelName(), extractor().getSubChannelName());
    }

    @Test
    @Override
    public void testSubChannelUrl() throws Exception {
        final String subChannelUrl = extractor().getSubChannelUrl();
        assertEquals(expectedSubChannelUrl(), subChannelUrl);

        if (!expectedSubChannelUrl().isEmpty()) {
            // this stream has a subchannel
            assertIsSecureUrl(subChannelUrl);
        }
    }

    @Test
    @Override
    public void testSubChannelAvatarUrl() throws Exception {
        if (expectedSubChannelName().isEmpty() && expectedSubChannelUrl().isEmpty()) {
            // this stream has no subchannel
            assertEquals("", extractor().getSubChannelAvatarUrl());
        } else {
            // this stream has a subchannel
            assertIsSecureUrl(extractor().getSubChannelAvatarUrl());
        }
    }

    @Test
    @Override
    public void testThumbnailUrl() throws Exception {
        assertIsSecureUrl(extractor().getThumbnailUrl());
    }

    @Test
    @Override
    public void testDescription() throws Exception {
        final Description description = extractor().getDescription();
        assertNotNull(description);

        if (expectedDescriptionIsEmpty()) {
            assertTrue(description.getContent().isEmpty(), "description is not empty");
        } else {
            assertFalse(description.getContent().isEmpty(), "description is empty");
        }

        for (final String s : expectedDescriptionContains()) {
            ExtractorAsserts.assertContains(s, description.getContent());
        }
    }

    @Test
    @Override
    public void testLength() throws Exception {
        assertEquals(expectedLength(), extractor().getLength());
    }

    @Test
    @Override
    public void testTimestamp() throws Exception {
        assertEquals(expectedTimestamp(), extractor().getTimeStamp());
    }

    @Test
    @Override
    public void testViewCount() throws Exception {
        assertGreaterOrEqual(expectedViewCountAtLeast(), extractor().getViewCount());
    }

    @Test
    @Override
    public void testUploadDate() throws Exception {
        final DateWrapper dateWrapper = extractor().getUploadDate();

        if (expectedUploadDate() == null) {
            assertNull(dateWrapper);
        } else {
            assertNotNull(dateWrapper);

            final LocalDateTime expectedDateTime = LocalDateTime.parse(expectedUploadDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            final LocalDateTime actualDateTime = dateWrapper.offsetDateTime().toLocalDateTime();

            assertEquals(expectedDateTime, actualDateTime);
        }
    }

    @Test
    @Override
    public void testTextualUploadDate() throws Exception {
        assertEquals(expectedTextualUploadDate(), extractor().getTextualUploadDate());
    }

    @Test
    @Override
    public void testLikeCount() throws Exception {
        if (expectedLikeCountAtLeast() == -1) {
            assertEquals(-1, extractor().getLikeCount());
        } else {
            assertGreaterOrEqual(expectedLikeCountAtLeast(), extractor().getLikeCount());
        }
    }

    @Test
    @Override
    public void testDislikeCount() throws Exception {
        if (expectedDislikeCountAtLeast() == -1) {
            assertEquals(-1, extractor().getDislikeCount());
        } else {
            assertGreaterOrEqual(expectedDislikeCountAtLeast(), extractor().getDislikeCount());
        }
    }

    @Test
    @Override
    public void testRelatedItems() throws Exception {
        final InfoItemsCollector<?, ?> relatedStreams = extractor().getRelatedItems();

        if (expectedHasRelatedItems()) {
            assertNotNull(relatedStreams);
            defaultTestListOfItems(extractor().getService(), relatedStreams.getItems(),
                    relatedStreams.getErrors());
        } else {
            assertTrue(relatedStreams == null || relatedStreams.getItems().isEmpty());
        }
    }

    @Test
    @Override
    public void testAgeLimit() throws Exception {
        assertEquals(expectedAgeLimit(), extractor().getAgeLimit());
    }

    @Test
    @Override
    public void testErrorMessage() throws Exception {
        assertEquals(expectedErrorMessage(), extractor().getErrorMessage());
    }

    @Test
    @Override
    public void testVideoStreams() throws Exception {
        final List<VideoStream> videoStreams = extractor().getVideoStreams();
        final List<VideoStream> videoOnlyStreams = extractor().getVideoOnlyStreams();
        assertNotNull(videoStreams);
        assertNotNull(videoOnlyStreams);
        videoStreams.addAll(videoOnlyStreams);

        if (expectedHasVideoStreams()) {
            assertFalse(videoStreams.isEmpty());

            for (final VideoStream stream : videoStreams) {
                if (stream.isUrl()) {
                    assertIsSecureUrl(stream.getContent());
                }
                final StreamType streamType = extractor().getStreamType();
                // On some video streams, the resolution can be empty and the format be unknown,
                // especially on livestreams (like streams with HLS master playlists)
                if (streamType != StreamType.LIVE_STREAM
                        && streamType != StreamType.AUDIO_LIVE_STREAM) {
                    assertFalse(stream.getResolution().isEmpty());
                    final int formatId = stream.getFormatId();
                    // see MediaFormat: video stream formats range from 0 to 0x100
                    assertTrue(0 <= formatId && formatId < 0x100,
                            "Format id does not fit a video stream: " + formatId);
                }
            }
        } else {
            assertTrue(videoStreams.isEmpty());
        }
    }

    @Test
    @Override
    public void testAudioStreams() throws Exception {
        final List<AudioStream> audioStreams = extractor().getAudioStreams();
        assertNotNull(audioStreams);

        if (expectedHasAudioStreams()) {
            assertFalse(audioStreams.isEmpty());

            for (final AudioStream stream : audioStreams) {
                if (stream.isUrl()) {
                    assertIsSecureUrl(stream.getContent());
                }

                // The media format can be unknown on some audio streams
                if (stream.getFormat() != null) {
                    final int formatId = stream.getFormat().id;
                    // see MediaFormat: audio stream formats range from 0x100 to 0x1000
                    assertTrue(0x100 <= formatId && formatId < 0x1000,
                            "Format id does not fit an audio stream: " + formatId);
                }
            }
        } else {
            assertTrue(audioStreams.isEmpty());
        }
    }

    @Test
    @Override
    public void testSubtitles() throws Exception {
        final List<SubtitlesStream> subtitles = extractor().getSubtitlesDefault();
        assertNotNull(subtitles);

        if (expectedHasSubtitles()) {
            assertFalse(subtitles.isEmpty());

            for (final SubtitlesStream stream : subtitles) {
                if (stream.isUrl()) {
                    assertIsSecureUrl(stream.getContent());
                }

                final int formatId = stream.getFormatId();
                // see MediaFormat: video stream formats range from 0x1000 to 0x10000
                assertTrue(0x1000 <= formatId && formatId < 0x10000,
                        "Format id does not fit a subtitles stream: " + formatId);
            }
        } else {
            assertTrue(subtitles.isEmpty());

            final MediaFormat[] formats = {MediaFormat.VTT, MediaFormat.TTML, MediaFormat.SRT,
                    MediaFormat.TRANSCRIPT1, MediaFormat.TRANSCRIPT2, MediaFormat.TRANSCRIPT3};
            for (final MediaFormat format : formats) {
                final List<SubtitlesStream> formatSubtitles = extractor().getSubtitles(format);
                assertNotNull(formatSubtitles);
                assertTrue(formatSubtitles.isEmpty());
            }
        }
    }

    @Override
    public void testGetDashMpdUrl() throws Exception {
        final String dashMpdUrl = extractor().getDashMpdUrl();
        if (expectedDashMpdUrlContains() == null) {
            assertNotNull(dashMpdUrl);
            assertTrue(dashMpdUrl.isEmpty());
        } else {
            assertIsSecureUrl(dashMpdUrl);
            ExtractorAsserts.assertContains(expectedDashMpdUrlContains(),
                    extractor().getDashMpdUrl());
        }
    }

    @Test
    @Override
    public void testFrames() throws Exception {
        final List<Frameset> frames = extractor().getFrames();
        assertNotNull(frames);

        if (expectedHasFrames()) {
            assertFalse(frames.isEmpty());
            for (final Frameset f : frames) {
                for (final String url : f.getUrls()) {
                    assertIsValidUrl(url);
                    assertIsSecureUrl(url);
                }
                assertTrue(f.getDurationPerFrame() > 0);
                assertEquals(f.getFrameBoundsAt(0)[3], f.getFrameWidth());
            }
        } else {
            assertTrue(frames.isEmpty());
        }
    }

    @Test
    @Override
    public void testHost() throws Exception {
        assertEquals(expectedHost(), extractor().getHost());
    }

    @Test
    @Override
    public void testPrivacy() throws Exception {
        assertEquals(expectedPrivacy(), extractor().getPrivacy());
    }

    @Test
    @Override
    public void testCategory() throws Exception {
        assertEquals(expectedCategory(), extractor().getCategory());
    }

    @Test
    @Override
    public void testLicence() throws Exception {
        assertEquals(expectedLicence(), extractor().getLicence());
    }

    @Test
    @Override
    public void testLanguageInfo() throws Exception {
        assertEquals(expectedLanguageInfo(), extractor().getLanguageInfo());
    }

    @Test
    @Override
    public void testTags() throws Exception {
        assertEqualsOrderIndependent(expectedTags(), extractor().getTags());
    }

    @Test
    @Override
    public void testSupportInfo() throws Exception {
        assertEquals(expectedSupportInfo(), extractor().getSupportInfo());
    }

    @Test
    public void testStreamSegmentsCount() throws Exception {
        if (expectedStreamSegmentsCount() >= 0) {
            assertEquals(expectedStreamSegmentsCount(), extractor().getStreamSegments().size());
        }
    }

    /**
     * @see DefaultSearchExtractorTest#testMetaInfo()
     */
    @Test
    public void testMetaInfo() throws Exception {
        final List<MetaInfo> metaInfoList = extractor().getMetaInfo();
        final List<MetaInfo> expectedMetaInfoList = expectedMetaInfo();

        for (final MetaInfo expectedMetaInfo : expectedMetaInfoList) {
            final List<String> texts = metaInfoList.stream()
                    .map((metaInfo) -> metaInfo.getContent().getContent())
                    .collect(Collectors.toList());
            final List<String> titles = metaInfoList.stream().map(MetaInfo::getTitle).collect(Collectors.toList());
            final List<URL> urls = metaInfoList.stream().flatMap(info -> info.getUrls().stream())
                    .collect(Collectors.toList());
            final List<String> urlTexts = metaInfoList.stream().flatMap(info -> info.getUrlTexts().stream())
                    .collect(Collectors.toList());

            assertTrue(texts.contains(expectedMetaInfo.getContent().getContent()));
            assertTrue(titles.contains(expectedMetaInfo.getTitle()));

            for (final String expectedUrlText : expectedMetaInfo.getUrlTexts()) {
                assertTrue(urlTexts.contains(expectedUrlText));
            }
            for (final URL expectedUrl : expectedMetaInfo.getUrls()) {
                assertTrue(urls.contains(expectedUrl));
            }
        }

    }
}
