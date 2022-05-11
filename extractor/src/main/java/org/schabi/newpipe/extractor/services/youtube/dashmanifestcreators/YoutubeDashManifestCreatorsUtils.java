package org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.DeliveryType;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.addClientInfoHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getAndroidUserAgent;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getIosUserAgent;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isAndroidStreamingUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isIosStreamingUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isTvHtml5SimplyEmbeddedPlayerStreamingUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isWebStreamingUrl;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * Utilities and constants for YouTube DASH manifest creators.
 *
 * <p>
 * This class includes common methods of manifest creators and useful constants.
 * </p>
 *
 * <p>
 * Generation of DASH documents and their conversion as a string is done using external classes
 * from {@link org.w3c.dom} and {@link javax.xml} packages.
 * </p>
 */
public final class YoutubeDashManifestCreatorsUtils {

    private YoutubeDashManifestCreatorsUtils() {
    }

    /**
     * The redirect count limit that this class uses, which is the same limit as OkHttp.
     */
    public static final int MAXIMUM_REDIRECT_COUNT = 20;

    /**
     * URL parameter of the first sequence for live, post-live-DVR and OTF streams.
     */
    public static final String SQ_0 = "&sq=0";

    /**
     * URL parameter of the first stream request made by official clients.
     */
    public static final String RN_0 = "&rn=0";

    /**
     * URL parameter specific to web clients. When this param is added, if a redirection occurs,
     * the server will not redirect clients to the redirect URL. Instead, it will provide this URL
     * as the response body.
     */
    public static final String ALR_YES = "&alr=yes";

    /**
     * Constant which represents the {@code MPD} element of DASH manifests.
     */
    public static final String MPD = "MPD";

    /**
     * Constant which represents the {@code Period} element of DASH manifests.
     */
    public static final String PERIOD = "Period";

    /**
     * Constant which represents the {@code AdaptationSet} element of DASH manifests.
     */
    public static final String ADAPTATION_SET = "AdaptationSet";

    /**
     * Constant which represents the {@code Role} element of DASH manifests.
     */
    public static final String ROLE = "Role";

    /**
     * Constant which represents the {@code Representation} element of DASH manifests.
     */
    public static final String REPRESENTATION = "Representation";

    /**
     * Constant which represents the {@code AudioChannelConfiguration} element of DASH manifests.
     */
    public static final String AUDIO_CHANNEL_CONFIGURATION = "AudioChannelConfiguration";

    /**
     * Constant which represents the {@code SegmentTemplate} element of DASH manifests.
     */
    public static final String SEGMENT_TEMPLATE = "SegmentTemplate";

    /**
     * Constant which represents the {@code SegmentTimeline} element of DASH manifests.
     */
    public static final String SEGMENT_TIMELINE = "SegmentTimeline";

    /**
     * Constant which represents the {@code SegmentBase} element of DASH manifests.
     */
    public static final String BASE_URL = "BaseURL";

    /**
     * Constant which represents the {@code SegmentBase} element of DASH manifests.
     */
    public static final String SEGMENT_BASE = "SegmentBase";

    /**
     * Constant which represents the {@code Initialization} element of DASH manifests.
     */
    public static final String INITIALIZATION = "Initialization";

