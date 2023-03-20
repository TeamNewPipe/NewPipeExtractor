package org.schabi.newpipe.extractor.services.youtube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.CreationException;
import org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeOtfDashManifestCreator;
import org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeProgressiveDashManifestCreator;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Stream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.SEGMENT_TEMPLATE;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.SEGMENT_TIMELINE;
import static org.schabi.newpipe.extractor.utils.Utils.isBlank;

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
class YoutubeDashManifestCreatorsTest {
    // Setting a higher number may let Google video servers return 403s
    private static final int MAX_STREAMS_TO_TEST_PER_METHOD = 3;
    private static final String url = "https://www.youtube.com/watch?v=DJ8GQUNUXGM";
    private static YoutubeStreamExtractor extractor;
    private static long videoLength;

    @BeforeAll
    public static void setUp() throws Exception {
        YoutubeParsingHelper.resetClientVersionAndKey();
        YoutubeParsingHelper.setNumberGenerator(new Random(1));
        NewPipe.init(DownloaderTestImpl.getInstance());

        extractor = (YoutubeStreamExtractor) YouTube.getStreamExtractor(url);
        extractor.fetchPage();
        videoLength = extractor.getLength();
    }

    @Test
    void testOtfStreams() throws Exception {
        assertDashStreams(extractor.getVideoOnlyStreams());
        assertDashStreams(extractor.getAudioStreams());

        // no video stream with audio uses the DASH delivery method (YouTube OTF stream type)
        assertEquals(0, assertFilterStreams(extractor.getVideoStreams(),
                DeliveryMethod.DASH).size());
    }

    @Test
    void testProgressiveStreams() throws Exception {
        assertProgressiveStreams(extractor.getVideoOnlyStreams());
        assertProgressiveStreams(extractor.getAudioStreams());

        // we are not able to generate DASH manifests of video formats with audio
        assertThrows(CreationException.class,
                () -> assertProgressiveStreams(extractor.getVideoStreams()));
    }

    private void assertDashStreams(final List<? extends Stream> streams) throws Exception {

        for (final Stream stream : assertFilterStreams(streams, DeliveryMethod.DASH)) {
            //noinspection ConstantConditions
            final String manifest = YoutubeOtfDashManifestCreator.fromOtfStreamingUrl(
                    stream.getContent(), stream.getItagItem(), videoLength);
            assertNotBlank(manifest);

            assertManifestGenerated(
                    manifest,
                    stream.getItagItem(),
                    document -> assertAll(
                            () -> assertSegmentTemplateElement(document),
                            () -> assertSegmentTimelineAndSElements(document)
                    )
            );
        }
    }

    private void assertProgressiveStreams(final List<? extends Stream> streams) throws Exception {

        for (final Stream stream : assertFilterStreams(streams, DeliveryMethod.PROGRESSIVE_HTTP)) {
            //noinspection ConstantConditions
            final String manifest =
                    YoutubeProgressiveDashManifestCreator.fromProgressiveStreamingUrl(
                            stream.getContent(), stream.getItagItem(), videoLength);
            assertNotBlank(manifest);

            assertManifestGenerated(
                    manifest,
                    stream.getItagItem(),
                    document -> assertAll(
                            () -> assertBaseUrlElement(document),
                            () -> assertSegmentBaseElement(document, stream.getItagItem()),
                            () -> assertInitializationElement(document, stream.getItagItem())
                    )
            );
        }
    }

    @Nonnull
    private List<? extends Stream> assertFilterStreams(
            @Nonnull final List<? extends Stream> streams,
            final DeliveryMethod deliveryMethod) {

        final List<? extends Stream> filteredStreams = streams.stream()
                .filter(stream -> stream.getDeliveryMethod() == deliveryMethod)
                .limit(MAX_STREAMS_TO_TEST_PER_METHOD)
                .collect(Collectors.toList());

        assertAll(filteredStreams.stream()
                .flatMap(stream -> java.util.stream.Stream.of(
                        () -> assertNotBlank(stream.getContent()),
                        () -> assertNotNull(stream.getItagItem())
                ))
        );

        return filteredStreams;
    }

