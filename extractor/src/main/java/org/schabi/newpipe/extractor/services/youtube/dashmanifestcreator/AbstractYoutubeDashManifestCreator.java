package org.schabi.newpipe.extractor.services.youtube.dashmanifestcreator;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.addClientInfoHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getAndroidUserAgent;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getIosUserAgent;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isAndroidStreamingUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isIosStreamingUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isTvHtml5SimplyEmbeddedPlayerStreamingUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isWebStreamingUrl;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.ADAPTATION_SET;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.AUDIO_CHANNEL_CONFIGURATION;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.MPD;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.PERIOD;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.REPRESENTATION;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.ROLE;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.SEGMENT_TEMPLATE;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.SEGMENT_TIMELINE;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.itag.format.AudioItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.format.VideoItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.info.ItagInfo;
import org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreationException;
import org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreator;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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

// TODO: Doc
public abstract class AbstractYoutubeDashManifestCreator implements DashManifestCreator {

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

    protected final ItagInfo<?> itagInfo;
    protected final long durationSecondsFallback;

    protected Document document;

    protected AbstractYoutubeDashManifestCreator(
            @Nonnull final ItagInfo<?> itagInfo,
            final long durationSecondsFallback) {
        this.itagInfo = Objects.requireNonNull(itagInfo);
        this.durationSecondsFallback = durationSecondsFallback;
    }

    protected boolean isLiveDelivery() {
        return false;
    }

    // region generate manifest elements