    /**
     * Generate a {@link Document} with common manifest creator elements added to it.
     *
     * <p>
     * Those are:
     * <ul>
     *     <li>{@code MPD} (using {@link #generateDocumentAndMpdElement(long)});</li>
     *     <li>{@code Period} (using {@link #generatePeriodElement(Document)});</li>
     *     <li>{@code AdaptationSet} (using {@link #generateAdaptationSetElement(Document,
     *     ItagItem)});</li>
     *     <li>{@code Role} (using {@link #generateRoleElement(Document)});</li>
     *     <li>{@code Representation} (using {@link #generateRepresentationElement(Document,
     *     ItagItem)});</li>
     *     <li>and, for audio streams, {@code AudioChannelConfiguration} (using
     *     {@link #generateAudioChannelConfigurationElement(Document, ItagItem)}).</li>
     * </ul>
     * </p>
     *
     * @param itagItem the {@link ItagItem} associated to the stream, which must not be null
     * @param streamDuration the duration of the stream, in milliseconds
     * @return a {@link Document} with the common elements added in it
     */
    @Nonnull
    public static Document generateDocumentAndDoCommonElementsGeneration(
            @Nonnull final ItagItem itagItem,
            final long streamDuration) throws CreationException {
        final Document document = generateDocumentAndMpdElement(streamDuration);

        generatePeriodElement(document);
        generateAdaptationSetElement(document, itagItem);
        generateRoleElement(document);
        generateRepresentationElement(document, itagItem);
        if (itagItem.itagType == ItagItem.ItagType.AUDIO) {
            generateAudioChannelConfigurationElement(document, itagItem);
        }

        return document;
    }

    /**
     * Create a {@link Document} instance and generate the {@code <MPD>} element of the manifest.
     *
     * <p>
     * The generated {@code <MPD>} element looks like the manifest returned into the player
     * response of videos:
     * </p>
     *
     * <p>
     * {@code <MPD xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xmlns="urn:mpeg:DASH:schema:MPD:2011"
     * xsi:schemaLocation="urn:mpeg:DASH:schema:MPD:2011 DASH-MPD.xsd" minBufferTime="PT1.500S"
     * profiles="urn:mpeg:dash:profile:isoff-main:2011" type="static"
     * mediaPresentationDuration="PT$duration$S">}
     * (where {@code $duration$} represents the duration in seconds (a number with 3 digits after
     * the decimal point)).
     * </p>
     *
     * @param duration the duration of the stream, in milliseconds
     * @return a {@link Document} instance which contains a {@code <MPD>} element
     */
    @Nonnull
    public static Document generateDocumentAndMpdElement(final long duration)
            throws CreationException {
        try {
            final Document document = newDocument();

            final Element mpdElement = document.createElement(MPD);
            document.appendChild(mpdElement);

            final Attr xmlnsXsiAttribute = document.createAttribute("xmlns:xsi");
            xmlnsXsiAttribute.setValue("http://www.w3.org/2001/XMLSchema-instance");
            mpdElement.setAttributeNode(xmlnsXsiAttribute);

            final Attr xmlns = document.createAttribute("xmlns");
            xmlns.setValue("urn:mpeg:DASH:schema:MPD:2011");
            mpdElement.setAttributeNode(xmlns);

            final Attr xsiSchemaLocationAttribute = document.createAttribute("xsi:schemaLocation");
            xsiSchemaLocationAttribute.setValue("urn:mpeg:DASH:schema:MPD:2011 DASH-MPD.xsd");
            mpdElement.setAttributeNode(xsiSchemaLocationAttribute);

            final Attr minBufferTimeAttribute = document.createAttribute("minBufferTime");
            minBufferTimeAttribute.setValue("PT1.500S");
            mpdElement.setAttributeNode(minBufferTimeAttribute);

            final Attr profilesAttribute = document.createAttribute("profiles");
            profilesAttribute.setValue("urn:mpeg:dash:profile:full:2011");
            mpdElement.setAttributeNode(profilesAttribute);

            final Attr typeAttribute = document.createAttribute("type");
            typeAttribute.setValue("static");
            mpdElement.setAttributeNode(typeAttribute);

            final Attr mediaPresentationDurationAttribute = document.createAttribute(
                    "mediaPresentationDuration");
            final String durationSeconds = String.format(Locale.ENGLISH, "%.3f",
                    duration / 1000.0);
            mediaPresentationDurationAttribute.setValue("PT" + durationSeconds + "S");
            mpdElement.setAttributeNode(mediaPresentationDurationAttribute);

            return document;
        } catch (final Exception e) {
            throw new CreationException(
                    "Could not generate the DASH manifest or append the MPD document to it", e);
        }
    }

