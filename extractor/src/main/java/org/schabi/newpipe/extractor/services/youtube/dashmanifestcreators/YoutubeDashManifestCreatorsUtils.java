package org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getOriginReferrerHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getVisionOsUserAgent;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isVisionOsStreamingUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isWebStreamingUrl;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.DeliveryType;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeBaseAudioDemuxedStream;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeBaseDemuxedStream;
import org.schabi.newpipe.extractor.services.youtube.stream.YoutubeBaseVideoDemuxedStream;
import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseAudioStream;
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
final class YoutubeDashManifestCreatorsUtils {

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

    // XML elements of DASH MPD manifests
    // see https://www.brendanlong.com/the-structure-of-an-mpeg-dash-mpd.html
    public static final String MPD = "MPD";
    public static final String PERIOD = "Period";
    public static final String ADAPTATION_SET = "AdaptationSet";
    public static final String ROLE = "Role";
    public static final String REPRESENTATION = "Representation";
    public static final String AUDIO_CHANNEL_CONFIGURATION = "AudioChannelConfiguration";
    public static final String SEGMENT_TEMPLATE = "SegmentTemplate";
    public static final String SEGMENT_TIMELINE = "SegmentTimeline";
    public static final String BASE_URL = "BaseURL";
    public static final String SEGMENT_BASE = "SegmentBase";
    public static final String INITIALIZATION = "Initialization";

    /**
     * Throws an {@link CreationException} if the given {@link YoutubeBaseDemuxedStream} is not
     * supported, i.e. it is not a subclass or an instance of {@link YoutubeBaseAudioDemuxedStream}
     * or {@link YoutubeBaseVideoDemuxedStream}.
     *
     * @param stream the {@link YoutubeBaseDemuxedStream} to test
     * @throws CreationException if the given {@link YoutubeBaseDemuxedStream} is not supported
     */
    static void checkIfStreamIsSupported(@Nonnull final YoutubeBaseDemuxedStream stream) {
        if (!(stream instanceof YoutubeBaseVideoDemuxedStream)
                && !(stream instanceof YoutubeBaseAudioDemuxedStream)) {
            throw new CreationException("Unsupported type: " + stream.getClass().getName());
        }
    }

    /**
     * Create an attribute with {@link Document#createAttribute(String)}, assign to it the provided
     * name and value, then add it to the provided element using {@link
     * Element#setAttributeNode(Attr)}.
     *
     * @param element element to which to add the created node
     * @param doc     document to use to create the attribute
     * @param name    name of the attribute
     * @param value   value of the attribute, will be set using {@link Attr#setValue(String)}
     */
    static void setAttribute(@Nonnull final Element element,
                             @Nonnull final Document doc,
                             @Nonnull final String name,
                             @Nonnull final String value) {
        final Attr attr = doc.createAttribute(name);
        attr.setValue(value);
        element.setAttributeNode(attr);
    }

