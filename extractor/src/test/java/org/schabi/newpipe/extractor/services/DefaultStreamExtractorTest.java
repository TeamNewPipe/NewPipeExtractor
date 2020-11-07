package org.schabi.newpipe.extractor.services;

import org.junit.Test;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertAtLeast;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEqualsOrderIndependent;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsValidUrl;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestListOfItems;

/**
 * Test for {@link StreamExtractor}
 */
public abstract class DefaultStreamExtractorTest extends DefaultExtractorTest<StreamExtractor>
        implements BaseStreamExtractorTest {

    public abstract StreamType expectedStreamType();
    public abstract String expectedUploaderName();
    public abstract String expectedUploaderUrl();
    public String expectedSubChannelName() { return ""; } // default: there is no subchannel
    public String expectedSubChannelUrl() { return ""; } // default: there is no subchannel
    public abstract List<String> expectedDescriptionContains(); // e.g. for full links
    public abstract long expectedLength();
    public long expectedTimestamp() { return 0; } // default: there is no timestamp
    public abstract long expectedViewCountAtLeast();
    @Nullable public abstract String expectedUploadDate(); // format: "yyyy-MM-dd HH:mm:ss.SSS"
    @Nullable public abstract String expectedTextualUploadDate();
    public abstract long expectedLikeCountAtLeast(); // return -1 if ratings are disabled
    public abstract long expectedDislikeCountAtLeast(); // return -1 if ratings are disabled
    public boolean expectedHasRelatedStreams() { return true; } // default: there are related videos
    public int expectedAgeLimit() { return StreamExtractor.NO_AGE_LIMIT; } // default: no limit
    @Nullable public String expectedErrorMessage() { return null; } // default: no error message
    public boolean expectedHasVideoStreams() { return true; } // default: there are video streams
    public boolean expectedHasAudioStreams() { return true; } // default: there are audio streams
    public boolean expectedHasSubtitles() { return true; } // default: there are subtitles streams
    @Nullable public String expectedDashMpdUrlContains() { return null; } // default: no dash mpd
    public boolean expectedHasFrames() { return true; } // default: there are frames
    public String expectedHost() { return ""; } // default: no host for centralized platforms
    public String expectedPrivacy() { return ""; } // default: no privacy policy available
    public String expectedCategory() { return ""; } // default: no category
    public String expectedLicence() { return ""; } // default: no licence
    public Locale expectedLanguageInfo() { return null; } // default: no language info available
    public List<String> expectedTags() { return Collections.emptyList(); } // default: no tags
    public String expectedSupportInfo() { return ""; } // default: no support info available

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
        assertFalse("description is empty", description.getContent().isEmpty());

        for (final String s : expectedDescriptionContains()) {
            assertThat(description.getContent(), containsString(s));
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
        assertAtLeast(expectedViewCountAtLeast(), extractor().getViewCount());
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
            assertAtLeast(expectedLikeCountAtLeast(), extractor().getLikeCount());
        }
    }

    @Test
    @Override
    public void testDislikeCount() throws Exception {
        if (expectedDislikeCountAtLeast() == -1) {
            assertEquals(-1, extractor().getDislikeCount());
        } else {
            assertAtLeast(expectedDislikeCountAtLeast(), extractor().getDislikeCount());
        }
    }

    @Test
    @Override
    public void testRelatedStreams() throws Exception {
        final StreamInfoItemsCollector relatedStreams = extractor().getRelatedStreams();

        if (expectedHasRelatedStreams()) {
            assertNotNull(relatedStreams);
            defaultTestListOfItems(extractor().getService(), relatedStreams.getItems(),
                    relatedStreams.getErrors());
        } else {
            assertNull(relatedStreams);
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
                assertIsSecureUrl(stream.getUrl());
                assertFalse(stream.getResolution().isEmpty());

                final int formatId = stream.getFormatId();
                // see MediaFormat: video stream formats range from 0 to 0x100
                assertTrue("format id does not fit a video stream: " + formatId,
                        0 <= formatId && formatId < 0x100);
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
                assertIsSecureUrl(stream.getUrl());

                final int formatId = stream.getFormatId();
                // see MediaFormat: video stream formats range from 0x100 to 0x1000
                assertTrue("format id does not fit an audio stream: " + formatId,
                        0x100 <= formatId && formatId < 0x1000);
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
                assertIsSecureUrl(stream.getUrl());

                final int formatId = stream.getFormatId();
                // see MediaFormat: video stream formats range from 0x1000 to 0x10000
                assertTrue("format id does not fit a subtitles stream: " + formatId,
                        0x1000 <= formatId && formatId < 0x10000);
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
            assertThat(extractor().getDashMpdUrl(), containsString(expectedDashMpdUrlContains()));
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
}