    /**
     * Generate the {@code <Period>} element, appended as a child of the {@code <MPD>} element.
     *
     * <p>
     * The {@code <MPD>} element needs to be generated before this element with
     * {@link #generateDocumentAndMpdElement(long)}.
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <Period>} element will be
     *                 appended
     */
    public static void generatePeriodElement(@Nonnull final Document document)
            throws CreationException {
        try {
            final Element mpdElement = (Element) document.getElementsByTagName(MPD).item(0);
            final Element periodElement = document.createElement(PERIOD);
            mpdElement.appendChild(periodElement);
        } catch (final DOMException e) {
            throw CreationException.couldNotAddElement(PERIOD, e);
        }
    }

    /**
     * Generate the {@code <AdaptationSet>} element, appended as a child of the {@code <Period>}
     * element.
     *
     * <p>
     * The {@code <Period>} element needs to be generated before this element with
     * {@link #generatePeriodElement(Document)}.
     * </p>
     *
     * @param document the {@link Document} on which the {@code <Period>} element will be
     *                 appended
     * @param itagItem the {@link ItagItem} corresponding to the stream, which must not be null
     */
    public static void generateAdaptationSetElement(@Nonnull final Document document,
                                                    @Nonnull final ItagItem itagItem)
            throws CreationException {
        try {
            final Element periodElement = (Element) document.getElementsByTagName(PERIOD)
                    .item(0);
            final Element adaptationSetElement = document.createElement(ADAPTATION_SET);

            final Attr idAttribute = document.createAttribute("id");
            idAttribute.setValue("0");
            adaptationSetElement.setAttributeNode(idAttribute);

            final MediaFormat mediaFormat = itagItem.getMediaFormat();
            if (mediaFormat == null || isNullOrEmpty(mediaFormat.getMimeType())) {
                throw CreationException.couldNotAddElement(ADAPTATION_SET,
                        "the MediaFormat or its mime type is null or empty");
            }

            final Attr mimeTypeAttribute = document.createAttribute("mimeType");
            mimeTypeAttribute.setValue(mediaFormat.getMimeType());
            adaptationSetElement.setAttributeNode(mimeTypeAttribute);

            final Attr subsegmentAlignmentAttribute = document.createAttribute(
                    "subsegmentAlignment");
            subsegmentAlignmentAttribute.setValue("true");
            adaptationSetElement.setAttributeNode(subsegmentAlignmentAttribute);

            periodElement.appendChild(adaptationSetElement);
        } catch (final DOMException e) {
            throw CreationException.couldNotAddElement(ADAPTATION_SET, e);
        }
    }

    /**
     * Generate the {@code <Role>} element, appended as a child of the {@code <AdaptationSet>}
     * element.
     *
     * <p>
     * This element, with its attributes and values, is:
     * </p>
     *
     * <p>
     * {@code <Role schemeIdUri="urn:mpeg:DASH:role:2011" value="main"/>}
     * </p>
     *
     * <p>
     * The {@code <AdaptationSet>} element needs to be generated before this element with
     * {@link #generateAdaptationSetElement(Document, ItagItem)}).
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <Role>} element will be
     *                 appended
     */
    public static void generateRoleElement(@Nonnull final Document document)
            throws CreationException {
        try {
            final Element adaptationSetElement = (Element) document.getElementsByTagName(
                    ADAPTATION_SET).item(0);
            final Element roleElement = document.createElement(ROLE);

            final Attr schemeIdUriAttribute = document.createAttribute("schemeIdUri");
            schemeIdUriAttribute.setValue("urn:mpeg:DASH:role:2011");
            roleElement.setAttributeNode(schemeIdUriAttribute);

            final Attr valueAttribute = document.createAttribute("value");
            valueAttribute.setValue("main");
            roleElement.setAttributeNode(valueAttribute);

            adaptationSetElement.appendChild(roleElement);
        } catch (final DOMException e) {
            throw CreationException.couldNotAddElement(ROLE, e);
        }
    }

