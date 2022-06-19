package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreater;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsValidUrl;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertNotBlank;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.ADAPTATION_SET;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.AUDIO_CHANNEL_CONFIGURATION;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.BASE_URL;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.INITIALIZATION;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.MPD;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.PERIOD;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.REPRESENTATION;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.ROLE;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.SEGMENT_BASE;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.SEGMENT_TEMPLATE;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.SEGMENT_TIMELINE;
import static org.schabi.newpipe.extractor.utils.Utils.isBlank;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.dashmanifestcreator.YoutubeOtfDashManifestCreator;
import org.schabi.newpipe.extractor.services.youtube.dashmanifestcreator.YoutubeProgressiveDashManifestCreator;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.streamdata.delivery.DASHManifestDeliveryData;
import org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreator;
import org.schabi.newpipe.extractor.streamdata.stream.AudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.BaseAudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.Stream;
import org.schabi.newpipe.extractor.streamdata.stream.VideoAudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.VideoStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
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
class YoutubeDashManifestCreatorsTest {
    // Setting a higher number may let Google video servers return 403s
    private static final int MAX_STREAMS_TO_TEST_PER_METHOD = 5;
    private static final String URL = "https://www.youtube.com/watch?v=DJ8GQUNUXGM";

    private static YoutubeStreamExtractor extractor;

    @BeforeAll
    public static void setUp() throws Exception {
        YoutubeTestsUtils.ensureStateless();
        // Has to be done with a real downloader otherwise because there are secondary requests when
        // building a DASHManifest which require valid requests with a real IP
        NewPipe.init(DownloaderTestImpl.getInstance());

        extractor = (YoutubeStreamExtractor) YouTube.getStreamExtractor(URL);
        extractor.fetchPage();
    }

    @Test
    void testVideoOnlyStreams() throws ExtractionException {
        final List<VideoStream> videoStreams = getDashStreams(extractor.getVideoOnlyStreams());
        assertTrue(videoStreams.size() > 0);
        checkDashStreams(videoStreams);
    }

    @Test
    void testVideoStreams() throws ExtractionException {
        final List<VideoAudioStream> videoAudioStreams = getDashStreams(extractor.getVideoStreams());
        assertEquals(0, videoAudioStreams.size(), "There should be no dash streams for video-audio streams");
    }

    @Test
    void testAudioStreams() throws ExtractionException {
        final List<AudioStream> audioStreams = getDashStreams(extractor.getAudioStreams());
        assertTrue(audioStreams.size() > 0);
        checkDashStreams(audioStreams);
    }

    private <S extends Stream<?>> List<S> getDashStreams(final List<S> streams) {
        return streams.stream()
                .filter(s -> s.deliveryData() instanceof DASHManifestDeliveryData)
                .collect(Collectors.toList());
    }

    private <S extends Stream<?>> void checkDashStreams(final Collection<S> streams) {
        assertAll(streams.stream()
                .limit(MAX_STREAMS_TO_TEST_PER_METHOD)
                .map(s ->
                        () -> checkManifest(s,
                                ((DASHManifestDeliveryData) s.deliveryData())
                                        .getCachedDashManifestAsString()))
        );
    }

    private void checkManifest(
            final Stream<?> stream,
            final String manifestAsString) throws Exception {
        assertNotBlank(manifestAsString, "Generated manifest string is blank");
        checkGeneratedManifest(manifestAsString, stream);
    }

    private void checkGeneratedManifest(
            final String dashManifest,
            final Stream<?> stream
    ) throws Exception {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document =
                documentBuilder.parse(new InputSource(new StringReader(dashManifest)));

        final List<Executable> asserts = new ArrayList<>(Arrays.asList(
                () -> assertMpdElement(document),
                () -> assertPeriodElement(document),
                () -> assertAdaptationSetElement(document),
                () -> assertRoleElement(document),
                () -> assertRepresentationElement(document, stream)
        ));

        if (stream instanceof BaseAudioStream) {
            asserts.add(() -> assertAudioChannelConfigurationElement(document));
        }

        final DashManifestCreator dashManifestCreator =
                ((DASHManifestDeliveryData) stream.deliveryData()).getDashManifestCreator();
        if (dashManifestCreator instanceof YoutubeOtfDashManifestCreator) {
            asserts.add(() -> assertSegmentTemplateElement(document));
            asserts.add(() -> assertSegmentTimelineAndSElements(document));
        } else if (dashManifestCreator instanceof YoutubeProgressiveDashManifestCreator) {
            asserts.add(() -> assertBaseUrlElement(document));
            asserts.add(() -> assertSegmentBaseElement(document));
            asserts.add(() -> assertInitializationElement(document));
        }

        assertAll(asserts);
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

    private void assertAdaptationSetElement(@Nonnull final Document document) {
        final Element element = assertGetElement(document, ADAPTATION_SET, PERIOD);
        assertAttrNotBlank(element, "mimeType");
    }

    private void assertRoleElement(@Nonnull final Document document) {
        assertGetElement(document, ROLE, ADAPTATION_SET);
    }

    private void assertRepresentationElement(@Nonnull final Document document,
                                             @Nonnull final Stream<?> stream) {
        final Element element = assertGetElement(document, REPRESENTATION, ADAPTATION_SET);

        assertAttrNotBlank(element, "bandwidth");
        assertAttrNotBlank(element, "codecs");

        if (stream instanceof VideoStream) {
            assertAttrNotBlank(element, "frameRate");
            assertAttrNotBlank(element, "height");
            assertAttrNotBlank(element, "width");
        }

    }

    private void assertAudioChannelConfigurationElement(@Nonnull final Document document) {
        final Element element = assertGetElement(document, AUDIO_CHANNEL_CONFIGURATION,
                REPRESENTATION);
        assertAttrNotBlank(element, "value");
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

    private void assertSegmentBaseElement(@Nonnull final Document document) {
        final Element element = assertGetElement(document, SEGMENT_BASE, REPRESENTATION);
        assertAttrNotBlank(element, "indexRange");
    }

    private void assertInitializationElement(@Nonnull final Document document) {
        final Element element = assertGetElement(document, INITIALIZATION, SEGMENT_BASE);
        assertAttrNotBlank(element, "range");
    }

    private void assertAttrNotBlank(@Nonnull final Element element, final String attribute) {
        assertNotBlank(element.getAttribute(attribute), "Attribute '" + attribute + "' is blank");
    }


    @Nonnull
    private Element assertGetElement(@Nonnull final Document document,
                                     final String tagName,
                                     final String expectedParentTagName) {

        final Element element = (Element) document.getElementsByTagName(tagName).item(0);
        assertNotNull(element, "Could not get first element with tagName=" + tagName);
        assertTrue(element.getParentNode().isEqualNode(
                document.getElementsByTagName(expectedParentTagName).item(0)),
                "Element with tag name \"" + tagName + "\" does not have a parent node"
                + " with tag name \"" + expectedParentTagName + "\"");
        return element;
    }
}