    /**
     * Generate a {@link Document} with common manifest creator elements added to it.
     *
     * <br>
     * Those are:
     * <ul>
     *     <li>{@code MPD} (using {@link #generateDocumentAndMpdElement(long)});</li>
     *     <li>{@code Period} (using {@link #generatePeriodElement(Document)});</li>
     *     <li>{@code AdaptationSet} (using {@link #generateAdaptationSetElement(Document,
     *     YoutubeBaseDemuxedStream)});</li>
     *     <li>{@code Role} (using {@link #generateRoleElement(Document,
     *     YoutubeBaseDemuxedStream)});</li>
     *     <li>{@code Representation} (using {@link #generateRepresentationElement(Document,
     *     YoutubeBaseDemuxedStream)});</li>
     *     <li>and, for audio streams, {@code AudioChannelConfiguration} (using
     *     {@link #generateAudioChannelConfigurationElement(Document, BaseAudioStream)}).</li>
     * </ul>
     *
     * @param stream the {@link YoutubeBaseAudioDemuxedStream} or
     *               {@link YoutubeBaseVideoDemuxedStream} object associated to the stream, which
     *               must not be null, other subtypes will throw a {@link CreationException}
     * @param streamDuration the duration of the stream, in milliseconds
     * @return a {@link Document} with the common elements added in it
     */
    @Nonnull
    static Document generateDocumentAndDoCommonElementsGeneration(
            @Nonnull final YoutubeBaseDemuxedStream stream,
            final long streamDuration) throws CreationException {
        final Document doc = generateDocumentAndMpdElement(streamDuration);

        generatePeriodElement(doc);
        generateAdaptationSetElement(doc, stream);
        generateRoleElement(doc, stream);
        generateRepresentationElement(doc, stream);
        if (stream instanceof YoutubeBaseAudioDemuxedStream audioStream) {
            generateAudioChannelConfigurationElement(doc, audioStream);
        }

        return doc;
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
    static Document generateDocumentAndMpdElement(final long duration)
            throws CreationException {
        try {
            final Document doc = newDocument();

            final Element mpdElement = doc.createElement(MPD);
            doc.appendChild(mpdElement);

            setAttribute(mpdElement, doc, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            setAttribute(mpdElement, doc, "xmlns", "urn:mpeg:DASH:schema:MPD:2011");
            setAttribute(mpdElement, doc, "xsi:schemaLocation",
                    "urn:mpeg:DASH:schema:MPD:2011 DASH-MPD.xsd");
            setAttribute(mpdElement, doc, "minBufferTime", "PT1.500S");
            setAttribute(mpdElement, doc, "profiles", "urn:mpeg:dash:profile:full:2011");
            setAttribute(mpdElement, doc, "type", "static");
            setAttribute(mpdElement, doc, "mediaPresentationDuration",
                    String.format(Locale.ENGLISH, "PT%.3fS", duration / 1000.0));

            return doc;
        } catch (final Exception e) {
            throw new CreationException(
                    "Could not generate the DASH manifest or append the MPD doc to it", e);
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
     * @param doc the {@link Document} on which the {@code <Period>} element will be appended
     */
    private static void generatePeriodElement(@Nonnull final Document doc)
            throws CreationException {
        try {
            final Element mpdElement = (Element) doc.getElementsByTagName(MPD).item(0);
            final Element periodElement = doc.createElement(PERIOD);
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
     * @param doc    the {@link Document} on which the {@code <Period>} element will be appended
     * @param stream the {@link YoutubeBaseDemuxedStream} object representing the stream, which
     *               must not be null
     */
    private static void generateAdaptationSetElement(@Nonnull final Document doc,
                                                     @Nonnull final YoutubeBaseDemuxedStream stream)
            throws CreationException {
        try {
            final Element periodElement = (Element) doc.getElementsByTagName(PERIOD)
                    .item(0);
            final Element adaptationSetElement = doc.createElement(ADAPTATION_SET);

            setAttribute(adaptationSetElement, doc, "id", "0");

            final String mimeType;
            if (stream instanceof YoutubeBaseAudioDemuxedStream audioDemuxedStream) {
                mimeType = audioDemuxedStream.getMediaFormat().mimeType;
                final Locale audioLocale = audioDemuxedStream.getLocale();
                if (audioLocale != null) {
                    final String audioLanguage = audioLocale.getLanguage();
                    if (!audioLanguage.isEmpty()) {
                        setAttribute(adaptationSetElement, doc, "lang", audioLanguage);
                    }
                }
            } else {
                mimeType = ((YoutubeBaseVideoDemuxedStream) stream).getMediaFormat().mimeType;
            }

            setAttribute(adaptationSetElement, doc, "mimeType", mimeType);
            setAttribute(adaptationSetElement, doc, "subsegmentAlignment", "true");

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
     * {@code <Role schemeIdUri="urn:mpeg:DASH:role:2011" value="VALUE"/>}, where {@code VALUE} is
     * {@code main} for videos and audios, {@code description} for descriptive audio and
     * {@code dub} for dubbed audio.
     * </p>
     *
     * <p>
     * The {@code <AdaptationSet>} element needs to be generated before this element with
     * {@link #generateAdaptationSetElement(Document, YoutubeBaseDemuxedStream)}).
     * </p>
     *
     * @param doc    the {@link Document} on which the {@code <Role>} element will be appended
     * @param stream the {@link YoutubeBaseDemuxedStream} object representing this stream
     */
    private static void generateRoleElement(@Nonnull final Document doc,
                                            @Nonnull final YoutubeBaseDemuxedStream stream)
            throws CreationException {
        try {
            final Element adaptationSetElement = (Element) doc.getElementsByTagName(
                    ADAPTATION_SET).item(0);
            final Element roleElement = doc.createElement(ROLE);

            setAttribute(roleElement, doc, "schemeIdUri", "urn:mpeg:DASH:role:2011");
            if (stream instanceof YoutubeBaseAudioDemuxedStream audioStream) {
                setAttribute(roleElement, doc, "value",
                        getRoleValue(audioStream.getTrackType()));
            } else if (stream instanceof YoutubeBaseVideoDemuxedStream) {
                // There is only one video track on YouTube, which should recieve the main value
                setAttribute(roleElement, doc, "value", "main");
            } else {
                throw new CreationException("Unsupported stream type: " + stream);
            }

            adaptationSetElement.appendChild(roleElement);
        } catch (final DOMException e) {
            throw CreationException.couldNotAddElement(ROLE, e);
        }
    }

    /**
     * Get the value of the {@code <Role>} element based on the {@link AudioTrackType} attribute
     * of an audio stream.
     *
     * @param trackType audio track type
     * @return role value
     */
    @Nonnull
    private static String getRoleValue(@Nullable final AudioTrackType trackType) {
        if (trackType != null) {
            return switch (trackType) {
                case ORIGINAL -> "main";
                case DUBBED -> "dub";
                case DESCRIPTIVE -> "description";
                default ->
                    // Secondary track types do not seem to have a dedicated role in the DASH
                    // specification, so use alternate for them
                        "alternate";
            };
        }
        return "main";
    }

    /**
     * Generate the {@code <Representation>} element, appended as a child of the
     * {@code <AdaptationSet>} element.
     *
     * <p>
     * The {@code <AdaptationSet>} element needs to be generated before this element with
     * {@link #generateAdaptationSetElement(Document, YoutubeBaseDemuxedStream)}).
     * </p>
     *
     * @param doc    the {@link Document} on which the {@code <SegmentTimeline>} element will be
     *               appended
     * @param stream the {@link YoutubeBaseDemuxedStream} object representing the stream
     */
    private static void generateRepresentationElement(
            @Nonnull final Document doc,
            @Nonnull final YoutubeBaseDemuxedStream stream)
            throws CreationException {
        try {
            final Element adaptationSetElement = (Element) doc.getElementsByTagName(
                    ADAPTATION_SET).item(0);
            final Element representationElement = doc.createElement(REPRESENTATION);

            final String id = stream.getId();
            if (id == null) {
                throw CreationException.couldNotAddElement(REPRESENTATION, "the id is null");
            }
            setAttribute(representationElement, doc, "id", id);

            final String codec;
            final Integer bitrate;
            if (stream instanceof YoutubeBaseAudioDemuxedStream audioStream) {
                bitrate = audioStream.getAverageBitrate();
                codec = audioStream.getCodec();

                final Attr audioSamplingRateAttribute = doc.createAttribute("audioSamplingRate");
                final Integer sampleRate = audioStream.getSampleRate();
                if (sampleRate != null && sampleRate > 0) {
                    audioSamplingRateAttribute.setValue(String.valueOf(sampleRate));
                }
            } else {
                final YoutubeBaseVideoDemuxedStream videoStream =
                        ((YoutubeBaseVideoDemuxedStream) stream);
                bitrate = videoStream.getAverageBitrate();
                codec = videoStream.getCodec();

                final Integer height = videoStream.getHeight();
                final Integer width = videoStream.getWidth();

                if (height == null || width == null) {
                    throw CreationException.couldNotAddElement(REPRESENTATION,
                            "either width or height of the video stream is null");
                }

                if (height <= 0 || width <= 0) {
                    throw CreationException.couldNotAddElement(REPRESENTATION,
                            "either width or height of the video stream is <= 0");
                }

                setAttribute(representationElement, doc, "width", String.valueOf(width));
                setAttribute(representationElement, doc, "height", String.valueOf(height));

                final Integer fps = videoStream.getFramerate();
                if (fps != null && fps > 0) {
                    setAttribute(representationElement, doc, "frameRate", String.valueOf(fps));
                }
            }

            if (isNullOrEmpty(codec)) {
                throw CreationException.couldNotAddElement(ADAPTATION_SET,
                        "the codec value is null or empty");
            }

            setAttribute(representationElement, doc, "codecs", codec);
            setAttribute(representationElement, doc, "startWithSAP", "1");
            setAttribute(representationElement, doc, "maxPlayoutRate", "1");

            if (bitrate == null) {
                throw CreationException.couldNotAddElement(REPRESENTATION,
                        "the bitrate of the stream is null");
            }

            if (bitrate <= 0) {
                throw CreationException.couldNotAddElement(REPRESENTATION,
                        "the bitrate of the stream is <= 0");
            }

            setAttribute(representationElement, doc, "bandwidth", String.valueOf(bitrate));

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
     * (where {@code audioChannelsValue} is get from the {@link BaseAudioStream} passed as the
     * second parameter of this method)
     * </p>
     *
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement(Document, YoutubeBaseDemuxedStream)}).
     * </p>
     *
     * @param doc         the {@link Document} on which the {@code <AudioChannelConfiguration>}
     *                    element will be appended
     * @param audioStream the {@link BaseAudioStream} object representing the stream, which must
     *                    not be null
     * @throws CreationException if {@link BaseAudioStream#getChannelsCount()} returns null or a
     * value <= 0, which should not happen on YouTube
     */
    private static void generateAudioChannelConfigurationElement(
            @Nonnull final Document doc,
            @Nonnull final BaseAudioStream audioStream) throws CreationException {
        try {
            final Element representationElement = (Element) doc.getElementsByTagName(
                    REPRESENTATION).item(0);
            final Element audioChannelConfigurationElement = doc.createElement(
                    AUDIO_CHANNEL_CONFIGURATION);

            setAttribute(audioChannelConfigurationElement, doc, "schemeIdUri",
                    "urn:mpeg:dash:23003:3:audio_channel_configuration:2011");

            final Integer audioChannelsCount = audioStream.getChannelsCount();
            if (audioChannelsCount == null) {
                throw new CreationException("The number of audio channels is not available (null)");
            }

            if (audioChannelsCount <= 0) {
                throw new CreationException("The number of audio channels is <= 0: "
                        + audioChannelsCount);
            }
            setAttribute(audioChannelConfigurationElement, doc, "value",
                    String.valueOf(audioChannelsCount));

            representationElement.appendChild(audioChannelConfigurationElement);
        } catch (final DOMException e) {
            throw CreationException.couldNotAddElement(AUDIO_CHANNEL_CONFIGURATION, e);
        }
    }

    /**
     * Convert a DASH manifest {@link Document doc} to a string and cache it.
     *
     * @param originalBaseStreamingUrl the original base URL of the stream
     * @param doc                      the doc to be converted
     * @param manifestCreatorCache     the {@link ManifestCreatorCache} on which store the string
     *                                 generated
     * @return the DASH manifest {@link Document doc} converted to a string
     */
    static String buildAndCacheResult(
            @Nonnull final String originalBaseStreamingUrl,
            @Nonnull final Document doc,
            @Nonnull final ManifestCreatorCache<String, String> manifestCreatorCache)
            throws CreationException {

        try {
            final String documentXml = documentToXml(doc);
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
     * This method is only used when generating DASH manifests from post-live-DVR streams.
     * </p>
     *
     * It will produce a {@code <SegmentTemplate>} element with the following attributes:
     * <ul>
     *     <li>{@code startNumber}, which takes the value {@code 0};</li>
     *     <li>{@code timescale}, which is always {@code 1000};</li>
     *     <li>{@code media}, which is the base URL of the stream on which is appended
     *     {@code &sq=$Number$};</li>
     * </ul>
     *
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement(Document, YoutubeBaseDemuxedStream)}).
     * </p>
     *
     * @param doc          the {@link Document} on which the {@code <SegmentTemplate>} element will
     *                     be appended
     * @param baseUrl      the base URL of the post-live-DVR stream
     */
    static void generateSegmentTemplateElement(@Nonnull final Document doc,
                                               @Nonnull final String baseUrl)
            throws CreationException {
        try {
            final Element representationElement = (Element) doc.getElementsByTagName(
                    REPRESENTATION).item(0);
            final Element segmentTemplateElement = doc.createElement(SEGMENT_TEMPLATE);

            // The first sequence of post DVR streams is the beginning of the video stream and not
            // an initialization segment
            setAttribute(segmentTemplateElement, doc, "startNumber", "0");
            setAttribute(segmentTemplateElement, doc, "timescale", "1000");
            setAttribute(segmentTemplateElement, doc, "media", baseUrl + "&sq=$Number$");

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
     * {@link #generateSegmentTemplateElement(Document, String)}.
     * </p>
     *
     * @param doc the {@link Document} on which the {@code <SegmentTimeline>} element will be
     *            appended
     */
    public static void generateSegmentTimelineElement(@Nonnull final Document doc)
            throws CreationException {
        try {
            final Element segmentTemplateElement = (Element) doc.getElementsByTagName(
                    SEGMENT_TEMPLATE).item(0);
            final Element segmentTimelineElement = doc.createElement(SEGMENT_TIMELINE);

            segmentTemplateElement.appendChild(segmentTimelineElement);
        } catch (final DOMException e) {
            throw CreationException.couldNotAddElement(SEGMENT_TIMELINE, e);
        }
    }

    /**
     * Get the "initialization" {@link Response response} of a stream.
     * <br>
     * This method fetches, for OTF streams and for post-live-DVR streams:
     *     <ul>
     *         <li>the base URL of the stream, to which are appended {@link #SQ_0} and
     *         {@link #RN_0} parameters, with a {@code POST} request for streaming URLs from
     *         {@code WEB}, and {@code VISIONOS} clients;</li>
     *         <li>for streaming URLs from HTML5 clients, the {@link #ALR_YES} param is also added.
     *         </li>
     *     </ul>
     *
     * @param baseStreamingUrl the base URL of the stream, which must not be null
     * @param expectedMimeType the expected mime type of the media format of the stream, which may
     *                         be null
     * @param deliveryType     the {@link DeliveryType} of the stream
     * @return the "initialization" response, without redirections on the network on which the
     * request(s) is/are made
     */
    @SuppressWarnings("checkstyle:FinalParameters")
    @Nonnull
    static Response getInitializationResponse(@Nonnull String baseStreamingUrl,
                                              @Nullable final String expectedMimeType,
                                              final DeliveryType deliveryType)
            throws CreationException {
        final boolean isWebStreamingUrl = isWebStreamingUrl(baseStreamingUrl);
        if (isWebStreamingUrl) {
            baseStreamingUrl += ALR_YES;
        }
        baseStreamingUrl = appendRnSqParamsIfNeeded(baseStreamingUrl, deliveryType);

        final Downloader downloader = NewPipe.getDownloader();
        if (isWebStreamingUrl && !isNullOrEmpty(expectedMimeType)) {
            return getStreamingWebUrlWithoutRedirects(downloader, baseStreamingUrl,
                    expectedMimeType);
        } else if (isVisionOsStreamingUrl(baseStreamingUrl)) {
            try {
                return downloader.post(baseStreamingUrl,
                        Map.of("User-Agent", List.of(getVisionOsUserAgent(null))),
                        "".getBytes(StandardCharsets.UTF_8));
            } catch (final IOException | ExtractionException e) {
                throw new CreationException("Could not get the VISIONOS streaming URL response", e);
            }
        }

        try {
            return downloader.get(baseStreamingUrl);
        } catch (final IOException | ExtractionException e) {
            throw new CreationException("Could not get the streaming URL response", e);
        }
    }

    /**
     * Generate a new {@link DocumentBuilder} secured from XXE attacks, on platforms which
     * support setting {@link XMLConstants#ACCESS_EXTERNAL_DTD} and
     * {@link XMLConstants#ACCESS_EXTERNAL_SCHEMA} in {@link DocumentBuilderFactory} instances.
     *
     * @return an instance of {@link Document} secured against XXE attacks on supported platforms,
     *         that should then be convertible to an XML string without security problems
     */
    private static Document newDocument() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (final Exception ignored) {
            // Ignore exceptions as setting these attributes to secure XML generation is not
            // supported by all platforms (like the Android implementation)
        }

        return documentBuilderFactory.newDocumentBuilder().newDocument();
    }

    /**
     * Generate a new {@link TransformerFactory} secured from XXE attacks, on platforms which
     * support setting {@link XMLConstants#ACCESS_EXTERNAL_DTD} and
     * {@link XMLConstants#ACCESS_EXTERNAL_SCHEMA} in {@link TransformerFactory} instances.
     *
     * @param doc the doc to convert, which must have been created using {@link #newDocument()} to
     *            properly prevent XXE attacks
     * @return the doc converted to an XML string, making sure there can't be XXE attacks
     */
    // Sonar warning is suppressed because it is still shown even if we apply its solution
    @SuppressWarnings("squid:S2755")
    private static String documentToXml(@Nonnull final Document doc)
            throws TransformerException {

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (final Exception ignored) {
            // Ignore exceptions as setting these attributes to secure XML generation is not
            // supported by all platforms (like the Android implementation)
        }

        final Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        final StringWriter result = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(result));

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
    @Nonnull
    private static String appendRnSqParamsIfNeeded(@Nonnull final String baseStreamingUrl,
                                                   @Nonnull final DeliveryType deliveryType) {
        return baseStreamingUrl + (deliveryType == DeliveryType.PROGRESSIVE ? "" : SQ_0) + RN_0;
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
     * {@link #getInitializationResponse(String, String, DeliveryType)}.
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
            final Map<String, List<String>> headers = new HashMap<>(
                    getOriginReferrerHeaders("https://www.youtube.com"));

            String responseMimeType = "";

            int redirectsCount = 0;
            while (!responseMimeType.equals(responseMimeTypeExpected)
                    && redirectsCount < MAXIMUM_REDIRECT_COUNT) {
                final byte[] html5Body = new byte[] {0x78, 0};
                final Response response = downloader.post(streamingUrl, headers, html5Body);

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