    /**
     * Generate the {@code <Representation>} element, appended as a child of the
     * {@code <AdaptationSet>} element.
     *
     * <p>
     * The {@code <AdaptationSet>} element needs to be generated before this element with
     * {@link #generateAdaptationSetElement(Document, ItagItem)}).
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <SegmentTimeline>} element will
     *                 be appended
     * @param itagItem the {@link ItagItem} to use, which must not be null
     */
    public static void generateRepresentationElement(@Nonnull final Document document,
                                                     @Nonnull final ItagItem itagItem)
            throws CreationException {
        try {
            final Element adaptationSetElement = (Element) document.getElementsByTagName(
                    ADAPTATION_SET).item(0);
            final Element representationElement = document.createElement(REPRESENTATION);

            final int id = itagItem.id;
            if (id <= 0) {
                throw CreationException.couldNotAddElement(REPRESENTATION,
                        "the id of the ItagItem is <= 0");
            }
            final Attr idAttribute = document.createAttribute("id");
            idAttribute.setValue(String.valueOf(id));
            representationElement.setAttributeNode(idAttribute);

            final String codec = itagItem.getCodec();
            if (isNullOrEmpty(codec)) {
                throw CreationException.couldNotAddElement(ADAPTATION_SET,
                        "the codec value of the ItagItem is null or empty");
            }
            final Attr codecsAttribute = document.createAttribute("codecs");
            codecsAttribute.setValue(codec);
            representationElement.setAttributeNode(codecsAttribute);

            final Attr startWithSAPAttribute = document.createAttribute("startWithSAP");
            startWithSAPAttribute.setValue("1");
            representationElement.setAttributeNode(startWithSAPAttribute);

            final Attr maxPlayoutRateAttribute = document.createAttribute("maxPlayoutRate");
            maxPlayoutRateAttribute.setValue("1");
            representationElement.setAttributeNode(maxPlayoutRateAttribute);

            final int bitrate = itagItem.getBitrate();
            if (bitrate <= 0) {
                throw CreationException.couldNotAddElement(REPRESENTATION,
                        "the bitrate of the ItagItem is <= 0");
            }
            final Attr bandwidthAttribute = document.createAttribute("bandwidth");
            bandwidthAttribute.setValue(String.valueOf(bitrate));
            representationElement.setAttributeNode(bandwidthAttribute);

            final ItagItem.ItagType itagType = itagItem.itagType;

            if (itagType == ItagItem.ItagType.VIDEO || itagType == ItagItem.ItagType.VIDEO_ONLY) {
                final int height = itagItem.getHeight();
                final int width = itagItem.getWidth();
                if (height <= 0 && width <= 0) {
                    throw CreationException.couldNotAddElement(REPRESENTATION,
                            "both width and height of the ItagItem are <= 0");
                }

                if (width > 0) {
                    final Attr widthAttribute = document.createAttribute("width");
                    widthAttribute.setValue(String.valueOf(width));
                    representationElement.setAttributeNode(widthAttribute);
                }

                final Attr heightAttribute = document.createAttribute("height");
                heightAttribute.setValue(String.valueOf(itagItem.getHeight()));
                representationElement.setAttributeNode(heightAttribute);

                final int fps = itagItem.getFps();
                if (fps > 0) {
                    final Attr frameRateAttribute = document.createAttribute("frameRate");
                    frameRateAttribute.setValue(String.valueOf(fps));
                    representationElement.setAttributeNode(frameRateAttribute);
                }
            }

            if (itagType == ItagItem.ItagType.AUDIO && itagItem.getSampleRate() > 0) {
                final Attr audioSamplingRateAttribute = document.createAttribute(
                        "audioSamplingRate");
                audioSamplingRateAttribute.setValue(String.valueOf(itagItem.getSampleRate()));
            }

            adaptationSetElement.appendChild(representationElement);
        } catch (final DOMException e) {
            throw CreationException.couldNotAddElement(REPRESENTATION, e);
        }
    }

