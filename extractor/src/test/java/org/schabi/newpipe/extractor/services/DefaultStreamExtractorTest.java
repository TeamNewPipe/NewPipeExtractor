package org.schabi.newpipe.extractor.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEqualsOrderIndependent;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreaterOrEqual;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsValidUrl;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestListOfItems;
import static org.schabi.newpipe.extractor.stream.StreamExtractor.UNKNOWN_SUBSCRIBER_COUNT;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.Privacy;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.streamdata.delivery.DASHManifestDeliveryData;
import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.delivery.UrlBasedDeliveryData;
import org.schabi.newpipe.extractor.streamdata.stream.AudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.SubtitleStream;
import org.schabi.newpipe.extractor.streamdata.stream.VideoAudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.VideoStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Test for {@link StreamExtractor}
 */
public abstract class DefaultStreamExtractorTest extends DefaultExtractorTest<StreamExtractor>
        implements BaseStreamExtractorTest {

    public boolean expectedIsLive() { return false; }
    public boolean expectedIsAudioOnly() { return false; }
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
    public boolean expectedHasVideoOnlyStreams() { return true; }
    public boolean expectedHasVideoAndAudioStreams() { return true; }
    public boolean expectedHasAudioStreams() { return true; }
    public boolean expectedHasSubtitles() { return true; } // default: there are subtitles streams
    @Nullable public String expectedDashMpdUrlContains() { return null; } // default: no dash mpd
    public boolean expectedHasFrames() { return true; } // default: there are frames
    public String expectedHost() { return ""; } // default: no host for centralized platforms
    public Privacy expectedPrivacy() { return Privacy.PUBLIC; } // default: public
    public String expectedCategory() { return ""; } // default: no category
    public String expectedLicence() { return ""; } // default: no licence
    public Locale expectedLanguageInfo() { return null; } // default: no language info available
    public List<String> expectedTags() { return Collections.emptyList(); } // default: no tags
    public String expectedSupportInfo() { return ""; } // default: no support info available
    public int expectedStreamSegmentsCount() { return -1; } // return 0 or greater to test (default is -1 to ignore)
    public List<MetaInfo> expectedMetaInfo() throws MalformedURLException { return Collections.emptyList(); } // default: no metadata info available

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
    public void testVideoOnlyStreams() throws Exception {
        final List<VideoStream> videoOnlyStreams = extractor().getVideoOnlyStreams();
        assertNotNull(videoOnlyStreams);

        if (expectedHasVideoOnlyStreams()) {
            assertFalse(videoOnlyStreams.isEmpty());

            for (final VideoStream stream : videoOnlyStreams) {
                assertNotNull(stream.mediaFormat());
                assertNotNull(stream.videoQualityData());
                checkDeliveryData(stream.deliveryData());
            }
        } else {
            assertTrue(videoOnlyStreams.isEmpty());
        }
    }

    @Test
    @Override
    public void testVideoAudioStreams() throws Exception {
        final List<VideoAudioStream> videoAudioStreams = extractor().getVideoStreams();
        assertNotNull(videoAudioStreams);

        if (expectedHasVideoAndAudioStreams()) {
            assertFalse(videoAudioStreams.isEmpty());

            for (final VideoAudioStream stream : videoAudioStreams) {
                assertNotNull(stream.mediaFormat());
                assertNotNull(stream.videoQualityData());
                checkDeliveryData(stream.deliveryData());
            }
        } else {
            assertTrue(videoAudioStreams.isEmpty());
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
                assertNotNull(stream.mediaFormat());
                checkDeliveryData(stream.deliveryData());
            }
        } else {
            assertTrue(audioStreams.isEmpty());
        }
    }

    @Test
    @Override
    public void testSubtitles() throws Exception {
        final List<SubtitleStream> subtitles = extractor().getSubtitles();
        assertNotNull(subtitles);

        if (expectedHasSubtitles()) {
            assertFalse(subtitles.isEmpty());

            for (final SubtitleStream stream : subtitles) {
                assertNotNull(stream.languageCode());
                assertNotNull(stream.mediaFormat());
                checkDeliveryData(stream.deliveryData());
            }
        } else {
            assertTrue(subtitles.isEmpty());
        }
    }

    private void checkDeliveryData(final DeliveryData deliveryData) {
        if (deliveryData instanceof UrlBasedDeliveryData) {
            assertIsSecureUrl(((UrlBasedDeliveryData) deliveryData).url());
        } else if (deliveryData instanceof DASHManifestDeliveryData) {
            final DASHManifestDeliveryData dashManifestDD =
                    (DASHManifestDeliveryData) deliveryData;
            assertNotNull(dashManifestDD.getDashManifestCreator());
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

    @Test
    public void testIsLive() throws Exception {
        assertEquals(expectedIsLive(), extractor().isLive());
    }

    @Test
    public void testIsAudioOnly() throws Exception {
        assertEquals(expectedIsAudioOnly(), extractor().isAudioOnly());
    }
}