    private void assertManifestGenerated(final String dashManifest,
                                         final ItagItem itagItem,
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
                () -> assertAdaptationSetElement(document, itagItem),
                () -> assertRoleElement(document, itagItem),
                () -> assertRepresentationElement(document, itagItem),
                () -> {
                    if (itagItem.itagType.equals(ItagItem.ItagType.AUDIO)) {
                        assertAudioChannelConfigurationElement(document, itagItem);
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
                                            @Nonnull final ItagItem itagItem) {
        final Element element = assertGetElement(document, ADAPTATION_SET, PERIOD);
        assertAttrEquals(itagItem.getMediaFormat().getMimeType(), element, "mimeType");

        if (itagItem.itagType == ItagItem.ItagType.AUDIO) {
            final Locale itagAudioLocale = itagItem.getAudioLocale();
            if (itagAudioLocale != null) {
                assertAttrEquals(itagAudioLocale.getLanguage(), element, "lang");
            }
        }
    }

    private void assertRoleElement(@Nonnull final Document document,
                                   @Nonnull final ItagItem itagItem) {
        final Element element = assertGetElement(document, ROLE, ADAPTATION_SET);
        assertAttrEquals(itagItem.isDescriptiveAudio() ? "alternate" : "main", element, "value");
    }

    private void assertRepresentationElement(@Nonnull final Document document,
                                             @Nonnull final ItagItem itagItem) {
        final Element element = assertGetElement(document, REPRESENTATION, ADAPTATION_SET);

        assertAttrEquals(itagItem.getBitrate(), element, "bandwidth");
        assertAttrEquals(itagItem.getCodec(), element, "codecs");

        if (itagItem.itagType == ItagItem.ItagType.VIDEO_ONLY
                || itagItem.itagType == ItagItem.ItagType.VIDEO) {
            assertAttrEquals(itagItem.getFps(), element, "frameRate");
            assertAttrEquals(itagItem.getHeight(), element, "height");
            assertAttrEquals(itagItem.getWidth(), element, "width");
        }

        assertAttrEquals(itagItem.id, element, "id");
    }

    private void assertAudioChannelConfigurationElement(@Nonnull final Document document,
                                                        @Nonnull final ItagItem itagItem) {
        final Element element = assertGetElement(document, AUDIO_CHANNEL_CONFIGURATION,
                REPRESENTATION);
        assertAttrEquals(itagItem.getAudioChannels(), element, "value");
    }

    private void assertSegmentTemplateElement(@Nonnull final Document document) {
        final Element element = assertGetElement(document, SEGMENT_TEMPLATE, REPRESENTATION);

        final String initializationValue = element.getAttribute("initialization");
        assertIsValidUrl(initializationValue);
        assertTrue(initializationValue.endsWith("&sq=0"));

        final String mediaValue = element.getAttribute("media");
        assertIsValidUrl(mediaValue);
        assertTrue(mediaValue.endsWith("&sq=$Number$"));

        assertEquals("1", element.getAttribute("startNumber"));
    }

    private void assertSegmentTimelineAndSElements(@Nonnull final Document document) {
        final Element element = assertGetElement(document, SEGMENT_TIMELINE, SEGMENT_TEMPLATE);
        final NodeList childNodes = element.getChildNodes();
        assertGreater(0, childNodes.getLength());

        assertAll(IntStream.range(0, childNodes.getLength())
                .mapToObj(childNodes::item)
                .map(Element.class::cast)
                .flatMap(sElement -> java.util.stream.Stream.of(
                        () -> assertEquals("S", sElement.getTagName()),
                        () -> assertGreater(0, Integer.parseInt(sElement.getAttribute("d"))),
                        () -> {
                            final String rValue = sElement.getAttribute("r");
                            // A segment duration can or can't be repeated, so test the next segment
                            // if there is no r attribute
                            if (!isBlank(rValue)) {
                                assertGreater(0, Integer.parseInt(rValue));
                            }
                        }
                    )
                )
        );
    }

    private void assertBaseUrlElement(@Nonnull final Document document) {
        final Element element = assertGetElement(document, BASE_URL, REPRESENTATION);
        assertIsValidUrl(element.getTextContent());
    }

    private void assertSegmentBaseElement(@Nonnull final Document document,
                                          @Nonnull final ItagItem itagItem) {
        final Element element = assertGetElement(document, SEGMENT_BASE, REPRESENTATION);
        assertRangeEquals(itagItem.getIndexStart(), itagItem.getIndexEnd(), element, "indexRange");
    }

    private void assertInitializationElement(@Nonnull final Document document,
                                             @Nonnull final ItagItem itagItem) {
        final Element element = assertGetElement(document, INITIALIZATION, SEGMENT_BASE);
        assertRangeEquals(itagItem.getInitStart(), itagItem.getInitEnd(), element, "range");
    }


    private void assertAttrEquals(final int expected,
                                  @Nonnull final Element element,
                                  final String attribute) {

        final int actual = Integer.parseInt(element.getAttribute(attribute));
        assertAll(
                () -> assertGreater(0, actual),
                () -> assertEquals(expected, actual)
        );
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