    /**
     * Generate the {@code <AudioChannelConfiguration>} element, appended as a child of the
     * {@code <Representation>} element.
     *
     * <p>
     * This method is only used when generating DASH manifests of audio streams.
     * </p>
     *
     * <p>
     * It will produce the following element:
     * <br>
     * {@code <AudioChannelConfiguration
     * schemeIdUri="urn:mpeg:dash:23003:3:audio_channel_configuration:2011"
     * value="audioChannelsValue"}
     * <br>
     * (where {@code audioChannelsValue} is get from the {@link ItagItem} passed as the second
     * parameter of this method)
     * </p>
     *
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement(Document, ItagItem)}).
     * </p>
     *
     * @param document the {@link Document} on which the {@code <AudioChannelConfiguration>}
     *                 element will be appended
     * @param itagItem the {@link ItagItem} to use, which must not be null
     */
    public static void generateAudioChannelConfigurationElement(
            @Nonnull final Document document,
            @Nonnull final ItagItem itagItem) throws CreationException {
        try {
            final Element representationElement = (Element) document.getElementsByTagName(
                    REPRESENTATION).item(0);
            final Element audioChannelConfigurationElement = document.createElement(
                    AUDIO_CHANNEL_CONFIGURATION);

            final Attr schemeIdUriAttribute = document.createAttribute("schemeIdUri");
            schemeIdUriAttribute.setValue(
                    "urn:mpeg:dash:23003:3:audio_channel_configuration:2011");
            audioChannelConfigurationElement.setAttributeNode(schemeIdUriAttribute);

            final Attr valueAttribute = document.createAttribute("value");
            final int audioChannels = itagItem.getAudioChannels();
            if (audioChannels <= 0) {
                throw new CreationException("the number of audioChannels in the ItagItem is <= 0: "
                        + audioChannels);
            }
            valueAttribute.setValue(String.valueOf(itagItem.getAudioChannels()));
            audioChannelConfigurationElement.setAttributeNode(valueAttribute);

            representationElement.appendChild(audioChannelConfigurationElement);
        } catch (final DOMException e) {
            throw CreationException.couldNotAddElement(AUDIO_CHANNEL_CONFIGURATION, e);
        }
    }

    /**
     * Convert a DASH manifest {@link Document document} to a string and cache it.
     *
     * @param originalBaseStreamingUrl the original base URL of the stream
     * @param document                 the document to be converted
     * @param manifestCreatorCache     the {@link ManifestCreatorCache} on which store the string
     *                                 generated
     * @return the DASH manifest {@link Document document} converted to a string
     */
    public static String buildAndCacheResult(
            @Nonnull final String originalBaseStreamingUrl,
            @Nonnull final Document document,
            @Nonnull final ManifestCreatorCache<String, String> manifestCreatorCache)
            throws CreationException {

        try {
            final String documentXml = documentToXml(document);
            manifestCreatorCache.put(originalBaseStreamingUrl, documentXml);
            return documentXml;
        } catch (final Exception e) {
            throw new CreationException(
                    "Could not convert the DASH manifest generated to a string", e);
        }
    }

