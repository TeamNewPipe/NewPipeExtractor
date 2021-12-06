package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.utils.Utils.isBlank;

/**
 * Test for {@link YoutubeDashManifestCreator}.
 *
 * <p>
 * We cannot test the generation of DASH manifests for ended livestreams because these videos will
 * be re-encoded as normal videos later, so we can't use a specific video.
 * </p>
 * <p>
 * The generation of DASH manifests for OTF streams, which can be tested, uses a video licenced
 * under the Creative Commons Attribution licence (reuse allowed):
 * {@code https://www.youtube.com/watch?v=DJ8GQUNUXGM}
 * </p>
 * <p>
 * We couldn't use mocks for these tests because the streaming URLs needs to fetched and the IP
 * address used to get these URLs is required (used as a param in the URLs; without it, video
 * servers return 403 Forbidden).
 * </p>
 * <p>
 * So the real downloader will be used everytime on this test class.
 * </p>
 */
public class YoutubeDashManifestCreatorTest {
    // Setting a higher number may let Google video servers return a lot of 403s
    private static final int MAXIMUM_NUMBER_OF_STREAMS_TO_TEST = 3;

    public static class testGenerationOfOtfAndProgressiveManifests {
        private static final String url = "https://www.youtube.com/watch?v=DJ8GQUNUXGM";
        private static YoutubeStreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            YoutubeParsingHelper.setNumberGenerator(new Random(1));
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube.getStreamExtractor(url);
            extractor.fetchPage();
        }

