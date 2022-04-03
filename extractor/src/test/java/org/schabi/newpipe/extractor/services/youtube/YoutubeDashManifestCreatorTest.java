package org.schabi.newpipe.extractor.services.youtube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.utils.Utils.isBlank;

/**
 * Test for {@link YoutubeDashManifestCreator}.
 *
 * <p>
 * We cannot test the generation of DASH manifests for ended livestreams because these videos will
 * be re-encoded as normal videos later, so we can't use a specific video.
 * </p>
 *
 * <p>
 * The generation of DASH manifests for OTF streams, which can be tested, uses a video licenced
 * under the Creative Commons Attribution licence (reuse allowed):
 * {@code https://www.youtube.com/watch?v=DJ8GQUNUXGM}
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
class YoutubeDashManifestCreatorTest {
    // Setting a higher number may let Google video servers return a lot of 403s
    private static final int MAXIMUM_NUMBER_OF_STREAMS_TO_TEST = 3;

    public static class TestGenerationOfOtfAndProgressiveManifests {
        private static final String url = "https://www.youtube.com/watch?v=DJ8GQUNUXGM";
        private static YoutubeStreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            YoutubeParsingHelper.setNumberGenerator(new Random(1));
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube.getStreamExtractor(url);
            extractor.fetchPage();
        }

        @Test
        void testOtfStreamsANewEraOfOpen() throws Exception {
            testStreams(DeliveryMethod.DASH,
                    extractor.getVideoOnlyStreams());
            testStreams(DeliveryMethod.DASH,
                    extractor.getAudioStreams());
            // This should not happen because there are no video stream with audio which use the
            // DASH delivery method (YouTube OTF stream type)
            try {
                testStreams(DeliveryMethod.DASH,
                        extractor.getVideoStreams());
            } catch (final Exception e) {
                assertEquals(YoutubeDashManifestCreator.YoutubeDashManifestCreationException.class,
                        e.getClass(), "The exception thrown was not the one excepted: "
                                + e.getClass().getName()
                                + "was thrown instead of YoutubeDashManifestCreationException");
            }
        }

        @Test
        void testProgressiveStreamsANewEraOfOpen() throws Exception {
            testStreams(DeliveryMethod.PROGRESSIVE_HTTP,
                    extractor.getVideoOnlyStreams());
            testStreams(DeliveryMethod.PROGRESSIVE_HTTP,
                    extractor.getAudioStreams());
            // This exception should be always thrown, as we are not able to generate DASH
            // manifests of video formats with audio
            final List<VideoStream> videoStreams = extractor.getVideoStreams();
            if (!videoStreams.isEmpty()) {
                assertThrows(YoutubeDashManifestCreator.YoutubeDashManifestCreationException.class,
                        () -> testStreams(DeliveryMethod.PROGRESSIVE_HTTP, videoStreams),
                        "The exception thrown for the generation of DASH manifests for YouTube "
                                + "progressive video streams with audio was not the one excepted");
            }
        }

        private void testStreams(@Nonnull final DeliveryMethod deliveryMethodToTest,
                                 @Nonnull final List<? extends Stream> streamList)
                throws Exception {
            int i = 0;
            final int streamListSize = streamList.size();
            final boolean isDeliveryMethodToTestProgressiveHttpDeliveryMethod =
                    deliveryMethodToTest == DeliveryMethod.PROGRESSIVE_HTTP;
            final long videoLength = extractor.getLength();

            // Test at most the first five streams we found
            while (i <= YoutubeDashManifestCreatorTest.MAXIMUM_NUMBER_OF_STREAMS_TO_TEST
                    && i < streamListSize) {
                final Stream stream = streamList.get(i);
                if (stream.getDeliveryMethod() == deliveryMethodToTest) {
                    final String baseUrl = stream.getContent();
                    assertFalse(isBlank(baseUrl), "The base URL of the stream is empty");

                    final ItagItem itagItem = stream.getItagItem();
                    assertNotNull(itagItem, "The itagItem is null");

                    final String dashManifest;
                    if (isDeliveryMethodToTestProgressiveHttpDeliveryMethod) {
                        dashManifest = YoutubeDashManifestCreator
                                .createDashManifestFromProgressiveStreamingUrl(baseUrl, itagItem,
                                        videoLength);
                    } else if (deliveryMethodToTest == DeliveryMethod.DASH) {
                        dashManifest = YoutubeDashManifestCreator
                                .createDashManifestFromOtfStreamingUrl(baseUrl, itagItem,
                                        videoLength);
                    } else {
                        throw new IllegalArgumentException(
                                "The delivery method provided is not the progressive HTTP or the DASH delivery method");
                    }
                    testManifestGenerated(dashManifest, itagItem,
                            isDeliveryMethodToTestProgressiveHttpDeliveryMethod);
                    assertFalse(isBlank(dashManifest), "The DASH manifest is null or empty: "
                            + dashManifest);
                }
                ++i;
            }
        }

        private void testManifestGenerated(final String dashManifest,
                                           @Nonnull final ItagItem itagItem,
                                           final boolean isAProgressiveStreamingUrl)
                throws Exception {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.parse(new InputSource(
                    new StringReader(dashManifest)));

            testMpdElement(document);
            testPeriodElement(document);
            testAdaptationSetElement(document, itagItem);
            testRoleElement(document);
            testRepresentationElement(document, itagItem);
            if (itagItem.itagType.equals(ItagItem.ItagType.AUDIO)) {
                testAudioChannelConfigurationElement(document, itagItem);
            }
            if (isAProgressiveStreamingUrl) {
                testBaseUrlElement(document);
                testSegmentBaseElement(document, itagItem);
                testInitializationElement(document, itagItem);
            } else {
                testSegmentTemplateElement(document);
                testSegmentTimelineAndSElements(document);
            }
        }

        private void testMpdElement(@Nonnull final Document document) {
            final Element mpdElement = (Element) document.getElementsByTagName("MPD")
                    .item(0);
            assertNotNull(mpdElement, "The MPD element doesn't exist");
            assertNull(mpdElement.getParentNode().getNodeValue(), "The MPD element has a parent element");

            final String mediaPresentationDurationValue = mpdElement
                    .getAttribute("mediaPresentationDuration");
            assertNotNull(mediaPresentationDurationValue,
                    "The value of the mediaPresentationDuration attribute is empty or the corresponding attribute doesn't exist");
            assertTrue(mediaPresentationDurationValue.startsWith("PT"),
                    "The mediaPresentationDuration attribute of the DASH manifest is not valid");
        }

        private void testPeriodElement(@Nonnull final Document document) {
            final Element periodElement = (Element) document.getElementsByTagName("Period")
                    .item(0);
            assertNotNull(periodElement, "The Period element doesn't exist");
            assertTrue(periodElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("MPD").item(0)),
                    "The MPD element doesn't contain a Period element");
        }

        private void testAdaptationSetElement(@Nonnull final Document document,
                                              @Nonnull final ItagItem itagItem) {
            final Element adaptationSetElement = (Element) document
                    .getElementsByTagName("AdaptationSet").item(0);
            assertNotNull(adaptationSetElement, "The AdaptationSet element doesn't exist");
            assertTrue(adaptationSetElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("Period").item(0)),
                    "The Period element doesn't contain an AdaptationSet element");

            final String mimeTypeDashManifestValue = adaptationSetElement
                    .getAttribute("mimeType");
            assertFalse(isBlank(mimeTypeDashManifestValue),
                    "The value of the mimeType attribute is empty or the corresponding attribute doesn't exist");

            final String mimeTypeItagItemValue = itagItem.getMediaFormat().getMimeType();
            assertFalse(isBlank(mimeTypeItagItemValue), "The mimeType of the ItagItem is empty");

            assertEquals(mimeTypeDashManifestValue, mimeTypeItagItemValue,
                    "The mimeType attribute of the DASH manifest (" + mimeTypeItagItemValue
                            + ") is not equal to the mimeType set in the ItagItem object ("
                            + mimeTypeItagItemValue + ")");
        }

        private void testRoleElement(@Nonnull final Document document) {
            final Element roleElement = (Element) document.getElementsByTagName("Role")
                    .item(0);
            assertNotNull(roleElement, "The Role element doesn't exist");
            assertTrue(roleElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("AdaptationSet").item(0)),
                    "The AdaptationSet element doesn't contain a Role element");
        }

        private void testRepresentationElement(@Nonnull final Document document,
                                               @Nonnull final ItagItem itagItem) {
            final Element representationElement = (Element) document
                    .getElementsByTagName("Representation").item(0);
            assertNotNull(representationElement, "The Representation element doesn't exist");
            assertTrue(representationElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("AdaptationSet").item(0)),
                    "The AdaptationSet element doesn't contain a Representation element");

            final String bandwidthDashManifestValue = representationElement
                    .getAttribute("bandwidth");
            assertFalse(isBlank(bandwidthDashManifestValue),
                    "The value of the bandwidth attribute is empty or the corresponding attribute doesn't exist");

            final int bandwidthDashManifest;
            try {
                bandwidthDashManifest = Integer.parseInt(bandwidthDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the bandwidth attribute is not an integer",
                        e);
            }
            assertTrue(bandwidthDashManifest > 0,
                    "The value of the bandwidth attribute is less than or equal to 0");

            final int bitrateItagItem = itagItem.getBitrate();
            assertTrue(bitrateItagItem > 0,
                    "The bitrate of the ItagItem is less than or equal to 0");

            assertEquals(bandwidthDashManifest, bitrateItagItem,
                    "The value of the bandwidth attribute of the DASH manifest ("
                            + bandwidthDashManifest
                            + ") is not equal to the bitrate value set in the ItagItem object ("
                            + bitrateItagItem + ")");

            final String codecsDashManifestValue = representationElement.getAttribute("codecs");
            assertFalse(isBlank(codecsDashManifestValue),
                    "The value of the codecs attribute is empty or the corresponding attribute doesn't exist");

            final String codecsItagItemValue = itagItem.getCodec();
            assertFalse(isBlank(codecsItagItemValue), "The codec of the ItagItem is empty");

            assertEquals(codecsDashManifestValue, codecsItagItemValue,
                    "The value of the codecs attribute of the DASH manifest ("
                            + codecsDashManifestValue
                            + ") is not equal to the codecs value set in the ItagItem object ("
                            + codecsItagItemValue + ")");

            if (itagItem.itagType == ItagItem.ItagType.VIDEO_ONLY
                    || itagItem.itagType == ItagItem.ItagType.VIDEO) {
                testVideoItagItemAttributes(representationElement, itagItem);
            }

            final String idDashManifestValue = representationElement.getAttribute("id");
            assertFalse(isBlank(idDashManifestValue),
                    "The value of the id attribute is empty or the corresponding attribute doesn't exist");

            final int idDashManifest;
            try {
                idDashManifest = Integer.parseInt(idDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the id attribute is not an integer",
                        e);
            }
            assertTrue(idDashManifest > 0, "The value of the id attribute is less than or equal to 0");

            final int idItagItem = itagItem.id;
            assertTrue(idItagItem > 0, "The id of the ItagItem is less than or equal to 0");
            assertEquals(idDashManifest, idItagItem,
                    "The value of the id attribute of the DASH manifest (" + idDashManifestValue
                            + ") is not equal to the id of the ItagItem object (" + idItagItem
                            + ")");
        }

        private void testVideoItagItemAttributes(@Nonnull final Element representationElement,
                                                 @Nonnull final ItagItem itagItem) {
            final String frameRateDashManifestValue = representationElement
                    .getAttribute("frameRate");
            assertFalse(isBlank(frameRateDashManifestValue),
                    "The value of the frameRate attribute is empty or the corresponding attribute doesn't exist");

            final int frameRateDashManifest;
            try {
                frameRateDashManifest = Integer.parseInt(frameRateDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the frameRate attribute is not an integer",
                        e);
            }
            assertTrue(frameRateDashManifest > 0,
                    "The value of the frameRate attribute is less than or equal to 0");

            final int fpsItagItem = itagItem.getFps();
            assertTrue(fpsItagItem > 0, "The fps of the ItagItem is unknown");

            assertEquals(frameRateDashManifest, fpsItagItem,
                    "The value of the frameRate attribute of the DASH manifest ("
                            + frameRateDashManifest
                            + ") is not equal to the frame rate value set in the ItagItem object ("
                            + fpsItagItem + ")");

            final String heightDashManifestValue = representationElement.getAttribute("height");
            assertFalse(isBlank(heightDashManifestValue),
                    "The value of the height attribute is empty or the corresponding attribute doesn't exist");

            final int heightDashManifest;
            try {
                heightDashManifest = Integer.parseInt(heightDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the height attribute is not an integer",
                        e);
            }
            assertTrue(heightDashManifest > 0,
                    "The value of the height attribute is less than or equal to 0");

            final int heightItagItem = itagItem.getHeight();
            assertTrue(heightItagItem > 0,
                    "The height of the ItagItem is less than or equal to 0");

            assertEquals(heightDashManifest, heightItagItem,
                    "The value of the height attribute of the DASH manifest ("
                            + heightDashManifest
                            + ") is not equal to the height value set in the ItagItem object ("
                            + heightItagItem + ")");

            final String widthDashManifestValue = representationElement.getAttribute("width");
            assertFalse(isBlank(widthDashManifestValue),
                    "The value of the width attribute is empty or the corresponding attribute doesn't exist");

            final int widthDashManifest;
            try {
                widthDashManifest = Integer.parseInt(widthDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the width attribute is not an integer",
                        e);
            }
            assertTrue(widthDashManifest > 0,
                    "The value of the width attribute is less than or equal to 0");

            final int widthItagItem = itagItem.getWidth();
            assertTrue(widthItagItem > 0, "The width of the ItagItem is less than or equal to 0");

            assertEquals(widthDashManifest, widthItagItem,
                    "The value of the width attribute of the DASH manifest (" + widthDashManifest
                            + ") is not equal to the width value set in the ItagItem object ("
                            + widthItagItem + ")");
        }

        private void testAudioChannelConfigurationElement(@Nonnull final Document document,
                                                          @Nonnull final ItagItem itagItem) {
            final Element audioChannelConfigurationElement = (Element) document
                    .getElementsByTagName("AudioChannelConfiguration").item(0);
            assertNotNull(audioChannelConfigurationElement,
                    "The AudioChannelConfiguration element doesn't exist");
            assertTrue(audioChannelConfigurationElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("Representation").item(0)),
                    "The Representation element doesn't contain an AudioChannelConfiguration element");

            final String audioChannelsDashManifestValue = audioChannelConfigurationElement
                    .getAttribute("value");
            assertFalse(isBlank(audioChannelsDashManifestValue),
                    "The value of the value attribute is empty or the corresponding attribute doesn't exist");

            final int audioChannelsDashManifest;
            try {
                audioChannelsDashManifest = Integer.parseInt(audioChannelsDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError(
                        "The number of audio channels (the value attribute) is not an integer",
                        e);
            }
            assertTrue(audioChannelsDashManifest > 0,
                    "The number of audio channels (the value attribute) is less than or equal to 0");

            final int audioChannelsItagItem = itagItem.getAudioChannels();
            assertTrue(audioChannelsItagItem > 0,
                    "The number of audio channels of the ItagItem is less than or equal to 0");

            assertEquals(audioChannelsDashManifest, audioChannelsItagItem,
                    "The value of the value attribute of the DASH manifest ("
                            + audioChannelsDashManifest
                            + ") is not equal to the number of audio channels set in the ItagItem object ("
                            + audioChannelsItagItem + ")");
        }

        private void testSegmentTemplateElement(@Nonnull final Document document) {
            final Element segmentTemplateElement = (Element) document
                    .getElementsByTagName("SegmentTemplate").item(0);
            assertNotNull(segmentTemplateElement, "The SegmentTemplate element doesn't exist");
            assertTrue(segmentTemplateElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("Representation").item(0)),
                    "The Representation element doesn't contain a SegmentTemplate element");

            final String initializationValue = segmentTemplateElement
                    .getAttribute("initialization");
            assertFalse(isBlank(initializationValue),
                    "The value of the initialization attribute is empty or the corresponding attribute doesn't exist");
            try {
                new URL(initializationValue);
            } catch (final MalformedURLException e) {
                throw new AssertionError("The value of the initialization attribute is not an URL",
                        e);
            }
            assertTrue(initializationValue.endsWith("&sq=0"),
                    "The value of the initialization attribute doesn't end with &sq=0");

            final String mediaValue = segmentTemplateElement.getAttribute("media");
            assertFalse(isBlank(mediaValue),
                    "The value of the media attribute is empty or the corresponding attribute doesn't exist");
            try {
                new URL(mediaValue);
            } catch (final MalformedURLException e) {
                throw new AssertionError("The value of the media attribute is not an URL",
                        e);
            }
            assertTrue(mediaValue.endsWith("&sq=$Number$"),
                    "The value of the media attribute doesn't end with &sq=$Number$");

            final String startNumberValue = segmentTemplateElement.getAttribute("startNumber");
            assertFalse(isBlank(startNumberValue),
                    "The value of the startNumber attribute is empty or the corresponding attribute doesn't exist");
            assertEquals("1", startNumberValue,
                    "The value of the startNumber attribute is not equal to 1");
        }

        private void testSegmentTimelineAndSElements(@Nonnull final Document document) {
            final Element segmentTimelineElement = (Element) document
                    .getElementsByTagName("SegmentTimeline").item(0);
            assertNotNull(segmentTimelineElement, "The SegmentTimeline element doesn't exist");
            assertTrue(segmentTimelineElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("SegmentTemplate").item(0)),
                    "The SegmentTemplate element doesn't contain a SegmentTimeline element");
            testSElements(segmentTimelineElement);
        }

        private void testSElements(@Nonnull final Element segmentTimelineElement) {
            final NodeList segmentTimelineElementChildren = segmentTimelineElement.getChildNodes();
            final int segmentTimelineElementChildrenLength = segmentTimelineElementChildren
                    .getLength();
            assertNotEquals(0, segmentTimelineElementChildrenLength,
                    "The DASH manifest doesn't have a segment element (S) in the SegmentTimeLine element");

            for (int i = 0; i < segmentTimelineElementChildrenLength; i++) {
                final Element sElement = (Element) segmentTimelineElement.getElementsByTagName("S")
                        .item(i);

                final String dValue = sElement.getAttribute("d");
                assertFalse(isBlank(dValue),
                        "The value of the duration of this segment (the d attribute of this S element) is empty or the corresponding attribute doesn't exist");

                final int d;
                try {
                    d = Integer.parseInt(dValue);
                } catch (final NumberFormatException e) {
                    throw new AssertionError("The value of the d attribute is not an integer", e);
                }
                assertTrue(d > 0, "The value of the d attribute is less than or equal to 0");

                final String rValue = sElement.getAttribute("r");
                // A segment duration can or can't be repeated, so test the next segment if there
                // is no r attribute
                if (!isBlank(rValue)) {
                    final int r;
                    try {
                        r = Integer.parseInt(dValue);
                    } catch (final NumberFormatException e) {
                        throw new AssertionError("The value of the r attribute is not an integer",
                                e);
                    }
                    assertTrue(r > 0, "The value of the r attribute is less than or equal to 0");
                }
            }
        }

        private void testBaseUrlElement(@Nonnull final Document document) {
            final Element baseURLElement = (Element) document
                    .getElementsByTagName("BaseURL").item(0);
            assertNotNull(baseURLElement, "The BaseURL element doesn't exist");
            assertTrue(baseURLElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("Representation").item(0)),
                    "The Representation element doesn't contain a BaseURL element");

            final String baseURLElementContentValue = baseURLElement
                    .getTextContent();
            assertFalse(isBlank(baseURLElementContentValue),
                    "The content of the BaseURL element is empty or the corresponding element has no content");

            try {
                new URL(baseURLElementContentValue);
            } catch (final MalformedURLException e) {
                throw new AssertionError("The content of the BaseURL element is not an URL", e);
            }
        }

        private void testSegmentBaseElement(@Nonnull final Document document,
                                            @Nonnull final ItagItem itagItem) {
            final Element segmentBaseElement = (Element) document
                    .getElementsByTagName("SegmentBase").item(0);
            assertNotNull(segmentBaseElement, "The SegmentBase element doesn't exist");
            assertTrue(segmentBaseElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("Representation").item(0)),
                    "The Representation element doesn't contain a SegmentBase element");

            final String indexRangeValue = segmentBaseElement
                    .getAttribute("indexRange");
            assertFalse(isBlank(indexRangeValue),
                    "The value of the indexRange attribute is empty or the corresponding attribute doesn't exist");
            final String[] indexRangeParts = indexRangeValue.split("-");
            assertEquals(2, indexRangeParts.length,
                    "The value of the indexRange attribute is not valid");

            final int dashManifestIndexStart;
            try {
                dashManifestIndexStart = Integer.parseInt(indexRangeParts[0]);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the indexRange attribute is not valid", e);
            }

            final int itagItemIndexStart = itagItem.getIndexStart();
            assertTrue(itagItemIndexStart > 0,
                    "The indexStart of the ItagItem is less than or equal to 0");
            assertEquals(dashManifestIndexStart, itagItemIndexStart,
                    "The indexStart value of the indexRange attribute of the DASH manifest ("
                            + dashManifestIndexStart
                            + ") is not equal to the indexStart of the ItagItem object ("
                            + itagItemIndexStart + ")");

            final int dashManifestIndexEnd;
            try {
                dashManifestIndexEnd = Integer.parseInt(indexRangeParts[1]);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the indexRange attribute is not valid", e);
            }

            final int itagItemIndexEnd = itagItem.getIndexEnd();
            assertTrue(itagItemIndexEnd > 0,
                    "The indexEnd of the ItagItem is less than or equal to 0");

            assertEquals(dashManifestIndexEnd, itagItemIndexEnd,
                    "The indexEnd value of the indexRange attribute of the DASH manifest ("
                            + dashManifestIndexEnd
                            + ") is not equal to the indexEnd of the ItagItem object ("
                            + itagItemIndexEnd + ")");
        }

        private void testInitializationElement(@Nonnull final Document document,
                                               @Nonnull final ItagItem itagItem) {
            final Element initializationElement = (Element) document
                    .getElementsByTagName("Initialization").item(0);
            assertNotNull(initializationElement, "The Initialization element doesn't exist");
            assertTrue(initializationElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("SegmentBase").item(0)),
                    "The SegmentBase element doesn't contain an Initialization element");

            final String rangeValue = initializationElement
                    .getAttribute("range");
            assertFalse(isBlank(rangeValue),
                    "The value of the range attribute is empty or the corresponding attribute doesn't exist");
            final String[] rangeParts = rangeValue.split("-");
            assertEquals(2, rangeParts.length, "The value of the range attribute is not valid");

            final int dashManifestInitStart;
            try {
                dashManifestInitStart = Integer.parseInt(rangeParts[0]);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the range attribute is not valid", e);
            }

            final int itagItemInitStart = itagItem.getInitStart();
            assertTrue(itagItemInitStart >= 0, "The initStart of the ItagItem is less than 0");
            assertEquals(dashManifestInitStart, itagItemInitStart,
                    "The initStart value of the range attribute of the DASH manifest ("
                            + dashManifestInitStart
                            + ") is not equal to the initStart of the ItagItem object ("
                            + itagItemInitStart + ")");

            final int dashManifestInitEnd;
            try {
                dashManifestInitEnd = Integer.parseInt(rangeParts[1]);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the indexRange attribute is not valid", e);
            }

            final int itagItemInitEnd = itagItem.getInitEnd();
            assertTrue(itagItemInitEnd > 0, "The indexEnd of the ItagItem is less than or equal to 0");
            assertEquals(dashManifestInitEnd, itagItemInitEnd,
                    "The initEnd value of the range attribute of the DASH manifest ("
                            + dashManifestInitEnd
                            + ") is not equal to the initEnd of the ItagItem object ("
                            + itagItemInitEnd + ")");
        }
    }
}