    /**
     * Generate the {@code <SegmentTemplate>} element, appended as a child of the
     * {@code <Representation>} element.
     *
     * <p>
     * This method is only used when generating DASH manifests from OTF and post-live-DVR streams.
     * </p>
     *
     * <p>
     * It will produce a {@code <SegmentTemplate>} element with the following attributes:
     * <ul>
     *     <li>{@code startNumber}, which takes the value {@code 0} for post-live-DVR streams and
     *     {@code 1} for OTF streams;</li>
     *     <li>{@code timescale}, which is always {@code 1000};</li>
     *     <li>{@code media}, which is the base URL of the stream on which is appended
     *     {@code &sq=$Number$};</li>
     *     <li>{@code initialization} (only for OTF streams), which is the base URL of the stream
     *     on which is appended {@link #SQ_0}.</li>
     * </ul>
     * </p>
     *
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement(Document, ItagItem)}).
     * </p>
     *
     * @param document     the {@link Document} on which the {@code <SegmentTemplate>} element will
     *                     be appended
     * @param baseUrl      the base URL of the OTF/post-live-DVR stream
     * @param deliveryType the stream {@link DeliveryType delivery type}, which must be either
     * {@link DeliveryType#OTF OTF} or {@link DeliveryType#LIVE LIVE}
     */
    public static void generateSegmentTemplateElement(@Nonnull final Document document,
                                                      @Nonnull final String baseUrl,
                                                      final DeliveryType deliveryType)
            throws CreationException {
        if (deliveryType != DeliveryType.OTF && deliveryType != DeliveryType.LIVE) {
            throw CreationException.couldNotAddElement(SEGMENT_TEMPLATE, "invalid delivery type: "
                    + deliveryType);
        }

        try {
            final Element representationElement = (Element) document.getElementsByTagName(
                    REPRESENTATION).item(0);
            final Element segmentTemplateElement = document.createElement(SEGMENT_TEMPLATE);

            final Attr startNumberAttribute = document.createAttribute("startNumber");
            final boolean isDeliveryTypeLive = deliveryType == DeliveryType.LIVE;
            // The first sequence of post DVR streams is the beginning of the video stream and not
            // an initialization segment
            final String startNumberValue = isDeliveryTypeLive ? "0" : "1";
            startNumberAttribute.setValue(startNumberValue);
            segmentTemplateElement.setAttributeNode(startNumberAttribute);

            final Attr timescaleAttribute = document.createAttribute("timescale");
            timescaleAttribute.setValue("1000");
            segmentTemplateElement.setAttributeNode(timescaleAttribute);

            // Post-live-DVR/ended livestreams streams don't require an initialization sequence
            if (!isDeliveryTypeLive) {
                final Attr initializationAttribute = document.createAttribute("initialization");
                initializationAttribute.setValue(baseUrl + SQ_0);
                segmentTemplateElement.setAttributeNode(initializationAttribute);
            }

            final Attr mediaAttribute = document.createAttribute("media");
            mediaAttribute.setValue(baseUrl + "&sq=$Number$");
            segmentTemplateElement.setAttributeNode(mediaAttribute);

            representationElement.appendChild(segmentTemplateElement);
        } catch (final DOMException e) {
            throw CreationException.couldNotAddElement(SEGMENT_TEMPLATE, e);
        }
    }

    /**
     * Generate the {@code <SegmentTimeline>} element, appended as a child of the
     * {@code <SegmentTemplate>} element.
     *
     * <p>
     * The {@code <SegmentTemplate>} element needs to be generated before this element with
     * {@link #generateSegmentTemplateElement(Document, String, DeliveryType)}.
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <SegmentTimeline>} element will
     *                 be appended
     */
    public static void generateSegmentTimelineElement(@Nonnull final Document document)
            throws CreationException {
        try {
            final Element segmentTemplateElement = (Element) document.getElementsByTagName(
                    SEGMENT_TEMPLATE).item(0);
            final Element segmentTimelineElement = document.createElement(SEGMENT_TIMELINE);

            segmentTemplateElement.appendChild(segmentTimelineElement);
        } catch (final DOMException e) {
            throw CreationException.couldNotAddElement(SEGMENT_TIMELINE, e);
        }
    }