        @Test
        public void testOtfStreamsANewEraOfOpen() throws Exception {
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
                assertEquals("The exception thrown was not the one excepted: "
                                + e.getClass().getName()
                                + "was thrown instead of YoutubeDashManifestCreationException",
                        YoutubeDashManifestCreator.YoutubeDashManifestCreationException.class,
                        e.getClass());
            }
        }

        @Test
        public void testProgressiveStreamsANewEraOfOpen() throws Exception {
            testStreams(DeliveryMethod.PROGRESSIVE_HTTP,
                    extractor.getVideoOnlyStreams());
            testStreams(DeliveryMethod.PROGRESSIVE_HTTP,
                    extractor.getAudioStreams());
            try {
                testStreams(DeliveryMethod.PROGRESSIVE_HTTP,
                        extractor.getVideoStreams());
            } catch (final Exception e) {
                assertEquals("The exception thrown was not the one excepted: "
                                + e.getClass().getName()
                                + "was thrown instead of YoutubeDashManifestCreationException",
                        YoutubeDashManifestCreator.YoutubeDashManifestCreationException.class,
                        e.getClass());
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
                    assertFalse("The base URL of the stream is empty", isBlank(baseUrl));

                    final ItagItem itagItem = stream.getItagItem();
                    assertNotNull("The itagItem is null", itagItem);

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
                    assertFalse("The DASH manifest is null or empty" + dashManifest,
                            isBlank(dashManifest));
                }
                i++;
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
            assertNotNull("The MPD element doesn't exist", mpdElement);
            assertNull("The MPD element has a parent element",
                    mpdElement.getParentNode().getNodeValue());

            final String mediaPresentationDurationValue = mpdElement
                    .getAttribute("mediaPresentationDuration");
            assertNotNull(
                    "The value of the mediaPresentationDuration attribute is empty or the corresponding attribute doesn't exist",
                    mediaPresentationDurationValue);
            assertTrue(
                    "The mediaPresentationDuration attribute of the DASH manifest is not valid",
                    mediaPresentationDurationValue.startsWith("PT"));
        }

        private void testPeriodElement(@Nonnull final Document document) {
            final Element periodElement = (Element) document.getElementsByTagName("Period")
                    .item(0);
            assertNotNull("The Period element doesn't exist", periodElement);
            assertTrue("The MPD element doesn't contain a Period element",
                    periodElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("MPD").item(0)));
        }

        private void testAdaptationSetElement(@Nonnull final Document document,
                                              @Nonnull final ItagItem itagItem) {
            final Element adaptationSetElement = (Element) document
                    .getElementsByTagName("AdaptationSet").item(0);
            assertNotNull("The AdaptationSet element doesn't exist", adaptationSetElement);
            assertTrue("The Period element doesn't contain an AdaptationSet element",
                    adaptationSetElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("Period").item(0)));

            final String mimeTypeDashManifestValue = adaptationSetElement
                    .getAttribute("mimeType");
            assertFalse("The value of the mimeType attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(mimeTypeDashManifestValue));

            final String mimeTypeItagItemValue = itagItem.getMediaFormat().getMimeType();
            assertFalse("The mimeType of the ItagItem is empty",
                    isBlank(mimeTypeItagItemValue));

            assertEquals("The mimeType attribute of the DASH manifest ("
                    + mimeTypeItagItemValue
                    + ") is not equal to the mimeType set in the ItagItem object ("
                    + mimeTypeItagItemValue
                    + ")", mimeTypeDashManifestValue, mimeTypeItagItemValue);
        }

        private void testRoleElement(@Nonnull final Document document) {
            final Element roleElement = (Element) document.getElementsByTagName("Role")
                    .item(0);
            assertNotNull("The Role element doesn't exist", roleElement);
            assertTrue("The AdaptationSet element doesn't contain a Role element",
                    roleElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("AdaptationSet").item(0)));
        }

        private void testRepresentationElement(@Nonnull final Document document,
                                               @Nonnull final ItagItem itagItem) {
            final Element representationElement = (Element) document
                    .getElementsByTagName("Representation").item(0);
            assertNotNull("The Representation element doesn't exist",
                    representationElement);
            assertTrue("The AdaptationSet element doesn't contain a Representation element",
                    representationElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("AdaptationSet").item(0)));

            final String bandwidthDashManifestValue = representationElement
                    .getAttribute("bandwidth");
            assertFalse("The value of the bandwidth attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(bandwidthDashManifestValue));

            final int bandwidthDashManifest;
            try {
                bandwidthDashManifest = Integer.parseInt(bandwidthDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the bandwidth attribute is not an integer",
                        e);
            }
            assertTrue("The value of the bandwidth attribute is less than or equal to 0",
                    bandwidthDashManifest > 0);

            final int bitrateItagItem = itagItem.getBitrate();
            assertTrue("The bitrate of the ItagItem is less than or equal to 0",
                    bitrateItagItem > 0);

            assertEquals("The value of the bandwidth attribute of the DASH manifest ("
                    + bandwidthDashManifest
                    + ") is not equal to the bitrate value set in the ItagItem object ("
                    + bitrateItagItem
                    + ")", bandwidthDashManifest, bitrateItagItem);

            final String codecsDashManifestValue = representationElement.getAttribute("codecs");
            assertFalse("The value of the codecs attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(codecsDashManifestValue));

            final String codecsItagItemValue = itagItem.getCodec();
            assertFalse("The codec of the ItagItem is empty", isBlank(codecsItagItemValue));

            assertEquals("The value of the codecs attribute of the DASH manifest ("
                    + codecsDashManifestValue
                    + ") is not equal to the codecs value set in the ItagItem object ("
                    + codecsItagItemValue
                    + ")", codecsDashManifestValue, codecsItagItemValue);

            if (itagItem.itagType == ItagItem.ItagType.VIDEO_ONLY
                    || itagItem.itagType == ItagItem.ItagType.VIDEO) {
                testVideoItagItemAttributes(representationElement, itagItem);
            }

            final String idDashManifestValue = representationElement.getAttribute("id");
            assertFalse("The value of the id attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(idDashManifestValue));

            final int idDashManifest;
            try {
                idDashManifest = Integer.parseInt(idDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the id attribute is not an integer",
                        e);
            }
            assertTrue("The value of the id attribute is less than or equal to 0",
                    idDashManifest > 0);

            final int idItagItem = itagItem.id;
            assertTrue("The id of the ItagItem is less than or equal to 0",
                    idItagItem > 0);
            assertEquals("The value of the id attribute of the DASH manifest ("
                    + idDashManifestValue
                    + ") is not equal to the id of the ItagItem object ("
                    + idItagItem
                    + ")", idDashManifest, idItagItem);
        }

        private void testVideoItagItemAttributes(@Nonnull final Element representationElement,
                                                 @Nonnull final ItagItem itagItem) {
            final String frameRateDashManifestValue = representationElement
                    .getAttribute("frameRate");
            assertFalse("The value of the frameRate attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(frameRateDashManifestValue));

            final int frameRateDashManifest;
            try {
                frameRateDashManifest = Integer.parseInt(frameRateDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the frameRate attribute is not an integer",
                        e);
            }
            assertTrue("The value of the frameRate attribute is less than or equal to 0",
                    frameRateDashManifest > 0);

            final int fpsItagItem = itagItem.fps;
            assertTrue("The fps of the ItagItem is unknown", fpsItagItem > 0);

            assertEquals("The value of the frameRate attribute of the DASH manifest ("
                    + frameRateDashManifest
                    + ") is not equal to the frame rate value set in the ItagItem object ("
                    + fpsItagItem
                    + ")", frameRateDashManifest, fpsItagItem);

            final String heightDashManifestValue = representationElement.getAttribute("height");
            assertFalse("The value of the height attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(heightDashManifestValue));

            final int heightDashManifest;
            try {
                heightDashManifest = Integer.parseInt(heightDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the height attribute is not an integer",
                        e);
            }
            assertTrue("The value of the height attribute is less than or equal to 0",
                    heightDashManifest > 0);

            final int heightItagItem = itagItem.getHeight();
            assertTrue("The height of the ItagItem is less than or equal to 0",
                    heightItagItem > 0);

            assertEquals("The value of the height attribute of the DASH manifest ("
                    + heightDashManifest
                    + ") is not equal to the height value set in the ItagItem object ("
                    + heightItagItem
                    + ")", heightDashManifest, heightItagItem);

            final String widthDashManifestValue = representationElement.getAttribute("width");
            assertFalse("The value of the width attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(widthDashManifestValue));

            final int widthDashManifest;
            try {
                widthDashManifest = Integer.parseInt(widthDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the width attribute is not an integer",
                        e);
            }
            assertTrue("The value of the width attribute is less than or equal to 0",
                    widthDashManifest > 0);

            final int widthItagItem = itagItem.getWidth();
            assertTrue("The width of the ItagItem is less than or equal to 0",
                    widthItagItem > 0);

            assertEquals("The value of the width attribute of the DASH manifest ("
                    + widthDashManifest
                    + ") is not equal to the width value set in the ItagItem object ("
                    + widthItagItem
                    + ")", widthDashManifest, widthItagItem);
        }

        private void testAudioChannelConfigurationElement(@Nonnull final Document document,
                                                          @Nonnull final ItagItem itagItem) {
            final Element audioChannelConfigurationElement = (Element) document
                    .getElementsByTagName("AudioChannelConfiguration").item(0);
            assertNotNull("The AudioChannelConfiguration element doesn't exist",
                    audioChannelConfigurationElement);
            assertTrue("The Representation element doesn't contain an AudioChannelConfiguration element",
                    audioChannelConfigurationElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("Representation").item(0)));

            final String audioChannelsDashManifestValue = audioChannelConfigurationElement
                    .getAttribute("value");
            assertFalse("The value of the value attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(audioChannelsDashManifestValue));

            final int audioChannelsDashManifest;
            try {
                audioChannelsDashManifest = Integer.parseInt(audioChannelsDashManifestValue);
            } catch (final NumberFormatException e) {
                throw new AssertionError(
                        "The number of audio channels (the value attribute) is not an integer",
                        e);
            }
            assertTrue("The number of audio channels (the value attribute) is less than or equal to 0",
                    audioChannelsDashManifest > 0);

            final int audioChannelsItagItem = itagItem.getAudioChannels();
            assertTrue("The number of audio channels of the ItagItem is less than or equal to 0",
                    audioChannelsItagItem > 0);

            assertEquals("The value of the value attribute of the DASH manifest ("
                    + audioChannelsDashManifest
                    + ") is not equal to the number of audio channels set in the ItagItem object ("
                    + audioChannelsItagItem
                    + ")", audioChannelsDashManifest, audioChannelsItagItem);
        }

        private void testSegmentTemplateElement(@Nonnull final Document document) {
            final Element segmentTemplateElement = (Element) document
                    .getElementsByTagName("SegmentTemplate").item(0);
            assertNotNull("The SegmentTemplate element doesn't exist",
                    segmentTemplateElement);
            assertTrue("The Representation element doesn't contain a SegmentTemplate element",
                    segmentTemplateElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("Representation").item(0)));

            final String initializationValue = segmentTemplateElement
                    .getAttribute("initialization");
            assertFalse("The value of the initialization attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(initializationValue));
            try {
                new URL(initializationValue);
            } catch (final MalformedURLException e) {
                throw new AssertionError("The value of the initialization attribute is not an URL",
                        e);
            }
            assertTrue("The value of the initialization attribute doesn't end with &sq=0&rn=0",
                    initializationValue.endsWith("&sq=0&rn=0"));

            final String mediaValue = segmentTemplateElement.getAttribute("media");
            assertFalse("The value of the media attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(mediaValue));
            try {
                new URL(mediaValue);
            } catch (final MalformedURLException e) {
                throw new AssertionError("The value of the media attribute is not an URL",
                        e);
            }
            assertTrue("The value of the media attribute doesn't end with &sq=$Number$&rn=$Number$",
                    mediaValue.endsWith("&sq=$Number$&rn=$Number$"));

            final String startNumberValue = segmentTemplateElement.getAttribute("startNumber");
            assertFalse("The value of the startNumber attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(startNumberValue));
            assertEquals("The value of the startNumber attribute is not equal to 1", "1",
                    startNumberValue);
        }

        private void testSegmentTimelineAndSElements(@Nonnull final Document document) {
            final Element segmentTimelineElement = (Element) document
                    .getElementsByTagName("SegmentTimeline").item(0);
            assertNotNull("The SegmentTimeline element doesn't exist",
                    segmentTimelineElement);
            assertTrue("The SegmentTemplate element doesn't contain a SegmentTimeline element",
                    segmentTimelineElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("SegmentTemplate").item(0)));
            testSElements(segmentTimelineElement);
        }

        private void testSElements(@Nonnull final Element segmentTimelineElement) {
            final NodeList segmentTimelineElementChildren = segmentTimelineElement.getChildNodes();
            final int segmentTimelineElementChildrenLength = segmentTimelineElementChildren
                    .getLength();
            assertNotEquals(
                    "The DASH manifest doesn't have a segment element (S) in the SegmentTimeLine element",
                    0,
                    segmentTimelineElementChildrenLength);

            for (int i = 0; i < segmentTimelineElementChildrenLength; i++) {
                final Element sElement = (Element) segmentTimelineElement.getElementsByTagName("S")
                        .item(i);

                final String dValue = sElement.getAttribute("d");
                assertFalse("The value of the duration of this segment (the d attribute of this S element) is empty or the corresponding attribute doesn't exist",
                        isBlank(dValue));

                final int d;
                try {
                    d = Integer.parseInt(dValue);
                } catch (final NumberFormatException e) {
                    throw new AssertionError("The value of the d attribute is not an integer", e);
                }
                assertTrue("The value of the d attribute is less than or equal to 0", d > 0);

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
                    assertTrue("The value of the r attribute is less than or equal to 0", r > 0);
                }
            }
        }

        private void testBaseUrlElement(@Nonnull final Document document) {
            final Element baseURLElement = (Element) document
                    .getElementsByTagName("BaseURL").item(0);
            assertNotNull("The BaseURL element doesn't exist", baseURLElement);
            assertTrue("The Representation element doesn't contain a BaseURL element",
                    baseURLElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("Representation").item(0)));

            final String baseURLElementContentValue = baseURLElement
                    .getTextContent();
            assertFalse("The content of the BaseURL element is empty or the corresponding element has no content",
                    isBlank(baseURLElementContentValue));

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
            assertNotNull("The SegmentBase element doesn't exist", segmentBaseElement);
            assertTrue("The Representation element doesn't contain a SegmentBase element",
                    segmentBaseElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("Representation").item(0)));

            final String indexRangeValue = segmentBaseElement
                    .getAttribute("indexRange");
            assertFalse("The value of the indexRange attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(indexRangeValue));
            final String[] indexRangeParts = indexRangeValue.split("-");
            assertEquals("The value of the indexRange attribute is not valid", 2,
                    indexRangeParts.length);

            final int dashManifestIndexStart;
            try {
                dashManifestIndexStart = Integer.parseInt(indexRangeParts[0]);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the indexRange attribute is not valid", e);
            }

            final int itagItemIndexStart = itagItem.getIndexStart();
            assertTrue("The indexStart of the ItagItem is less than or equal to 0",
                    itagItemIndexStart > 0);
            assertEquals("The indexStart value of the indexRange attribute of the DASH manifest ("
                    + dashManifestIndexStart
                    + ") is not equal to the indexStart of the ItagItem object ("
                    + itagItemIndexStart
                    + ")", dashManifestIndexStart, itagItemIndexStart);

            final int dashManifestIndexEnd;
            try {
                dashManifestIndexEnd = Integer.parseInt(indexRangeParts[1]);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the indexRange attribute is not valid", e);
            }

            final int itagItemIndexEnd = itagItem.getIndexEnd();
            assertTrue("The indexEnd of the ItagItem is less than or equal to 0",
                    itagItemIndexEnd > 0);
            assertEquals("The indexEnd value of the indexRange attribute of the DASH manifest ("
                    + dashManifestIndexEnd
                    + ") is not equal to the indexEnd of the ItagItem object ("
                    + itagItemIndexEnd
                    + ")", dashManifestIndexEnd, itagItemIndexEnd);
        }

        private void testInitializationElement(@Nonnull final Document document,
                                               @Nonnull final ItagItem itagItem) {
            final Element initializationElement = (Element) document
                    .getElementsByTagName("Initialization").item(0);
            assertNotNull("The Initialization element doesn't exist", initializationElement);
            assertTrue("The SegmentBase element doesn't contain an Initialization element",
                    initializationElement.getParentNode().isEqualNode(
                            document.getElementsByTagName("SegmentBase").item(0)));

            final String rangeValue = initializationElement
                    .getAttribute("range");
            assertFalse("The value of the range attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(rangeValue));
            final String[] rangeParts = rangeValue.split("-");
            assertEquals("The value of the range attribute is not valid", 2,
                    rangeParts.length);

            final int dashManifestInitStart;
            try {
                dashManifestInitStart = Integer.parseInt(rangeParts[0]);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the range attribute is not valid", e);
            }

            final int itagItemInitStart = itagItem.getInitStart();
            assertTrue("The initStart of the ItagItem is less than 0", itagItemInitStart >= 0);
            assertEquals("The initStart value of the range attribute of the DASH manifest ("
                    + dashManifestInitStart
                    + ") is not equal to the initStart of the ItagItem object ("
                    + itagItemInitStart
                    + ")", dashManifestInitStart, itagItemInitStart);

            final int dashManifestInitEnd;
            try {
                dashManifestInitEnd = Integer.parseInt(rangeParts[1]);
            } catch (final NumberFormatException e) {
                throw new AssertionError("The value of the indexRange attribute is not valid", e);
            }

            final int itagItemInitEnd = itagItem.getInitEnd();
            assertTrue("The indexEnd of the ItagItem is less than or equal to 0",
                    itagItemInitEnd > 0);
            assertEquals("The initEnd value of the range attribute of the DASH manifest ("
                    + dashManifestInitEnd
                    + ") is not equal to the initEnd of the ItagItem object ("
                    + itagItemInitEnd
                    + ")", dashManifestInitEnd, itagItemInitEnd);
        }
    }
}
