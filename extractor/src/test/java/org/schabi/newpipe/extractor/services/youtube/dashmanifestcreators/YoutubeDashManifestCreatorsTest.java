package org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreater;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreaterOrEqual;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsValidUrl;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertNotBlank;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.ADAPTATION_SET;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.AUDIO_CHANNEL_CONFIGURATION;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.BASE_URL;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.INITIALIZATION;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.MPD;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.PERIOD;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.REPRESENTATION;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.ROLE;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.SEGMENT_BASE;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.InitYoutubeTest;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.services.youtube.stream.Itag;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeBaseAudioDemuxedStream;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeBaseDemuxedStream;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeBaseVideoDemuxedStream;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeUriAudioStream;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeUriVideoStream;
import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.HttpDeliverySource;
import org.schabi.newpipe.extractor.stream.interfaces.Stream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Test for YouTube DASH manifest creators.
 *
 * <p>
 * Tests the generation of OTF and progressive manifests.
 * </p>
 *
 * <p>
 * We cannot test the generation of DASH manifests for ended livestreams because these videos will
 * be re-encoded as normal videos later, so we can't use a specific video.
 * </p>
 *
 * <p>
 * The generation of DASH manifests for OTF streams, which can be tested, uses a video licenced
 * under the Creative Commons Attribution licence (reuse allowed): {@code A New Era of Open?
 * COVID-19 and the Pursuit for Equitable Solutions} (<a href=
 * "https://www.youtube.com/watch?v=DJ8GQUNUXGM">https://www.youtube.com/watch?v=DJ8GQUNUXGM</a>)
 * </p>
 *
 * <p>
 * We couldn't use mocks for these tests because the streaming URLs needs to fetched and the IP
 * address used to get these URLs is required (used as a param in the URLs; without it, video
 * servers return 403/Forbidden HTTP response code).
 * </p>
 *
 * <p>
 * So the real downloader will be used everytime on this test class.
 * </p>
 */
class YoutubeDashManifestCreatorsTest extends DefaultSimpleExtractorTest<YoutubeStreamExtractor>
    implements InitYoutubeTest {
    // Setting a higher number may lead to anti bot sign ins
    private static final int MAX_STREAMS_TO_TEST_PER_STREAM_TYPE = 2;
    private static final String URL = "https://www.youtube.com/watch?v=DJ8GQUNUXGM";
    private long videoLength;

    @Override
    protected YoutubeStreamExtractor createExtractor() throws Exception {
        return (YoutubeStreamExtractor) YouTube.getStreamExtractor(URL);
    }

    @Override
    protected void fetchExtractor(final YoutubeStreamExtractor extractor) throws Exception {
        super.fetchExtractor(extractor);
        videoLength = extractor.getLength();
    }

    @Test
    void testProgressiveStreams() throws Exception {
        for (final YoutubeBaseDemuxedStream stream : assertFilterStreams(extractor().getStreams())) {
            final String url;
            if (stream instanceof YoutubeUriVideoStream videoStream) {
                url = ((HttpDeliverySource) videoStream.getDeliverySource()).getUri().getUri();
            } else {
                url = ((HttpDeliverySource) (((YoutubeUriAudioStream) stream).getDeliverySource()))
                        .getUri().getUri();
            }

            final String manifest =
                    YoutubeProgressiveDashManifestCreator.fromProgressiveStreamingUrl(
                            url, stream, videoLength);
            assertNotBlank(manifest);

            assertManifestGenerated(
                    manifest,
                    stream,
                    document -> assertAll(
                            () -> assertBaseUrlElement(document),
                            () -> assertSegmentBaseElement(document, stream.getItag()),
                            () -> assertInitializationElement(document, stream.getItag())
                    )
            );
        }
    }

    @Nonnull
    private List<YoutubeBaseDemuxedStream> assertFilterStreams(@Nonnull final List<Stream> streams) {

        final List<YoutubeBaseDemuxedStream> audioStreams = streams.stream()
                .filter(YoutubeUriAudioStream.class::isInstance)
                .map(YoutubeUriAudioStream.class::cast)
                .limit(MAX_STREAMS_TO_TEST_PER_STREAM_TYPE)
                .collect(Collectors.toUnmodifiableList());
        final List<YoutubeBaseDemuxedStream> videoStreams = streams.stream()
                .filter(YoutubeUriVideoStream.class::isInstance)
                .map(YoutubeUriVideoStream.class::cast)
                .limit(MAX_STREAMS_TO_TEST_PER_STREAM_TYPE)
                .collect(Collectors.toUnmodifiableList());

        final List<YoutubeBaseDemuxedStream> filteredStreams = new ArrayList<>();
        filteredStreams.addAll(audioStreams);
        filteredStreams.addAll(videoStreams);
        return filteredStreams;
    }

    private void assertManifestGenerated(final String dashManifest,
                                         final YoutubeBaseDemuxedStream stream,
                                         final Consumer<Document> additionalAsserts)
            throws Exception {

        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(new InputSource(
                new StringReader(dashManifest)));

        assertAll(
                () -> assertMpdElement(document),
                () -> assertPeriodElement(document),
                () -> assertAdaptationSetElement(document, stream),
                () -> assertRoleElement(document, stream),
                () -> assertRepresentationElement(document, stream),
                () -> {
                    if (stream instanceof YoutubeBaseAudioDemuxedStream audioStream) {
                        assertAudioChannelConfigurationElement(document, audioStream);
                    }
                },
                () -> additionalAsserts.accept(document)
        );
    }

    private void assertMpdElement(@Nonnull final Document document) {
        final Element element = (Element) document.getElementsByTagName(MPD).item(0);
        assertNotNull(element);
        assertNull(element.getParentNode().getNodeValue());

        final String mediaPresentationDuration = element.getAttribute("mediaPresentationDuration");
        assertNotNull(mediaPresentationDuration);
        assertTrue(mediaPresentationDuration.startsWith("PT"));
    }

    private void assertPeriodElement(@Nonnull final Document document) {
        assertGetElement(document, PERIOD, MPD);
    }

    private void assertAdaptationSetElement(@Nonnull final Document document,
                                            @Nonnull final YoutubeBaseDemuxedStream stream) {
        final Element element = assertGetElement(document, ADAPTATION_SET, PERIOD);
        final String mimeType;
        if (stream instanceof YoutubeBaseAudioDemuxedStream audioStream) {
            final Locale itagAudioLocale = audioStream.getLocale();
            if (itagAudioLocale != null) {
                assertAttrEquals(itagAudioLocale.getLanguage(), element, "lang");
            }

            mimeType = audioStream.getMediaFormat().mimeType;
        } else {
            mimeType = ((YoutubeBaseVideoDemuxedStream) stream).getMediaFormat().mimeType;
        }

        assertAttrEquals(mimeType, element, "mimeType");
    }

    private void assertRoleElement(@Nonnull final Document document,
                                   @Nonnull final YoutubeBaseDemuxedStream stream) {
        final Element element = assertGetElement(document, ROLE, ADAPTATION_SET);

        final String expect;
        if (stream instanceof YoutubeBaseVideoDemuxedStream) {
            expect = "main";
        } else {
            final AudioTrackType audioTrackType =
                    ((YoutubeBaseAudioDemuxedStream) stream).getTrackType();
            if (audioTrackType == null) {
                expect = "main";
            } else {
                expect = switch (audioTrackType) {
                    case ORIGINAL -> "main";
                    case DUBBED -> "dub";
                    case DESCRIPTIVE -> "description";
                    default -> "alternate";
                };
            }
        }

        assertAttrEquals(expect, element, "value");
    }

    private void assertRepresentationElement(@Nonnull final Document document,
                                             @Nonnull final YoutubeBaseDemuxedStream stream) {
        final Element element = assertGetElement(document, REPRESENTATION, ADAPTATION_SET);
        final Integer bitrate;
        final String codec;

        if (stream instanceof YoutubeBaseVideoDemuxedStream videoStream) {
            bitrate = videoStream.getAverageBitrate();
            codec = videoStream.getCodec();

            final Integer fps = videoStream.getFramerate();
            if (fps != null) {
                assertAttrEquals(String.valueOf(fps), element, "frameRate");
            }

            final Integer height = videoStream.getHeight();
            assertNotNull(height);
            assertAttrEquals(String.valueOf(height), element, "height");

            final Integer width = videoStream.getWidth();
            assertNotNull(width);
            assertAttrEquals(String.valueOf(width), element, "width");
        } else {
            final YoutubeBaseAudioDemuxedStream audioStream =
                    (YoutubeBaseAudioDemuxedStream) stream;
            bitrate = audioStream.getAverageBitrate();
            codec = audioStream.getCodec();
        }

        assertNotNull(bitrate);
        assertNotNull(codec);
        assertAttrEquals(String.valueOf(bitrate), element, "bandwidth");
        assertAttrEquals(codec, element, "codecs");

        assertAttrEquals(stream.getId(), element, "id");
    }

    private void assertAudioChannelConfigurationElement(
            @Nonnull final Document document,
            @Nonnull final YoutubeBaseAudioDemuxedStream audioStream) {
        final Element element = assertGetElement(document, AUDIO_CHANNEL_CONFIGURATION,
                REPRESENTATION);
        final Integer channelsCount = audioStream.getChannelsCount();
        assertNotNull(channelsCount);
        assertAttrEquals(String.valueOf(audioStream.getChannelsCount()), element, "value");
    }

    private void assertBaseUrlElement(@Nonnull final Document document) {
        final Element element = assertGetElement(document, BASE_URL, REPRESENTATION);
        assertIsValidUrl(element.getTextContent());
    }

    private void assertSegmentBaseElement(@Nonnull final Document document,
                                          @Nonnull final Itag itag) {
        final Element element = assertGetElement(document, SEGMENT_BASE, REPRESENTATION);
        // These attributes should not be null for progressive streams
        assertNotNull(itag.indexRangeStart);
        assertNotNull(itag.indexRangeEnd);
        assertRangeEquals(itag.indexRangeStart, itag.indexRangeEnd, element, "indexRange");
    }

    private void assertInitializationElement(@Nonnull final Document document,
                                             @Nonnull final Itag itag) {
        // These attributes should not be null for progressive streams
        assertNotNull(itag.initRangeStart);
        assertNotNull(itag.initRangeEnd);
        final Element element = assertGetElement(document, INITIALIZATION, SEGMENT_BASE);
        assertRangeEquals(itag.initRangeStart, itag.initRangeEnd, element, "range");
    }


    private void assertAttrEquals(final String expected,
                                  @Nonnull final Element element,
                                  final String attribute) {
        final String actual = element.getAttribute(attribute);
        assertAll(
                () -> assertNotBlank(actual),
                () -> assertEquals(expected, actual)
        );
    }

    private void assertRangeEquals(final int expectedStart,
                                   final int expectedEnd,
                                   @Nonnull final Element element,
                                   final String attribute) {
        final String range = element.getAttribute(attribute);
        assertNotBlank(range);
        final String[] rangeParts = range.split("-");
        assertEquals(2, rangeParts.length);

        final int actualStart = Integer.parseInt(rangeParts[0]);
        final int actualEnd = Integer.parseInt(rangeParts[1]);

        assertAll(
                () -> assertGreaterOrEqual(0, actualStart),
                () -> assertEquals(expectedStart, actualStart),
                () -> assertGreater(0, actualEnd),
                () -> assertEquals(expectedEnd, actualEnd)
        );
    }

    @Nonnull
    private Element assertGetElement(@Nonnull final Document document,
                                     final String tagName,
                                     final String expectedParentTagName) {

        final Element element = (Element) document.getElementsByTagName(tagName).item(0);
        assertNotNull(element);
        assertTrue(element.getParentNode().isEqualNode(
                document.getElementsByTagName(expectedParentTagName).item(0)),
                "Element with tag name \"" + tagName + "\" does not have a parent node"
                + " with tag name \"" + expectedParentTagName + "\"");
        return element;
    }
}