    /**
     * Get the "initialization" {@link Response response} of a stream.
     *
     * <p>This method fetches, for OTF streams and for post-live-DVR streams:
     *     <ul>
     *         <li>the base URL of the stream, to which are appended {@link #SQ_0} and
     *         {@link #RN_0} parameters, with a {@code GET} request for streaming URLs from HTML5
     *         clients and a {@code POST} request for the ones from the {@code ANDROID} and the
     *         {@code IOS} clients;</li>
     *         <li>for streaming URLs from HTML5 clients, the {@link #ALR_YES} param is also added.
     *         </li>
     *     </ul>
     * </p>
     *
     * @param baseStreamingUrl the base URL of the stream, which must not be null
     * @param itagItem         the {@link ItagItem} of stream, which must not be null
     * @param deliveryType     the {@link DeliveryType} of the stream
     * @return the "initialization" response, without redirections on the network on which the
     * request(s) is/are made
     */
    @SuppressWarnings("checkstyle:FinalParameters")
    @Nonnull
    public static Response getInitializationResponse(@Nonnull String baseStreamingUrl,
                                                     @Nonnull final ItagItem itagItem,
                                                     final DeliveryType deliveryType)
            throws CreationException {
        final boolean isHtml5StreamingUrl = isWebStreamingUrl(baseStreamingUrl)
                || isTvHtml5SimplyEmbeddedPlayerStreamingUrl(baseStreamingUrl);
        final boolean isAndroidStreamingUrl = isAndroidStreamingUrl(baseStreamingUrl);
        final boolean isIosStreamingUrl = isIosStreamingUrl(baseStreamingUrl);
        if (isHtml5StreamingUrl) {
            baseStreamingUrl += ALR_YES;
        }
        baseStreamingUrl = appendRnParamAndSqParamIfNeeded(baseStreamingUrl, deliveryType);

        final Downloader downloader = NewPipe.getDownloader();
        if (isHtml5StreamingUrl) {
            final String mimeTypeExpected = itagItem.getMediaFormat().getMimeType();
            if (!isNullOrEmpty(mimeTypeExpected)) {
                return getStreamingWebUrlWithoutRedirects(downloader, baseStreamingUrl,
                        mimeTypeExpected);
            }
        } else if (isAndroidStreamingUrl || isIosStreamingUrl) {
            try {
                final Map<String, List<String>> headers = new HashMap<>();
                headers.put("User-Agent", Collections.singletonList(
                        isAndroidStreamingUrl ? getAndroidUserAgent(null)
                                : getIosUserAgent(null)));
                final byte[] emptyBody = "".getBytes(StandardCharsets.UTF_8);
                return downloader.post(baseStreamingUrl, headers, emptyBody);
            } catch (final IOException | ExtractionException e) {
                throw new CreationException("Could not get the "
                        + (isIosStreamingUrl ? "ANDROID" : "IOS") + " streaming URL response", e);
            }
        }

        try {
            return downloader.get(baseStreamingUrl);
        } catch (final IOException | ExtractionException e) {
            throw new CreationException("Could not get the streaming URL response", e);
        }
    }

