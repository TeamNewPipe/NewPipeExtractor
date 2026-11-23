package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.stream.ContentAvailability;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriDeliverySource;
import org.schabi.newpipe.extractor.stream.interfaces.AudioStream;
import org.schabi.newpipe.extractor.stream.interfaces.ManifestStream;
import org.schabi.newpipe.extractor.stream.interfaces.Stream;
import org.schabi.newpipe.extractor.stream.interfaces.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.interfaces.VideoStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseAudioStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseManifestStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseMuxedStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseSubtitlesStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseVideoStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertContains;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmpty;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEqualsOrderIndependent;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreaterOrEqual;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsValidUrl;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertNotEmpty;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestImageCollection;
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
    public boolean expectedHasMuxedStreams() { return true; } // default: there are muxed streams (like video streams with audio)
    public boolean expectedHasDashGlobalManifestStreams() { return false; } // default: no DASH "global" MPD
    public boolean expectedHasHlsGlobalManifestStreams() { return false; } // default: no HLS "master" manifest
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
    public ContentAvailability expectedContentAvailability() { return ContentAvailability.UNKNOWN; } // default: unknown content availability
    public boolean expectedHasLiveChat() { return false; } // default: no live chat available

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
    public void testUploaderAvatars() throws Exception {
        defaultTestImageCollection(extractor().getUploaderAvatars());
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
    public void testSubChannelAvatars() throws Exception {
        if (expectedSubChannelName().isEmpty() && expectedSubChannelUrl().isEmpty()) {
            // this stream has no subchannel
            assertEmpty(extractor().getSubChannelAvatars());
        } else {
            // this stream has a subchannel
            defaultTestImageCollection(extractor().getSubChannelAvatars());
        }
    }

    @Test
    @Override
    public void testThumbnails() throws Exception {
        defaultTestImageCollection(extractor().getThumbnails());
    }

    @Test
    @Override
    public void testDescription() throws Exception {
        final Description description = extractor().getDescription();
        assertNotNull(description);

        if (expectedDescriptionIsEmpty()) {
            assertTrue(description.content().isEmpty(), "description is not empty");
        } else {
            assertFalse(description.content().isEmpty(), "description is empty");
        }

        for (final String s : expectedDescriptionContains()) {
            ExtractorAsserts.assertContains(s, description.content());
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
        final var dateWrapper = extractor().getUploadDate();
        final var expectedDate = expectedUploadDate();

        if (expectedDate == null) {
            assertNull(dateWrapper);
        } else {
            assertNotNull(dateWrapper);

            final var expectedDateTime = LocalDateTime.parse(expectedDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

            assertEquals(expectedDateTime, dateWrapper.getLocalDateTime(ZoneOffset.UTC));
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
    public void testStreams() throws Exception {
        final List<Stream> streams = extractor().getStreams();
        assertNotNull(streams);

        try {
            // No null elements should not be returned in the list
            assertFalse(streams.contains(null));
        } catch (final ClassCastException | NullPointerException ignored) {
            // If the list doesn't support null elements, then these exceptions may be thrown
            // Ignore them, as it is an expected behavior
        }

        testVideoStreams(streams.stream()
                .filter(BaseVideoStream.class::isInstance)
                .map(BaseVideoStream.class::cast)
                .toList());

        testAudioStreams(streams.stream()
                .filter(BaseAudioStream.class::isInstance)
                .map(BaseAudioStream.class::cast)
                .toList());

        testSubtitlesStreams(streams.stream()
                .filter(BaseSubtitlesStream.class::isInstance)
                .map(BaseSubtitlesStream.class::cast)
                .toList());

        testMuxedStreams(streams.stream()
                .filter(BaseMuxedStream.class::isInstance)
                .map(BaseMuxedStream.class::cast)
                .toList());

        testManifests(streams.stream()
                .filter(BaseManifestStream.class::isInstance)
                .map(BaseManifestStream.class::cast)
                .toList());
    }

    protected void testVideoStreams(@Nonnull final List<BaseVideoStream> videoStreams) {
        if (expectedHasVideoStreams()) {
            assertFalse(videoStreams.isEmpty());
            testVideoStreamsInternal(videoStreams);
        } else {
            assertTrue(videoStreams.isEmpty());
        }
    }

    private void testVideoStreamsInternal(@Nonnull final List<BaseVideoStream> videoStreams) {
        videoStreams.forEach(videoStream -> {
            assertNotNull(videoStream.getMediaFormat());
            assertNotNull(videoStream.getProjectionType());

            if (videoStream instanceof VideoStream castedVideoStream) {
                final DeliverySource deliverySource = castedVideoStream.getDeliverySource();
                assertNotNull(deliverySource);

                if (deliverySource instanceof UriDeliverySource uriDeliverySource) {
                    assertNotNull(uriDeliverySource.getUri());
                    assertNotNull(uriDeliverySource.getAlternateUris());
                }

                assertNotNull(castedVideoStream.getStreamingProtocol());
            }
        });
    }

    protected void testAudioStreams(@Nonnull final List<BaseAudioStream> audioStreams) {
        if (expectedHasAudioStreams()) {
            assertFalse(audioStreams.isEmpty());
            testAudioStreamsInternal(audioStreams);
        } else {
            assertTrue(audioStreams.isEmpty());
        }
    }

    protected void testAudioStreamsInternal(@Nonnull final List<BaseAudioStream> audioStreams) {
        audioStreams.forEach(audioStream -> {
            assertNotNull(audioStream.getMediaFormat());

            if (audioStream instanceof AudioStream castedAudioStream) {
                final DeliverySource deliverySource = castedAudioStream.getDeliverySource();
                assertNotNull(deliverySource);

                if (deliverySource instanceof UriDeliverySource uriDeliverySource) {
                    assertNotNull(uriDeliverySource.getUri());
                    assertNotNull(uriDeliverySource.getAlternateUris());
                }

                assertNotNull(castedAudioStream.getStreamingProtocol());
            }
        });
    }

    protected void testSubtitlesStreams(@Nonnull final List<BaseSubtitlesStream> subtitlesStreams) {
        if (expectedHasSubtitles()) {
            assertFalse(subtitlesStreams.isEmpty());
            testSubtitlesStreamsInternal(subtitlesStreams);
        } else {
            assertTrue(subtitlesStreams.isEmpty());
        }
    }

    private void testSubtitlesStreamsInternal(
            @Nonnull final List<BaseSubtitlesStream> subtitlesStreams) {
        subtitlesStreams.forEach(subtitlesStream -> {
            assertNotNull(subtitlesStream.getMediaFormat());
            assertNotNull(subtitlesStream.getTranslations());

            if (subtitlesStream instanceof SubtitlesStream castedSubtitlesStream) {
                final DeliverySource deliverySource = castedSubtitlesStream.getDeliverySource();
                assertNotNull(deliverySource);

                if (deliverySource instanceof UriDeliverySource uriDeliverySource) {
                    assertNotNull(uriDeliverySource.getUri());
                    assertNotNull(uriDeliverySource.getAlternateUris());
                }

                assertNotNull(castedSubtitlesStream.getStreamingProtocol());
            }
        });
    }

    protected void testMuxedStreams(@Nonnull final List<BaseMuxedStream> muxedStreams) {
        if (expectedHasMuxedStreams()) {
            assertFalse(muxedStreams.isEmpty());
            testMuxedStreamsInternal(muxedStreams);
        } else {
            assertTrue(muxedStreams.isEmpty());
        }
    }

    private void testMuxedStreamsInternal(@Nonnull final List<BaseMuxedStream> muxedStreams) {
        muxedStreams.forEach(muxedStream -> {
            final List<? extends BaseVideoStream> demuxedVideoStreamsInMuxedStream =
                    muxedStream.getDemuxedVideoStreams();
            if (demuxedVideoStreamsInMuxedStream != null) {
                testVideoStreamsInternal(
                        (List<BaseVideoStream>) demuxedVideoStreamsInMuxedStream);
            }

            final List<? extends BaseAudioStream> demuxedAudioStreamsInMuxedStream =
                    muxedStream.getDemuxedAudioStreams();
            if (demuxedAudioStreamsInMuxedStream != null) {
                testAudioStreamsInternal(
                        (List<BaseAudioStream>) demuxedAudioStreamsInMuxedStream);
            }

            final List<? extends BaseSubtitlesStream> demuxedSubtitlesStreamsInMuxedStream =
                    muxedStream.getDemuxedSubtitlesStreams();
            if (demuxedSubtitlesStreamsInMuxedStream != null) {
                testSubtitlesStreamsInternal(
                        (List<BaseSubtitlesStream>) demuxedSubtitlesStreamsInMuxedStream);
            }

            final List<? extends BaseMuxedStream> muxedStreamsInMuxedStream =
                    muxedStream.getMuxedStreams();
            if (muxedStreamsInMuxedStream != null) {
                testMuxedStreamsInternal((List<BaseMuxedStream>) muxedStreamsInMuxedStream);
            }
        });
    }

    protected void testManifests(@Nonnull final List<BaseManifestStream> manifestStreams) {
        boolean hasDashGlobalManifest = false;
        boolean hasHlsGlobalManifest = false;

        for (final BaseManifestStream manifestStream : manifestStreams) {
            if (manifestStream instanceof ManifestStream castedManifestStream) {
                final StreamingProtocol streamingProtocol =
                        castedManifestStream.getStreamingProtocol();
                assertNotNull(streamingProtocol);

                if (streamingProtocol == StreamingProtocol.DASH) {
                    hasDashGlobalManifest = true;
                } else if (streamingProtocol == StreamingProtocol.HLS) {
                    hasHlsGlobalManifest = true;
                }

                final DeliverySource deliverySource = castedManifestStream.getDeliverySource();
                assertNotNull(deliverySource);

                if (deliverySource instanceof UriDeliverySource uriDeliverySource) {
                    assertNotNull(uriDeliverySource.getUri());
                    assertNotNull(uriDeliverySource.getAlternateUris());
                }

                assertNotNull(castedManifestStream.getStreamingProtocol());
            }
        }

        if (expectedHasDashGlobalManifestStreams()) {
            assertTrue(hasDashGlobalManifest);
        } else {
            assertFalse(hasDashGlobalManifest);
        }

        if (expectedHasHlsGlobalManifestStreams()) {
            assertTrue(hasHlsGlobalManifest);
        } else {
            assertFalse(hasHlsGlobalManifest);
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
    @Override
    public void testStreamSegmentsCount() throws Exception {
        if (expectedStreamSegmentsCount() >= 0) {
            assertEquals(expectedStreamSegmentsCount(), extractor().getStreamSegments().size());
        }
    }

    /**
     * @see DefaultSearchExtractorTest#testMetaInfo()
     */
    @Test
    @Override
    public void testMetaInfo() throws Exception {
        final List<MetaInfo> metaInfoList = extractor().getMetaInfo();
        final List<MetaInfo> expectedMetaInfoList = expectedMetaInfo();

        final List<Executable> assertions = new ArrayList<>();
        for (final MetaInfo expectedMetaInfo : expectedMetaInfoList) {
            final List<String> texts = metaInfoList.stream()
                    .map(metaInfo -> metaInfo.getContent()
                            .content())
                    .toList();
            final List<String> titles = metaInfoList.stream()
                    .map(MetaInfo::getTitle)
                    .toList();
            final List<URL> urls = metaInfoList.stream()
                    .flatMap(info -> info.getUrls()
                            .stream())
                    .toList();
            final List<String> urlTexts = metaInfoList.stream()
                    .flatMap(info -> info.getUrlTexts()
                            .stream())
                    .toList();

            assertions.add(() -> assertContains(expectedMetaInfo.getContent()
                    .content(), texts));
            assertions.add(() -> assertContains(expectedMetaInfo.getTitle(), titles));

            for (final String expectedUrlText : expectedMetaInfo.getUrlTexts()) {
                assertions.add(() -> assertContains(expectedUrlText, urlTexts));
            }
            for (final URL expectedUrl : expectedMetaInfo.getUrls()) {
                assertions.add(() -> assertContains(expectedUrl, urls));
            }
        }
        assertAll(assertions);
    }

    @Test
    @Override
    public void testContentAvailability() throws Exception {
        assertEquals(expectedContentAvailability(), extractor().getContentAvailability());
    }

    @Test
    public void testHasLiveChat() throws Exception {
        assertEquals(expectedHasLiveChat(), extractor().hasLiveChat());
    }
}
