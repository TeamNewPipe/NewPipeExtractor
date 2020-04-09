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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Nullable;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertAtLeast;
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
    public abstract List<String> expectedDescriptionContains(); // e.g. for full links
    public abstract long expectedLength();
    public long expectedTimestamp() { return 0; }; // default: there is no timestamp
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
    public boolean expectedHasFrames() { return true; } // default: there are frames

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
    public void testThumbnailUrl() throws Exception {
        assertIsSecureUrl(extractor().getThumbnailUrl());
    }

    @Test
    @Override
    public void testDescription() throws Exception {
        final Description description = extractor().getDescription();
        assertNotNull(description);
        assertFalse("description is empty", description.getContent().isEmpty());

        for (String s : expectedDescriptionContains()) {
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

            final Calendar expectedDate = Calendar.getInstance();
            final Calendar actualDate = dateWrapper.date();
            expectedDate.setTimeZone(TimeZone.getTimeZone("GMT"));
            actualDate.setTimeZone(TimeZone.getTimeZone("GMT"));

            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            expectedDate.setTime(sdf.parse(expectedUploadDate()));
            assertEquals(expectedDate, actualDate);
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
        List<VideoStream> videoStreams = extractor().getVideoStreams();
        final List<VideoStream> videoOnlyStreams = extractor().getVideoOnlyStreams();
        assertNotNull(videoStreams);
        assertNotNull(videoOnlyStreams);
        videoStreams.addAll(videoOnlyStreams);

        if (expectedHasVideoStreams()) {
            assertFalse(videoStreams.isEmpty());

            for (VideoStream stream : videoStreams) {
                assertIsSecureUrl(stream.getUrl());
                assertFalse(stream.getResolution().isEmpty());

                int formatId = stream.getFormatId();
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

            for (AudioStream stream : audioStreams) {
                assertIsSecureUrl(stream.getUrl());

                int formatId = stream.getFormatId();
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
        List<SubtitlesStream> subtitles = extractor().getSubtitlesDefault();
        assertNotNull(subtitles);

        if (expectedHasSubtitles()) {
            assertFalse(subtitles.isEmpty());

            for (SubtitlesStream stream : subtitles) {
                assertIsSecureUrl(stream.getUrl());

                int formatId = stream.getFormatId();
                assertTrue("format id does not fit an audio stream: " + formatId,
                        0x1000 <= formatId && formatId < 0x10000);
            }
        } else {
            assertTrue(subtitles.isEmpty());

            MediaFormat[] formats = {MediaFormat.VTT, MediaFormat.TTML, MediaFormat.TRANSCRIPT1,
                    MediaFormat.TRANSCRIPT2, MediaFormat.TRANSCRIPT3, MediaFormat.SRT};
            for (MediaFormat format : formats) {
                subtitles = extractor().getSubtitles(format);
                assertNotNull(subtitles);
                assertTrue(subtitles.isEmpty());
            }
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
}