    /**
     * Generate a {@link Document} with common manifest creator elements added to it.
     *
     * <p>
     * Those are:
     * <ul>
     *     <li>{@code MPD} (using {@link #generateDocumentAndMpdElement(long)});</li>
     *     <li>{@code Period} (using {@link #generatePeriodElement()});</li>
     *     <li>{@code AdaptationSet} (using {@link #generateAdaptationSetElement()});</li>
     *     <li>{@code Role} (using {@link #generateRoleElement()});</li>
     *     <li>{@code Representation} (using {@link #generateRepresentationElement()});</li>
     *     <li>and, for audio streams, {@code AudioChannelConfiguration} (using
     *     {@link #generateAudioChannelConfigurationElement()}).</li>
     * </ul>
     * </p>
     *
     * @param streamDurationMs the duration of the stream, in milliseconds
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateDocumentAndCommonElements(final long streamDurationMs) {
        generateDocumentAndMpdElement(streamDurationMs);

        generatePeriodElement();
        generateAdaptationSetElement();
        generateRoleElement();
        generateRepresentationElement();
        if (itagInfo.getItagFormat() instanceof AudioItagFormat) {
            generateAudioChannelConfigurationElement();
        }
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
     * @param durationMs the duration of the stream, in milliseconds
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateDocumentAndMpdElement(final long durationMs) {
        try {
            newDocument();

            final Element mpdElement = createElement(MPD);
            document.appendChild(mpdElement);

            appendNewAttrWithValue(
                    mpdElement, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

            appendNewAttrWithValue(mpdElement, "xmlns", "urn:mpeg:DASH:schema:MPD:2011");

            appendNewAttrWithValue(
                    mpdElement,
                    "xsi:schemaLocation",
                    "urn:mpeg:DASH:schema:MPD:2011 DASH-MPD.xsd");

            appendNewAttrWithValue(mpdElement, "minBufferTime", "PT1.500S");

            appendNewAttrWithValue(
                    mpdElement, "profiles", "urn:mpeg:dash:profile:full:2011");

            appendNewAttrWithValue(mpdElement, "type", "static");

            final String durationSeconds =
                    String.format(Locale.ENGLISH, "%.3f", durationMs / 1000.0);
            appendNewAttrWithValue(
                    mpdElement, "mediaPresentationDuration", "PT" + durationSeconds + "S");
        } catch (final Exception e) {
            throw new DashManifestCreationException(
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
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generatePeriodElement() {
        try {
            getFirstElementByName(MPD).appendChild(createElement(PERIOD));
        } catch (final DOMException e) {
            throw DashManifestCreationException.couldNotAddElement(PERIOD, e);
        }
    }

    /**
     * Generate the {@code <AdaptationSet>} element, appended as a child of the {@code <Period>}
     * element.
     *
     * <p>
     * The {@code <Period>} element needs to be generated before this element with
     * {@link #generatePeriodElement()}.
     * </p>
     *
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateAdaptationSetElement() {
        try {
            final Element adaptationSetElement = createElement(ADAPTATION_SET);

            appendNewAttrWithValue(adaptationSetElement, "id", "0");

            appendNewAttrWithValue(
                    adaptationSetElement,
                    "mimeType",
                    itagInfo.getItagFormat().mediaFormat().mimeType());

            appendNewAttrWithValue(adaptationSetElement, "subsegmentAlignment", "true");

            getFirstElementByName(PERIOD).appendChild(adaptationSetElement);
        } catch (final DOMException e) {
            throw DashManifestCreationException.couldNotAddElement(ADAPTATION_SET, e);
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
     * {@link #generateAdaptationSetElement(Document)}).
     * </p>
     *
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateRoleElement() {
        try {
            final Element roleElement = createElement(ROLE);

            appendNewAttrWithValue(roleElement, "schemeIdUri", "urn:mpeg:DASH:role:2011");

            appendNewAttrWithValue(roleElement, "value", "main");

            getFirstElementByName(ADAPTATION_SET).appendChild(roleElement);
        } catch (final DOMException e) {
            throw DashManifestCreationException.couldNotAddElement(ROLE, e);
        }
    }

    /**
     * Generate the {@code <Representation>} element, appended as a child of the
     * {@code <AdaptationSet>} element.
     *
     * <p>
     * The {@code <AdaptationSet>} element needs to be generated before this element with
     * {@link #generateAdaptationSetElement()}).
     * </p>
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateRepresentationElement() {
        try {
            final Element representationElement = createElement(REPRESENTATION);

            appendNewAttrWithValue(
                    representationElement, "id", itagInfo.getItagFormat().id());

            final String codec = itagInfo.getCodec();
            if (isNullOrEmpty(codec)) {
                throw DashManifestCreationException.couldNotAddElement(ADAPTATION_SET,
                        "invalid codec=" + codec);
            }

            appendNewAttrWithValue(
                    representationElement, "codecs", codec);

            appendNewAttrWithValue(
                    representationElement, "startWithSAP", "1");

            appendNewAttrWithValue(
                    representationElement, "maxPlayoutRate", "1");

            final Integer bitrate = itagInfo.getBitRate();
            if (bitrate == null || bitrate <= 0) {
                throw DashManifestCreationException.couldNotAddElement(
                        REPRESENTATION,
                        "invalid bitrate=" + bitrate);
            }

            appendNewAttrWithValue(
                    representationElement, "bandwidth", bitrate);

            if (itagInfo.getItagFormat() instanceof VideoItagFormat) {

                final VideoQualityData videoQualityData = itagInfo.getCombinedVideoQualityData();

                if (videoQualityData.height() <= 0 && videoQualityData.width() <= 0) {
                    throw DashManifestCreationException.couldNotAddElement(
                            REPRESENTATION,
                            "both width and height are <= 0");
                }

                if (videoQualityData.width() > 0) {
                    appendNewAttrWithValue(
                            representationElement, "width", videoQualityData.width());
                }

                appendNewAttrWithValue(
                        representationElement, "height", videoQualityData.height());

                if (videoQualityData.fps() > 0) {
                    appendNewAttrWithValue(
                            representationElement, "frameRate", videoQualityData.fps());
                }
            }

            if (itagInfo.getItagFormat() instanceof AudioItagFormat
                    && itagInfo.getAudioSampleRate() != null
                    && itagInfo.getAudioSampleRate() > 0) {

                appendNewAttrWithValue(
                        representationElement,
                        "audioSamplingRate",
                        itagInfo.getAudioSampleRate());
            }

            getFirstElementByName(ADAPTATION_SET).appendChild(representationElement);
        } catch (final DOMException e) {
            throw DashManifestCreationException.couldNotAddElement(REPRESENTATION, e);
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
     * (where {@code audioChannelsValue} is get from the {@link ItagInfo} passed as the second
     * parameter of this method)
     * </p>
     *
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement()}).
     * </p>
     *
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateAudioChannelConfigurationElement() {
        try {
            final Element audioChannelConfigElement = createElement(AUDIO_CHANNEL_CONFIGURATION);

            appendNewAttrWithValue(
                    audioChannelConfigElement,
                    "schemeIdUri",
                    "urn:mpeg:dash:23003:3:audio_channel_configuration:2011");

            final Integer audioChannels = itagInfo.getAudioChannels();
            if (audioChannels != null && audioChannels <= 0) {
                throw new DashManifestCreationException(
                        "Invalid value for 'audioChannels'=" + audioChannels);
            }

            appendNewAttrWithValue(
                    audioChannelConfigElement, "value", itagInfo.getAudioChannels());

            getFirstElementByName(REPRESENTATION).appendChild(audioChannelConfigElement);
        } catch (final DOMException e) {
            throw DashManifestCreationException.couldNotAddElement(AUDIO_CHANNEL_CONFIGURATION, e);
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
     * {@link #generateRepresentationElement()}).
     * </p>
     *
     * @param baseUrl  the base URL of the OTF/post-live-DVR stream
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateSegmentTemplateElement(@Nonnull final String baseUrl) {
        try {
            final Element segmentTemplateElement = createElement(SEGMENT_TEMPLATE);

            // The first sequence of post DVR streams is the beginning of the video stream and not
            // an initialization segment
            appendNewAttrWithValue(
                    segmentTemplateElement, "startNumber", isLiveDelivery() ? "0" : "1");

            appendNewAttrWithValue(
                    segmentTemplateElement, "timescale", "1000");

            // Post-live-DVR/ended livestreams streams don't require an initialization sequence
            if (!isLiveDelivery()) {
                appendNewAttrWithValue(
                        segmentTemplateElement, "initialization", baseUrl + SQ_0);
            }

            appendNewAttrWithValue(
                    segmentTemplateElement, "media", baseUrl + "&sq=$Number$");

            getFirstElementByName(REPRESENTATION).appendChild(segmentTemplateElement);
        } catch (final DOMException e) {
            throw DashManifestCreationException.couldNotAddElement(SEGMENT_TEMPLATE, e);
        }
    }

    /**
     * Generate the {@code <SegmentTimeline>} element, appended as a child of the
     * {@code <SegmentTemplate>} element.
     *
     * <p>
     * The {@code <SegmentTemplate>} element needs to be generated before this element with
     * {@link #generateSegmentTemplateElement(String)}.
     * </p>
     *
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateSegmentTimelineElement()
            throws DashManifestCreationException {
        try {
            final Element segmentTemplateElement = getFirstElementByName(SEGMENT_TEMPLATE);
            final Element segmentTimelineElement = createElement(SEGMENT_TIMELINE);

            segmentTemplateElement.appendChild(segmentTimelineElement);
        } catch (final DOMException e) {
            throw DashManifestCreationException.couldNotAddElement(SEGMENT_TIMELINE, e);
        }
    }
    // endregion

    // region initResponse

    @SuppressWarnings("checkstyle:FinalParameters")
    @Nonnull
    protected Response getInitializationResponse(@Nonnull final String baseStreamingUrl) {
        final boolean isHtml5StreamingUrl = isWebStreamingUrl(baseStreamingUrl)
                || isTvHtml5SimplyEmbeddedPlayerStreamingUrl(baseStreamingUrl);
        final boolean isAndroidStreamingUrl = isAndroidStreamingUrl(baseStreamingUrl);
        final boolean isIosStreamingUrl = isIosStreamingUrl(baseStreamingUrl);

        String streamingUrl = baseStreamingUrl;
        if (isHtml5StreamingUrl) {
            streamingUrl += ALR_YES;
        }
        streamingUrl = appendBaseStreamingUrlParams(streamingUrl);

        final Downloader downloader = NewPipe.getDownloader();
        if (isHtml5StreamingUrl) {
            return getStreamingWebUrlWithoutRedirects(downloader, streamingUrl);
        } else if (isAndroidStreamingUrl || isIosStreamingUrl) {
            try {
                final Map<String, List<String>> headers = new HashMap<>();
                headers.put("User-Agent", Collections.singletonList(
                        isAndroidStreamingUrl
                                ? getAndroidUserAgent(null)
                                : getIosUserAgent(null)));
                final byte[] emptyBody = "".getBytes(StandardCharsets.UTF_8);
                return downloader.post(streamingUrl, headers, emptyBody);
            } catch (final IOException | ExtractionException e) {
                throw new DashManifestCreationException("Could not get the "
                        + (isIosStreamingUrl ? "IOS" : "ANDROID") + " streaming URL response", e);
            }
        }

        try {
            return downloader.get(streamingUrl);
        } catch (final IOException | ExtractionException e) {
            throw new DashManifestCreationException("Could not get the streaming URL response", e);
        }
    }

    @Nonnull
    protected Response getStreamingWebUrlWithoutRedirects(
            @Nonnull final Downloader downloader,
            @Nonnull final String streamingUrl) {
        try {
            final Map<String, List<String>> headers = new HashMap<>();
            addClientInfoHeaders(headers);

            String currentStreamingUrl = streamingUrl;

            for (int r = 0; r < MAXIMUM_REDIRECT_COUNT; r++) {
                final Response response = downloader.get(currentStreamingUrl, headers);

                final int responseCode = response.responseCode();
                if (responseCode != 200) {
                    throw new DashManifestCreationException(
                            "Could not get the initialization URL: HTTP response code "
                                    + responseCode);
                }

                // A valid HTTP 1.0+ response should include a Content-Type header, so we can
                // require that the response from video servers has this header.
                final String responseMimeType =
                        Objects.requireNonNull(
                                response.getHeader("Content-Type"),
                                "Could not get the Content-Type header from the response headers");

                // The response body is not the redirection URL
                if (!responseMimeType.equals("text/plain")) {
                    return response;
                }

                currentStreamingUrl = response.responseBody();
            }

            throw new DashManifestCreationException("Too many redirects");

        } catch (final IOException | ExtractionException e) {
            throw new DashManifestCreationException(
                    "Could not get the streaming URL response of a HTML5 client", e);
        }
    }

    @Nonnull
    protected String appendBaseStreamingUrlParams(@Nonnull final String baseStreamingUrl) {
        return baseStreamingUrl + SQ_0 + RN_0;
    }

    // endregion

    // region document util

    /**
     * Generate a new {@link DocumentBuilder} secured from XXE attacks, on platforms which
     * support setting {@link XMLConstants#ACCESS_EXTERNAL_DTD} and
     * {@link XMLConstants#ACCESS_EXTERNAL_SCHEMA} in {@link DocumentBuilderFactory} instances.
     */
    protected void newDocument() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (final Exception ignored) {
            // Ignore exceptions as setting these attributes to secure XML generation is not
            // supported by all platforms (like the Android implementation)
        }

        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
    }

    protected Attr appendNewAttrWithValue(
            final Element baseElement,
            final String name,
            final Object value
    ) {
        return appendNewAttrWithValue(baseElement, name, String.valueOf(value));
    }

    protected Attr appendNewAttrWithValue(
            final Element baseElement,
            final String name,
            final String value
    ) {
        final Attr attr = document.createAttribute(name);
        attr.setValue(value);
        baseElement.setAttributeNode(attr);

        return attr;
    }

    protected Element getFirstElementByName(final String name) {
        return (Element) document.getElementsByTagName(name).item(0);
    }

    protected Element createElement(final String name) {
        return document.createElement(name);
    }

    @SuppressWarnings("squid:S2755") // warning is still shown despite applied solution
    protected String documentToXml() throws TransformerException {
        /*
         * Generate a new {@link TransformerFactory} secured from XXE attacks, on platforms which
         * support setting {@link XMLConstants#ACCESS_EXTERNAL_DTD} and
         * {@link XMLConstants#ACCESS_EXTERNAL_SCHEMA} in {@link TransformerFactory} instances.
         */
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
        transformer.transform(new DOMSource(document), new StreamResult(result));

        return result.toString();
    }

    protected String documentToXmlSafe() {
        try {
            return documentToXml();
        } catch (final TransformerException e) {
            throw new DashManifestCreationException("Failed to convert XML-document to string", e);
        }
    }

    // endregion
}
