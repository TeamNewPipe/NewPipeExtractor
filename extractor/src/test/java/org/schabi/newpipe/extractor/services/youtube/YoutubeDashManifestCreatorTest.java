package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
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
import java.util.Random;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.utils.Utils.isBlank;

/**
 * Test for {@link YoutubeDashManifestCreator}.
 * <p>
 * We cannot test the generation of DASH manifests for ended livestreams because these videos will
 * be re-encoded as normal videos later, so we can't use a specific video.
 * <br>
 * <br>
 * The generation of DASH manifests for OTF streams, which can be tested, uses a video licenced
 * under the Creative Commons Attribution licence (reuse allowed):
 * {@code https://www.youtube.com/watch?v=DJ8GQUNUXGM}
 * <br>
 * <br>
 * We couldn't use mocks for these tests because the streaming URLs needs to fetched and the IP
 * address used to get these URLs is required (used as a param in the URLs; without it, video
 * servers return 403 Forbidden).
 * <br>
 * So the real downloader will be used everytime on this test class.
 * </p>
 */
public class YoutubeDashManifestCreatorTest {

    public static class testGenerationOfOtfManifests {
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
            // Test only the first five OTF streams we found
            int otfStreamsTested = 0;
            for (final VideoStream videoStream : extractor.getVideoOnlyStreams()) {
                if (otfStreamsTested < 5) {
                    if (videoStream.getDeliveryMethod() == DeliveryMethod.DASH) {
                        final String otfBaseUrl = videoStream.getContent();
                        assertFalse("The OTF base URL is empty", isBlank(otfBaseUrl));
                        final ItagItem itagItem = videoStream.getItagItem();
                        assertNotNull("The itagItem is null", itagItem);
                        final String dashManifest = YoutubeDashManifestCreator
                                .createDashManifestFromOtfStreamingUrl(otfBaseUrl, itagItem);
                        assertFalse("The DASH manifest is null or empty" + dashManifest,
                                isBlank(dashManifest));
                        testManifestGenerated(dashManifest, itagItem);
                        otfStreamsTested += 1;
                    }
                } else {
                    break;
                }
            }
        }

        private void testManifestGenerated(final String dashManifest,
                                           @Nonnull final ItagItem itagItem) throws Exception {
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
            testSegmentTemplateElement(document);
            testSegmentTimelineAndSElements(document);
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
            assertTrue("The value of the initialization attribute doesn't end with &sq=0",
                    initializationValue.endsWith("&sq=0"));

            final String mediaValue = segmentTemplateElement.getAttribute("media");
            assertFalse("The value of the media attribute is empty or the corresponding attribute doesn't exist",
                    isBlank(mediaValue));
            try {
                new URL(mediaValue);
            } catch (final MalformedURLException e) {
                throw new AssertionError("The value of the media attribute is not an URL",
                        e);
            }
            assertTrue("The value of the media attribute doesn't end with &sq=$Number$",
                    mediaValue.endsWith("&sq=$Number$"));

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
            assertTrue("The SegmentTemplate element doesn't contain an SegmentTimeline element",
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
    }
}