    /**
     * Generate a new {@link DocumentBuilder} secured from XEE attacks, on platforms which
     * support setting {@link XMLConstants#ACCESS_EXTERNAL_DTD} and
     * {@link XMLConstants#ACCESS_EXTERNAL_SCHEMA} in {@link DocumentBuilderFactory} instances.
     *
     * @return an instance of {@link Document} secured against XEE attacks on supported platforms,
     * that should then be convertible to an XML string without security problems
     */
    private static Document newDocument() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (final Exception ignored) {
            // Ignore exceptions as setting these attributes to secure XML generation is supported
            // by all platforms (like the Android implementation)
        }

        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.newDocument();
    }

    /**
     * Generate a new {@link TransformerFactory} secured from XEE attacks, on platforms which
     * support setting {@link XMLConstants#ACCESS_EXTERNAL_DTD} and
     * {@link XMLConstants#ACCESS_EXTERNAL_SCHEMA} in {@link TransformerFactory} instances.
     *
     * @param document the document to convert, which must have been created using
     *                 {@link #newDocument()} to properly prevent XEE attacks
     * @return the document converted to an XML string, making sure there can't be XEE attacks
     */
    // Sonar warning is suppressed because it is still shown even if we apply its solution
    @SuppressWarnings("squid:S2755")
    private static String documentToXml(@Nonnull final Document document)
            throws TransformerException {

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (final Exception ignored) {
            // Ignore exceptions as setting these attributes to secure XML generation is supported
            // by all platforms (like the Android implementation)
        }

        final Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        final StringWriter result = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(result));

        return result.toString();
    }

    /**
     * Append {@link #SQ_0} for post-live-DVR and OTF streams and {@link #RN_0} to all streams.
     *
     * @param baseStreamingUrl the base streaming URL to which the parameter(s) are being appended
     * @param deliveryType     the {@link DeliveryType} of the stream
     * @return the base streaming URL to which the param(s) are appended, depending on the
     * {@link DeliveryType} of the stream
     */
    @SuppressWarnings({"checkstyle:FinalParameters", "checkstyle:FinalLocalVariable"})
    @Nonnull
    private static String appendRnParamAndSqParamIfNeeded(
            @Nonnull String baseStreamingUrl,
            @Nonnull final DeliveryType deliveryType) {
        if (deliveryType != DeliveryType.PROGRESSIVE) {
            baseStreamingUrl += SQ_0;
        }

        return baseStreamingUrl + RN_0;
    }

    /**
     * Get a URL on which no redirection between playback hosts should be present on the network
     * and/or IP used to fetch the streaming URL, for HTML5 clients.
     *
     * <p>This method will follow redirects which works in the following way:
     *     <ol>
     *         <li>the {@link #ALR_YES} param is appended to all streaming URLs</li>
     *         <li>if no redirection occurs, the video server will return the streaming data;</li>
     *         <li>if a redirection occurs, the server will respond with HTTP status code 200 and a
     *         {@code text/plain} mime type. The redirection URL is the response body;</li>
     *         <li>the redirection URL is requested and the steps above from step 2 are repeated,
     *         until too many redirects are reached of course (the maximum number of redirects is
     *         {@link #MAXIMUM_REDIRECT_COUNT the same as OkHttp}).</li>
     *     </ol>
     * </p>
     *
     * <p>
     * For non-HTML5 clients, redirections are managed in the standard way in
     * {@link #getInitializationResponse(String, ItagItem, DeliveryType)}.
     * </p>
     *
     * @param downloader               the {@link Downloader} instance to be used
     * @param streamingUrl             the streaming URL which we are trying to get a streaming URL
     *                                 without any redirection on the network and/or IP used
     * @param responseMimeTypeExpected the response mime type expected from Google video servers
     * @return the {@link Response} of the stream, which should have no redirections
     */
    @SuppressWarnings("checkstyle:FinalParameters")
    @Nonnull
    private static Response getStreamingWebUrlWithoutRedirects(
            @Nonnull final Downloader downloader,
            @Nonnull String streamingUrl,
            @Nonnull final String responseMimeTypeExpected)
            throws CreationException {
        try {
            final Map<String, List<String>> headers = new HashMap<>();
            addClientInfoHeaders(headers);

            String responseMimeType = "";

            int redirectsCount = 0;
            while (!responseMimeType.equals(responseMimeTypeExpected)
                    && redirectsCount < MAXIMUM_REDIRECT_COUNT) {
                final Response response = downloader.get(streamingUrl, headers);

                final int responseCode = response.responseCode();
                if (responseCode != 200) {
                    throw new CreationException(
                            "Could not get the initialization URL: HTTP response code "
                                    + responseCode);
                }

                // A valid HTTP 1.0+ response should include a Content-Type header, so we can
                // require that the response from video servers has this header.
                responseMimeType = Objects.requireNonNull(response.getHeader("Content-Type"),
                        "Could not get the Content-Type header from the response headers");

                // The response body is the redirection URL
                if (responseMimeType.equals("text/plain")) {
                    streamingUrl = response.responseBody();
                    redirectsCount++;
                } else {
                    return response;
                }
            }

            if (redirectsCount >= MAXIMUM_REDIRECT_COUNT) {
                throw new CreationException(
                        "Too many redirects when trying to get the the streaming URL response of a "
                                + "HTML5 client");
            }

            // This should never be reached, but is required because we don't want to return null
            // here
            throw new CreationException(
                    "Could not get the streaming URL response of a HTML5 client: unreachable code "
                            + "reached!");
        } catch (final IOException | ExtractionException e) {
            throw new CreationException(
                    "Could not get the streaming URL response of a HTML5 client", e);
        }
    }
}
