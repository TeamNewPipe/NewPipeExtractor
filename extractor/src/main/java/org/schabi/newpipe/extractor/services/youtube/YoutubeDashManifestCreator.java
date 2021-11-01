package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.*;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class YoutubeDashManifestCreator {

    private static final Pattern SEGMENT_DURATION_MS_PATTERN = Pattern.compile(
            "Segment-Durations-Ms: ((?:\\d+,\\d+,)?(?:\\d+\\(r=\\d+\\)(,\\d+)+,)+)");
    private static final String SQ_0 = "&sq=0";
    private static final String RN_0 = "&rn=0";
    private static final String ALR_YES = "&alr=yes";
    private static final String HEADM_1 = "&headm=1";

    private static final List<Integer> segmentsDuration = new ArrayList<>();
    private static final List<Integer> durationRepetitions = new ArrayList<>();
    private static final Map<String, String> otfManifestsGenerated = new HashMap<>();
    private static final Map<String, String> postLiveStreamsManifestsGenerated = new HashMap<>();

    private YoutubeDashManifestCreator() {
    }

    public static class YoutubeDashManifestCreationException extends Exception {
        YoutubeDashManifestCreationException(final String message) {
            super(message);
        }

        YoutubeDashManifestCreationException(final String message, final Exception e) {
            super(message, e);
        }
    }

    @Nonnull
    public static String createDashManifestFromOtfStreamingUrl(
            @Nonnull String otfBaseStreamingUrl,
            @Nonnull final ItagItem itagItem)
            throws YoutubeDashManifestCreationException {
        if (otfManifestsGenerated.containsKey(otfBaseStreamingUrl)) {
            return otfManifestsGenerated.get(otfBaseStreamingUrl);
        }

        final String originalOtfBaseStreamingUrl = otfBaseStreamingUrl;
        final String responseBody;
        try {
            // Try to avoid redirects when streaming the content by saving the last URL we get
            // from video servers.
            final Response response = getInitializationResponse(otfBaseStreamingUrl,
                    itagItem, false);
            otfBaseStreamingUrl = response.latestUrl().replace(SQ_0, EMPTY_STRING)
                    .replace(RN_0, EMPTY_STRING).replace(ALR_YES, EMPTY_STRING);

            final int responseCode = response.responseCode();
            if (responseCode != 200) {
                throw new YoutubeDashManifestCreationException(
                        "Unable to create the DASH manifest: could not get the initialization URL of the OTF stream: response code "
                                + responseCode);
            }
            responseBody = response.responseBody();
        } catch (final IOException | ExtractionException e) {
            throw new YoutubeDashManifestCreationException(
                    "Unable to create the DASH manifest: could not fetch the initialization URL of the OTF stream", e);
        }

        final String[] segmentDuration;

        try {
            final String segmentDurationMs = Parser.matchGroup1(SEGMENT_DURATION_MS_PATTERN,
                    responseBody);
            segmentDuration = segmentDurationMs.split(",");
        } catch (final Parser.RegexException e) {
            throw new YoutubeDashManifestCreationException(
                    "Unable to generate the DASH manifest: could not get the duration of segments", e);
        }

        final Document document = generateDocumentAndMpdElement(segmentDuration, false);
        generatePeriodElement(document);
        generateAdaptationSetElement(document, itagItem.getMediaFormat().mimeType);
        generateRoleElement(document);
        generateRepresentationElement(document, itagItem);
        if (itagItem.itagType == ItagItem.ItagType.AUDIO) {
            generateAudioChannelConfigurationElement(document, itagItem);
        }
        generateSegmentTemplateElement(document, otfBaseStreamingUrl, false);
        generateSegmentTimelineElement(document);
        collectSegmentsData(segmentDuration);
        generateSegmentElementsForOtfStreams(document);

        return buildResult(originalOtfBaseStreamingUrl, document, otfManifestsGenerated);
    }

    @Nonnull
    public static String createDashManifestFromPostLiveStreamDvrStreamingUrl(
            @Nonnull String postLiveStreamDvrStreamingUrl,
            @Nonnull final ItagItem itagItem,
            final int targetDurationSec)
            throws YoutubeDashManifestCreationException {
        if (postLiveStreamsManifestsGenerated.containsKey(postLiveStreamDvrStreamingUrl)) {
            return postLiveStreamsManifestsGenerated.get(postLiveStreamDvrStreamingUrl);
        }
        final String originalPostLiveStreamDvrStreamingUrl = postLiveStreamDvrStreamingUrl;
        final String streamDuration;
        final String segmentCount;

        if (targetDurationSec <= 0) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: the targetDurationSec value is less than or equal to 0 (" + targetDurationSec + ")");
        }

        try {
            // Try to avoid redirects when streaming the content by saving the latest URL we get
            // from video servers.
            final Response response = getInitializationResponse(postLiveStreamDvrStreamingUrl,
                    itagItem, true);
            postLiveStreamDvrStreamingUrl = response.latestUrl().replace(SQ_0, EMPTY_STRING)
                    .replace(RN_0, EMPTY_STRING).replace(ALR_YES, EMPTY_STRING);

            final int responseCode = response.responseCode();
            if (responseCode != 200) {
                throw new YoutubeDashManifestCreationException(
                        "Unable to create the DASH manifest: could not get the initialization URL of the post live DVR stream: response code "
                                + responseCode);
            }

            final Map<String, List<String>> responseHeaders = response.responseHeaders();
            streamDuration = responseHeaders.get("X-Head-Time-Millis").get(0);
            segmentCount = responseHeaders.get("X-Head-Seqnum").get(0);
        } catch (final IOException | IndexOutOfBoundsException | ExtractionException e) {
            throw new YoutubeDashManifestCreationException(
                    "Unable to generate the DASH manifest: could not get the initialization URL of the post live DVR stream", e);
        }

        if (isNullOrEmpty(streamDuration)) {
            throw new YoutubeDashManifestCreationException(
                    "Unable to generate the DASH manifest: could not get the duration of the stream of the post live DVR stream");
        }
        if (isNullOrEmpty(segmentCount)) {
            throw new YoutubeDashManifestCreationException(
                    "Unable to generate the DASH manifest: could not get the number of segments of the post live DVR stream");
        }

        final Document document = generateDocumentAndMpdElement(new String[] { streamDuration },
                true);
        generatePeriodElement(document);
        generateAdaptationSetElement(document, itagItem.getMediaFormat().mimeType);
        generateRoleElement(document);
        generateRepresentationElement(document, itagItem);
        if (itagItem.itagType == ItagItem.ItagType.AUDIO) {
            generateAudioChannelConfigurationElement(document, itagItem);
        }
        generateSegmentTemplateElement(document, postLiveStreamDvrStreamingUrl, true);
        generateSegmentTimelineElement(document);
        generateSegmentElementForPostLiveDvrStreams(document, targetDurationSec, segmentCount);

        return buildResult(originalPostLiveStreamDvrStreamingUrl, document, postLiveStreamsManifestsGenerated);
    }

    @Nonnull
    private static Response getInitializationResponse(@Nonnull String initializationUrl,
                                                      @Nonnull final ItagItem itagItem,
                                                      final boolean isAPostLiveDvrStreamingUrl)
            throws IOException, ExtractionException, YoutubeDashManifestCreationException {
        final boolean isAWebStreamingUrl = isWebStreamingUrl(initializationUrl);
        final boolean isAnAndroidStreamingUrl = isAndroidStreamingUrl(initializationUrl);
        if (isAWebStreamingUrl) {
            initializationUrl += ALR_YES + SQ_0 + RN_0;
        } else if (isAnAndroidStreamingUrl && isAPostLiveDvrStreamingUrl) {
            initializationUrl += SQ_0 + RN_0 + HEADM_1;
        } else {
            initializationUrl += SQ_0 + RN_0;
        }

        final Downloader downloader = NewPipe.getDownloader();
        if (isAWebStreamingUrl) {
            final String mimeTypeExpected = itagItem.getMediaFormat().getMimeType();
            final Map<String, List<String>> headers = new HashMap<>();
            addClientInfoHeaders(headers);
            if (!isNullOrEmpty(mimeTypeExpected)) {
                String responseMimeType = "";
                while (!responseMimeType.equals(mimeTypeExpected)) {
                    final Response response = downloader.get(initializationUrl, headers);
                    final int responseCode = response.responseCode();
                    if (responseCode != 200) {
                        throw new YoutubeDashManifestCreationException(
                                "Unable to create the DASH manifest: could not get the initialization URL of the post live DVR stream: response code "
                                        + responseCode);
                    }

                    // A valid response must include a Content-Type header, so we can require that
                    // the response from video servers has this header.
                    try {
                        responseMimeType = Objects.requireNonNull(response.getHeader(
                                "Content-Type"));
                    } catch (final NullPointerException e) {
                        throw new YoutubeDashManifestCreationException(
                                "Unable to create the DASH manifest: could not get the Content-Type header from the initialization URL", e);
                    }

                    // The response body is the redirect URL
                    if (responseMimeType.equals("text/plain")) {
                        initializationUrl = response.responseBody();
                    } else {
                        return response;
                    }
                }
            }
        } else if (isAnAndroidStreamingUrl && isAPostLiveDvrStreamingUrl) {
            final Map<String, List<String>> headers = new HashMap<>();
            headers.put("User-Agent", Collections.singletonList(
                    getYoutubeAndroidAppUserAgent(null)));
            final byte[] emptyBody = "".getBytes(StandardCharsets.UTF_8);
            return downloader.post(initializationUrl, headers, emptyBody);
        }
        final Map<String, List<String>> headers = new HashMap<>();
        if (isAnAndroidStreamingUrl) {
            headers.put("User-Agent", Collections.singletonList(
                    getYoutubeAndroidAppUserAgent(null)));
        }

        return downloader.get(initializationUrl, headers);
    }

    /**
     * Collect all the segments from an OTF stream.
     *
     * @param segmentDuration the string array which contains all the sequences extracted with the
     *                        regular expression ({@link #SEGMENT_DURATION_MS_PATTERN})
     * @throws YoutubeDashManifestCreationException if something went wrong when trying to collect
     * the segments of the OTF stream
     */
    private static void collectSegmentsData(@Nonnull final String[] segmentDuration)
            throws YoutubeDashManifestCreationException {
        segmentsDuration.clear();
        durationRepetitions.clear();

        try {
            for (final String segDuration : segmentDuration) {
                final String[] segmentLengthRepeat = segDuration.split("\\(r=");
                int segmentRepeatCount = 0;
                // There are repetitions of a segment duration in other segments
                if (segmentLengthRepeat.length > 1) {
                    segmentRepeatCount = Integer.parseInt(Utils.removeNonDigitCharacters(
                            segmentLengthRepeat[1]));
                }
                final int segmentLength = Integer.parseInt(segmentLengthRepeat[0]);
                segmentsDuration.add(segmentLength);
                durationRepetitions.add(segmentRepeatCount);
            }
        } catch (final NumberFormatException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: unable to get the segments of the stream", e);
        }
    }

    /**
     * Get the duration of an OTF stream.
     *
     * The duration of OTF streams is not returned into the player response and needs to be
     * calculated by adding the duration of each segment.
     *
     * @param segmentDuration the segment duration object extracted from the initialization
     *                        sequence of the stream
     * @return the duration of the OTF stream
     * @throws YoutubeDashManifestCreationException if something went wrong when parsing the
     * {@code segmentDuration} object
     */
    private static int getStreamDuration(@Nonnull final String[] segmentDuration)
            throws YoutubeDashManifestCreationException {
        try {
            int streamLengthMs = 0;
            for (final String segDuration : segmentDuration) {
                final String[] segmentLengthRepeat = segDuration.split("\\(r=");
                int segmentRepeatCount = 0;
                // There are repetitions of a segment duration in other segments
                if (segmentLengthRepeat.length > 1) {
                    segmentRepeatCount = Integer.parseInt(Utils.removeNonDigitCharacters(
                            segmentLengthRepeat[1]));
                }
                final int segmentLength = Integer.parseInt(segmentLengthRepeat[0]);
                streamLengthMs += segmentLength + segmentRepeatCount * segmentLength;
            }
            return streamLengthMs;
        } catch (final NumberFormatException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: unable to get the length of the stream", e);
        }
    }

    /**
     * Create a {@link Document} object and generate the {@code <MPD>} element of the manifest.
     *
     * <p>
     * The generated {@code <MPD>} element looks like the manifest returned into the player
     * response of videos with OTF streams:
     * </p>
     * <p>
     * {@code <MPD xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xmlns="urn:mpeg:DASH:schema:MPD:2011"
     * xsi:schemaLocation="urn:mpeg:DASH:schema:MPD:2011 DASH-MPD.xsd" minBufferTime="PT1.500S"
     * profiles="urn:mpeg:dash:profile:isoff-main:2011" type="static"
     * mediaPresentationDuration="PT$duration$S">}
     * (where {@code $duration$} represents the duration in seconds (a number with 3 digits after
     * the decimal point)
     * </p>
     * <p>
     * If the duration is an integer or a double with less than 3 digits after the decimal point,
     * it will be converted into a double with 3 digits after the decimal point.
     * </p>
     *
     * @param segmentDuration     the segment duration object extracted from the initialization
     *                            sequence of the stream
     * @param isPostLiveDvrStream if the stream is a post live stream ({@code true}) or an OTF
     *                            stream ({@code false})
     * @return a {@link Document} object which contains a {@code <MPD>} element
     * @throws YoutubeDashManifestCreationException if something went wrong when
     * generating/appending the {@link Document object} or the {@code <MPD>} element
     */
    private static Document generateDocumentAndMpdElement(@Nonnull final String[] segmentDuration,
                                                          final boolean isPostLiveDvrStream)
            throws YoutubeDashManifestCreationException {
        final DocumentBuilderFactory dbFactory;
        final DocumentBuilder documentBuilder;
        final Document document;
        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = dbFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();

            final Element mpdElement = document.createElement("MPD");
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
            profilesAttribute.setValue("urn:mpeg:dash:profile:isoff-main:2011");
            mpdElement.setAttributeNode(profilesAttribute);

            final Attr typeAttribute = document.createAttribute("type");
            typeAttribute.setValue("static");
            mpdElement.setAttributeNode(typeAttribute);

            final Attr mediaPresentationDurationAttribute = document.createAttribute(
                    "mediaPresentationDuration");
            final int streamDuration;
            if (isPostLiveDvrStream) {
                streamDuration = Integer.parseInt(segmentDuration[0]);
            } else {
                streamDuration = getStreamDuration(segmentDuration);
            }
            final double duration = streamDuration / 1000.0;
            final String durationSeconds = String.format(Locale.ENGLISH, "%.3f", duration);
            mediaPresentationDurationAttribute.setValue("PT" + durationSeconds + "S");
            mpdElement.setAttributeNode(mediaPresentationDurationAttribute);
        } catch (final Exception e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the MPD element of the DASH manifest", e);
        }

        return document;
    }

    /**
     * Generate the {@code <Period>} element, appended as a child of the {@code <MPD>} element.
     *
     * <p>
     * The {@code <MPD>} element needs to be generated before this element with
     * {@link #generateDocumentAndMpdElement(String[], boolean)}).
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <Period>} element will be
     *                 appended
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     * appending the {@code <Period>} element to the document
     */
    private static void generatePeriodElement(@Nonnull final Document document)
            throws YoutubeDashManifestCreationException {
        try {
            final Element mpdElement = (Element) document.getElementsByTagName("MPD").item(0);
            final Element periodElement = document.createElement("Period");
            mpdElement.appendChild(periodElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the Period element of the DASH manifest", e);
        }
    }

    /**
     * Generate the {@code <Period>} element, appended as a child of the {@code <MPD>} element.
     *
     * <p>
     * The {@code <MPD>} element needs to be generated before this element with
     * {@link #generateDocumentAndMpdElement(String[], boolean)}).
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <Period>} element will be
     *                 appended
     * @param mimeType the mime type of the stream (which needs to be got with
     *                 {@link ItagItem#getMediaFormat} and
     *                 {@link org.schabi.newpipe.extractor.MediaFormat#getMimeType
     *                 MediaFormat.getMimeType})
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     * appending the {@code <Period>} element to the document
     */
    private static void generateAdaptationSetElement(@Nonnull final Document document,
                                                     @Nonnull final String mimeType)
            throws YoutubeDashManifestCreationException {
        try {
            final Element periodElement = (Element) document.getElementsByTagName("Period").item(0);
            final Element adaptationSetElement = document.createElement("AdaptationSet");

            final Attr idAttribute = document.createAttribute("id");
            idAttribute.setValue("0");
            adaptationSetElement.setAttributeNode(idAttribute);

            final Attr mimeTypeAttribute = document.createAttribute("mimeType");
            mimeTypeAttribute.setValue(mimeType);
            adaptationSetElement.setAttributeNode(mimeTypeAttribute);

            final Attr subsegmentAlignmentAttribute = document.createAttribute("subsegmentAlignment");
            subsegmentAlignmentAttribute.setValue("true");
            adaptationSetElement.setAttributeNode(subsegmentAlignmentAttribute);

            periodElement.appendChild(adaptationSetElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the AdaptationSet element of the DASH manifest", e);
        }
    }

    /**
     * Generate the {@code <Role>} element, appended as a child of the {@code <AdaptationSet>}
     * element.
     *
     * <p>
     * This element, with its attributes and values, is:
     * </p>
     * <p>
     * {@code <Role schemeIdUri="urn:mpeg:DASH:role:2011" value="main"/>}
     * </p>
     * <p>
     * The {@code <AdaptationSet>} element needs to be generated before this element with
     * {@link #generateAdaptationSetElement(Document, String)}).
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <Role>} element will be
     *                 appended
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     * appending the {@code <Role>} element to the document
     */
    private static void generateRoleElement(@Nonnull final Document document)
            throws YoutubeDashManifestCreationException {
        try {
            final Element adaptationSetElement = (Element) document.getElementsByTagName(
                    "AdaptationSet").item(0);
            final Element roleElement = document.createElement("Role");

            final Attr schemeIdUriAttribute = document.createAttribute("schemeIdUri");
            schemeIdUriAttribute.setValue("urn:mpeg:DASH:role:2011");
            roleElement.setAttributeNode(schemeIdUriAttribute);

            final Attr valueAttribute = document.createAttribute("value");
            valueAttribute.setValue("main");
            roleElement.setAttributeNode(valueAttribute);

            adaptationSetElement.appendChild(roleElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the Role element of the DASH manifest", e);
        }
    }

    private static void generateRepresentationElement(@Nonnull final Document document,
                                                      @Nonnull final ItagItem itagItem)
            throws YoutubeDashManifestCreationException {
        try {
            final Element adaptationSetElement = (Element) document.getElementsByTagName(
                    "AdaptationSet").item(0);
            final Element representationElement = document.createElement("Representation");

            final Attr idAttribute = document.createAttribute("id");
            idAttribute.setValue(String.valueOf(itagItem.id));
            representationElement.setAttributeNode(idAttribute);

            final Attr codecsAttribute = document.createAttribute("codecs");
            codecsAttribute.setValue(itagItem.getCodec());
            representationElement.setAttributeNode(codecsAttribute);

            final Attr widthAttribute = document.createAttribute("width");
            widthAttribute.setValue(String.valueOf(itagItem.getWidth()));
            representationElement.setAttributeNode(widthAttribute);

            final Attr heightAttribute = document.createAttribute("height");
            heightAttribute.setValue(String.valueOf(itagItem.getHeight()));
            representationElement.setAttributeNode(heightAttribute);

            final Attr startWithSAPAttribute = document.createAttribute("startWithSAP");
            startWithSAPAttribute.setValue("1");
            representationElement.setAttributeNode(startWithSAPAttribute);

            final Attr maxPlayoutRateAttribute = document.createAttribute("maxPlayoutRate");
            maxPlayoutRateAttribute.setValue("1");
            representationElement.setAttributeNode(maxPlayoutRateAttribute);

            final Attr bandwidthAttribute = document.createAttribute("bandwidth");
            bandwidthAttribute.setValue(String.valueOf(itagItem.getBitrate()));
            representationElement.setAttributeNode(bandwidthAttribute);

            final ItagItem.ItagType itagType = itagItem.itagType;

            if ((itagType == ItagItem.ItagType.VIDEO || itagType == ItagItem.ItagType.VIDEO_ONLY)
                    && itagItem.fps > 0) {
                final Attr frameRateAttribute = document.createAttribute("frameRate");
                frameRateAttribute.setValue(String.valueOf(itagItem.fps));
                representationElement.setAttributeNode(frameRateAttribute);
            }

            if (itagType == ItagItem.ItagType.AUDIO && itagItem.getSampleRate() > 0) {
                final Attr audioSamplingRateAttribute = document.createAttribute(
                        "audioSamplingRate");
                audioSamplingRateAttribute.setValue(String.valueOf(itagItem.getSampleRate()));
            }

            adaptationSetElement.appendChild(representationElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the Representation element of the DASH manifest", e);
        }
    }

    private static void generateAudioChannelConfigurationElement(
            @Nonnull final Document document,
            @Nonnull final ItagItem itagItem) throws YoutubeDashManifestCreationException {
        try {
            final Element representationElement = (Element) document.getElementsByTagName(
                    "Representation").item(0);
            final Element audioChannelConfigurationElement = document.createElement(
                    "AudioChannelConfiguration");

            final Attr schemeIdUriAttribute = document.createAttribute("schemeIdUri");
            schemeIdUriAttribute.setValue(
                    "urn:mpeg:dash:23003:3:audio_channel_configuration:2011");
            audioChannelConfigurationElement.setAttributeNode(schemeIdUriAttribute);

            final Attr valueAttribute = document.createAttribute("value");
            final int audioChannels = itagItem.getAudioChannels();
            if (audioChannels <= 0) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: the audioChannels value is less than or equal to 0 (" + audioChannels + ")");
            }
            valueAttribute.setValue(String.valueOf(itagItem.getAudioChannels()));
            audioChannelConfigurationElement.setAttributeNode(valueAttribute);

            representationElement.appendChild(audioChannelConfigurationElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the AudioChannelConfiguration element of the DASH manifest", e);
        }
    }

    private static void generateSegmentTemplateElement(@Nonnull final Document document,
                                                       @Nonnull final String baseUrl,
                                                       final boolean isPostLiveDvr)
            throws YoutubeDashManifestCreationException {
        try {
            final Element representationElement = (Element) document.getElementsByTagName(
                    "Representation").item(0);
            final Element segmentTemplateElement = document.createElement("SegmentTemplate");

            final Attr startNumberAttribute = document.createAttribute("startNumber");
            final String startNumberValue = isPostLiveDvr ? "0" : "1";
            startNumberAttribute.setValue(startNumberValue);
            segmentTemplateElement.setAttributeNode(startNumberAttribute);

            final Attr timescaleAttribute = document.createAttribute("timescale");
            timescaleAttribute.setValue("1000");
            segmentTemplateElement.setAttributeNode(timescaleAttribute);

            final Attr initializationAttribute = document.createAttribute("initialization");
            initializationAttribute.setValue(baseUrl + SQ_0 + RN_0);
            segmentTemplateElement.setAttributeNode(initializationAttribute);

            final Attr mediaAttribute = document.createAttribute("media");
            mediaAttribute.setValue(baseUrl + "&sq=$Number$&rn=$Number$");
            segmentTemplateElement.setAttributeNode(mediaAttribute);

            representationElement.appendChild(segmentTemplateElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the SegmentTemplate element of the DASH manifest", e);
        }
    }

    /**
     * Generate the {@code <SegmentTimeline>} element, appended as a child of the
     * {@code <SegmentTemplate>} element.
     *
     * <p>
     * The {@code <SegmentTemplate>} element needs to be generated before this element with
     * {@link #generateSegmentTemplateElement(Document, String, boolean)}).
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <SegmentTimeline>} element will
     *                 be appended
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     * appending the {@code <SegmentTimeline>} element to the document
     */
    private static void generateSegmentTimelineElement(@Nonnull final Document document)
            throws YoutubeDashManifestCreationException {
        try {
            final Element segmentTemplateElement = (Element) document.getElementsByTagName(
                    "SegmentTemplate").item(0);
            final Element segmentTimelineElement = document.createElement("SegmentTimeline");

            segmentTemplateElement.appendChild(segmentTimelineElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the SegmentTimeline element of the DASH manifest", e);
        }
    }

    private static void generateSegmentElementsForOtfStreams(@Nonnull final Document document)
            throws YoutubeDashManifestCreationException {
        try {
            if (isNullOrEmpty(segmentsDuration) || isNullOrEmpty(durationRepetitions)) {
                throw new IllegalStateException(
                        "Duration of segments and/or repetition(s) of segments are unknown");
            }
            final Element segmentTimelineElement = (Element) document.getElementsByTagName(
                    "SegmentTimeline").item(0);

            for (int i = 0; i < segmentsDuration.size(); i++) {
                final Element sElement = document.createElement("S");

                final int durationRepetition = durationRepetitions.get(i);
                if (durationRepetition != 0) {
                    final Attr rAttribute = document.createAttribute("r");
                    rAttribute.setValue(String.valueOf(durationRepetition));
                    sElement.setAttributeNode(rAttribute);
                }

                final Attr dAttribute = document.createAttribute("d");
                dAttribute.setValue(String.valueOf(segmentsDuration.get(i)));
                sElement.setAttributeNode(dAttribute);

                segmentTimelineElement.appendChild(sElement);
            }

        } catch (final DOMException | IllegalStateException | IndexOutOfBoundsException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the Segment elements of the DASH manifest", e);
        }
    }

    private static void generateSegmentElementForPostLiveDvrStreams(
            @Nonnull final Document document,
            final int targetDurationSeconds,
            final String segmentCount) throws YoutubeDashManifestCreationException {
        try {
            final Element segmentTimelineElement = (Element) document.getElementsByTagName(
                    "SegmentTimeline").item(0);
            final Element sElement = document.createElement("S");

            final Attr dAttribute = document.createAttribute("d");
            dAttribute.setValue(String.valueOf(targetDurationSeconds * 1000));
            sElement.setAttributeNode(dAttribute);

            final Attr rAttribute = document.createAttribute("r");
            rAttribute.setValue(String.valueOf(segmentCount));
            sElement.setAttributeNode(rAttribute);

            segmentTimelineElement.appendChild(sElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the Segment elements of the DASH manifest", e);
        }
    }

    /**
     * Convert a DASH manifest {@link Document document} to a string.
     *
     * @param originalBaseStreamingUrl    the original base URL of the stream
     * @param document                    the document to be converted
     * @param mapOfGeneratedManifestsType the {@link Map} on which store the string generated
     *                                    (which is either {@link #otfManifestsGenerated} or
     *                                    {@link #postLiveStreamsManifestsGenerated})
     * @return the DASH manifest {@link Document document} converted to a string
     * @throws YoutubeDashManifestCreationException if something went wrong when converting the
     * {@link Document document}
     */
    private static String buildResult(
            @Nonnull final String originalBaseStreamingUrl,
            @Nonnull final Document document,
            @Nonnull final Map<String, String> mapOfGeneratedManifestsType)
            throws YoutubeDashManifestCreationException {
        try {
            final StringWriter result = new StringWriter();
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            transformer.transform(new DOMSource(document), new StreamResult(result));
            final String stringResult = result.toString();
            mapOfGeneratedManifestsType.put(originalBaseStreamingUrl, stringResult);
            return stringResult;
        } catch (final TransformerException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not convert the DASH manifest generated to a string", e);
        }
    }

    /**
     * Get the number of cached OTF streams manifests.
     *
     * @return the number of cached OTF streams manifests
     */
    public static int getOtfCachedManifestsSize() {
        return otfManifestsGenerated.size();
    }

    /**
     * Get the number of cached post live streams manifests.
     *
     * @return the number of cached post live streams manifests
     */
    public static int getPostLiveStreamsCachedManifestsSize() {
        return postLiveStreamsManifestsGenerated.size();
    }

    /**
     * Clear the cached OTF manifests.
     */
    public static void clearOtfCachedManifests() {
        otfManifestsGenerated.clear();
    }

    /**
     * Clear the cached post live streams manifests.
     */
    public static void clearPostLiveStreamsCachedManifests() {
        postLiveStreamsManifestsGenerated.clear();
    }

    /**
     * Clear the cached OTF manifests and the cached post live streams manifests.
     */
    public static void clearManifestsInCache() {
        otfManifestsGenerated.clear();
        postLiveStreamsManifestsGenerated.clear();
    }
}
