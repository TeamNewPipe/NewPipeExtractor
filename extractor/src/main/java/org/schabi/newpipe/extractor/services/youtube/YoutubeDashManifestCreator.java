package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.MediaFormat;
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

/**
 * Class to generate DASH manifests from YouTube OTF, progressive and ended/post live DVR streams.
 *
 * <p>
 * It relies on external classes from {@link org.w3c.dom} and {@link javax.xml} packages.
 * </p>
 */
public final class YoutubeDashManifestCreator {

    /**
     * A {@link Pattern} to find the {@code Segment-Durations-Ms:} string and its value in the
     * initialization sequence of an OTF stream.
     */
    private static final Pattern SEGMENT_DURATION_MS_PATTERN = Pattern.compile(
            "Segment-Durations-Ms: ((?:\\d+,\\d+,)?(?:\\d+\\(r=\\d+\\)(,\\d+)+,)+)");

    /**
     * URL parameter of the first sequence for live, post live DVR and OTF streams.
     */
    private static final String SQ_0 = "&sq=0";

    /**
     * URL parameter of the first stream request made by official clients.
     */
    private static final String RN_0 = "&rn=0";

    /**
     * URL parameter specific to web clients. When this param is added, if a redirection occurs,
     * the server will not redirect clients to the redirect URL but will provide this URL as the
     * response body.
     */
    private static final String ALR_YES = "&alr=yes";

    /**
     * URL parameter specific to Android clients, only used as a "header" parameter for the first
     * sequence for post live DVR streams.
     *
     *
     * <p>
     * Android clients use POST request with a protobuf body in streaming URL requests.
     * </p>
     * <p>
     * The same behavior happens without this param when using a POST request, but it's better to
     * use it in order to spoof better official clients. Otherwise it has no effect.
     * </p>
     */
    private static final String HEADM_1 = "&headm=1";

    /**
     * The redirect count limit that this class uses, which is the same limit as OkHttp.
     */
    private static final int MAXIMUM_REDIRECT_COUNT = 20;

    /**
     * A list of segments duration of an OTF stream.
     *
     * <p>
     * This list is automatically cleared in the execution of
     * {@link #createDashManifestFromOtfStreamingUrl(String, ItagItem, long)}, before the DASH
     * manifest is converted to a string.
     * </p>
     */
    private static final List<Integer> SEGMENTS_DURATION = new ArrayList<>();

    /**
     * A list of duration repetitions of an OTF stream.
     *
     * <p>
     * This list is automatically cleared in the execution of
     * {@link #createDashManifestFromOtfStreamingUrl(String, ItagItem, long)}, before the DASH
     * manifest is converted to a string.
     * </p>
     */
    private static final List<Integer> DURATION_REPETITIONS = new ArrayList<>();

    /**
     * Cache of DASH manifests generated for OTF streams.
     */
    private static final Map<String, String> OTF_MANIFESTS_GENERATED = new HashMap<>();

    /**
     * Cache of DASH manifests generated for post live DVR streams.
     */
    private static final Map<String, String> POST_LIVE_STREAMS_MANIFESTS_GENERATED =
            new HashMap<>();

    /**
     * Cache of DASH manifests generated for progressive streams.
     */
    private static final Map<String, String> PROGRESSIVE_STREAMS_MANIFESTS_GENERATED =
            new HashMap<>();

    /**
     * Enum of streaming format types used by YouTube in their streams.
     */
    private enum DeliveryType {
        /**
         * YouTube's progressive delivery method, which work with HTTP range headers (but the
         * corresponding parameter is used instead by official clients).
         *
         * <p>
         * Initialization and index ranges are available to get metadata (the corresponding values
         * are returned in the player response).
         * </p>
         */
        PROGRESSIVE,
        /**
         * YouTube's OTF delivery method which uses a sequence parameter to get segments of
         * streams.
         *
         * <p>
         * The first sequence (which can be get with the {@link #SQ_0} param) contains all the
         * metadata needed to build the stream source (sidx
         * boxes, segments length, segment count, duration, ...)
         * </p>
         * <p>
         * Only used for videos (mostly ones with a small amount of views or ended livestreams
         * which have been just re-encoded as normal videos).
         * </p>
         */
        OTF,
        /**
         * YouTube's delivery method for livestreams which uses a sequence parameter to get
         * segments of streams.
         *
         * <p>
         * Each sequence (which can be get with the {@link #SQ_0} param) contains its own
         * metadata (sidx boxes, segment length, ...), which make no need of an initialization
         * segment.
         * </p>
         * <p>
         * Only used for livestreams (ended or running).
         * </p>
         */
        LIVE
    }

    private YoutubeDashManifestCreator() {
    }

    /**
     * Exception class of {@link YoutubeDashManifestCreator}.
     */
    public static final class YoutubeDashManifestCreationException extends Exception {
        YoutubeDashManifestCreationException(final String message) {
            super(message);
        }

        YoutubeDashManifestCreationException(final String message, final Exception e) {
            super(message, e);
        }
    }

    /**
     * Create DASH manifests from a YouTube OTF stream.
     *
     * <p>
     * OTF streams is one of the YouTube DASH specific streams which works with sequences and
     * without the need to get a manifest (even if one is provided but not used by main clients).
     * They can be found only on videos (mostly ones with a small amount of views or ended
     * livestreams which have been just re-encoded as normal videos).
     * </p>
     *
     * <p>This method needs:
     *     <ul>
     *         <li>the base URL of the stream (which returns HTTP code 404, after redirects and if
     *         the URL is valid, if you try to access to it);</li>
     *         <li>an {@link ItagItem} which needs to contain the following information:
     *              <ul>
     *                  <li>its type (see {@link ItagItem.ItagType}), to identify if the content is
     *                  an audio or a video stream</li>
     *                  <li>its bitrate;</li>
     *                  <li>its mime type;</li>
     *                  <li>its codec(s);</li>
     *                  <li>for audio streams: its audio channels;</li>
     *                  <li>for video streams: its width and height.</li>
     *              </ul>
     *         </li>
     *         <li>the duration of the video, which will be used if the duration could not be
     *         parsed from the first sequence of the stream.</li>
     *     </ul>
     * </p>
     *
     * <p>In order to generate the DASH manifest, this method will:
     *      <ul>
     *          <li>request the first sequence of the stream (the base URL on which the first
     *          sequence parameters are appended (see {@link #RN_0} and {@link #SQ_0})) with a POST
     *          or GET request (depending of the client on which the streaming URL comes from);
     *          </li>
     *          <li>follow its redirection(s), if there is one or more;</li>
     *          <li>save the last URL, remove the first sequence parameters;</li>
     *          <li>use the information provided in the {@link ItagItem} to generate all
     *          elements of the DASH manifest.</li>
     *      </ul>
     * </p>
     *
     * <p>If the duration could not be extracted, the {@code durationSecondsFallback} value will be
     * used as the stream duration.</p>
     *
     * @param otfBaseStreamingUrl     the base URL of the OTF stream, which cannot be null
     * @param itagItem                the {@link ItagItem} corresponding to the stream, which
     *                                cannot be null
     * @param durationSecondsFallback the duration of the video which will be used if the duration
     *                                could not be extracted from the first sequence
     * @return the manifest generated into a string
     * @throws YoutubeDashManifestCreationException if something went wrong when trying to generate
     *                                              the DASH manifest
     */
    @Nonnull
    public static String createDashManifestFromOtfStreamingUrl(
            @Nonnull String otfBaseStreamingUrl,
            @Nonnull final ItagItem itagItem,
            final long durationSecondsFallback)
            throws YoutubeDashManifestCreationException {
        if (OTF_MANIFESTS_GENERATED.containsKey(otfBaseStreamingUrl)) {
            return OTF_MANIFESTS_GENERATED.get(otfBaseStreamingUrl);
        }

        final String originalOtfBaseStreamingUrl = otfBaseStreamingUrl;
        final String responseBody;
        // Try to avoid redirects when streaming the content by saving the last URL we get
        // from video servers.
        final Response response = getInitializationResponse(otfBaseStreamingUrl,
                itagItem, DeliveryType.OTF);
        otfBaseStreamingUrl = response.latestUrl().replace(SQ_0, EMPTY_STRING)
                .replace(RN_0, EMPTY_STRING).replace(ALR_YES, EMPTY_STRING);

        final int responseCode = response.responseCode();
        if (responseCode != 200) {
            throw new YoutubeDashManifestCreationException(
                    "Unable to create the DASH manifest: could not get the initialization URL of the OTF stream: response code "
                            + responseCode);
        }
        responseBody = response.responseBody();

        final String[] segmentDuration;

        try {
            final String segmentDurationMs = Parser.matchGroup1(SEGMENT_DURATION_MS_PATTERN,
                    responseBody);
            segmentDuration = segmentDurationMs.split(",");
        } catch (final Parser.RegexException e) {
            throw new YoutubeDashManifestCreationException(
                    "Unable to generate the DASH manifest: could not get the duration of segments", e);
        }

        final Document document = generateDocumentAndMpdElement(segmentDuration, DeliveryType.OTF,
                itagItem, durationSecondsFallback);
        generatePeriodElement(document);
        generateAdaptationSetElement(document, itagItem);
        generateRoleElement(document);
        generateRepresentationElement(document, itagItem);
        if (itagItem.itagType == ItagItem.ItagType.AUDIO) {
            generateAudioChannelConfigurationElement(document, itagItem);
        }
        generateSegmentTemplateElement(document, otfBaseStreamingUrl, DeliveryType.OTF);
        generateSegmentTimelineElement(document);
        collectSegmentsData(segmentDuration);
        generateSegmentElementsForOtfStreams(document);

        SEGMENTS_DURATION.clear();
        DURATION_REPETITIONS.clear();

        return buildResult(originalOtfBaseStreamingUrl, document, OTF_MANIFESTS_GENERATED);
    }

    /**
     * Create DASH manifests from a YouTube post live DVR stream/ended livestream.
     *
     * <p>
     * Post live DVR streams/ended livestreams is one of the YouTube DASH specific streams which
     * works with sequences and without the need to get a manifest (even if one is provided but not
     * used by main clients (and is complete for big ended livestreams because it doesn't return
     * the full stream)).
     * </p>
     *
     * <p>
     * They can be found only on livestreams which have ended for a short amount of time (most of
     * times a few hours).
     * </p>
     *
     * <p>This method needs:
     *     <ul>
     *         <li>the base URL of the stream (which returns HTTP code 404, after redirects and if
     *         the URL is valid, if you try to access to it);</li>
     *         <li>an {@link ItagItem} which needs to contain the following information:
     *              <ul>
     *                  <li>its type (see {@link ItagItem.ItagType}), to identify if the content is
     *                  an audio or a video stream</li>
     *                  <li>its bitrate;</li>
     *                  <li>its mime type;</li>
     *                  <li>its codec(s);</li>
     *                  <li>for audio streams: its audio channels;</li>
     *                  <li>for video streams: its width and height.</li>
     *              </ul>
     *         </li>
     *         <li>the duration of the video, which will be used if the duration could not be
     *         parsed from the first sequence of the stream.</li>
     *     </ul>
     * </p>
     *
     * <p>In order to generate the DASH manifest, this method will:
     *      <ul>
     *          <li>request the first sequence of the stream (the base URL on which the first
     *          sequence parameters are appended (see {@link #RN_0} and {@link #SQ_0})) with a POST
     *          or GET request (depending of the client on which the streaming URL comes from);
     *          </li>
     *          <li>follow its redirection(s), if there is one or more;</li>
     *          <li>save the last URL, remove the first sequence parameters;</li>
     *          <li>use the information provided in the {@link ItagItem} to generate all
     *          elements of the DASH manifest.</li>
     *      </ul>
     * </p>
     *
     * <p>If the duration could not be extracted, the {@code durationSecondsFallback} value will be
     * used as the stream duration.</p>
     *
     * @param postLiveStreamDvrStreamingUrl the base URL of the post live DVR stream/ended
     *                                      livestream, which cannot be null
     * @param itagItem                      the {@link ItagItem} corresponding to the stream, which
     *                                      cannot be null
     * @param targetDurationSec             the target duration of each sequence, in seconds (this
     *                                      value is returned with the targetDurationSec field for
     *                                      each stream in YouTube player response)
     * @param durationSecondsFallback       the duration of the ended livestream which will be used
     *                                      if the duration could not be extracted from the first
     *                                      sequence
     * @return the manifest generated into a string
     * @throws YoutubeDashManifestCreationException if something went wrong when trying to generate
     *                                              the DASH manifest
     */
    @Nonnull
    public static String createDashManifestFromPostLiveStreamDvrStreamingUrl(
            @Nonnull String postLiveStreamDvrStreamingUrl,
            @Nonnull final ItagItem itagItem,
            final int targetDurationSec,
            final long durationSecondsFallback)
            throws YoutubeDashManifestCreationException {
        if (POST_LIVE_STREAMS_MANIFESTS_GENERATED.containsKey(postLiveStreamDvrStreamingUrl)) {
            return POST_LIVE_STREAMS_MANIFESTS_GENERATED.get(postLiveStreamDvrStreamingUrl);
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
                    itagItem, DeliveryType.LIVE);
            postLiveStreamDvrStreamingUrl = response.latestUrl().replace(SQ_0, EMPTY_STRING)
                    .replace(RN_0, EMPTY_STRING).replace(ALR_YES, EMPTY_STRING);

            final int responseCode = response.responseCode();
            if (responseCode != 200) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: could not get the initialization URL of the post live DVR stream: response code "
                                + responseCode);
            }

            final Map<String, List<String>> responseHeaders = response.responseHeaders();
            streamDuration = responseHeaders.get("X-Head-Time-Millis").get(0);
            segmentCount = responseHeaders.get("X-Head-Seqnum").get(0);
        } catch (final IndexOutOfBoundsException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: could not get the value of the X-Head-Time-Millis or the X-Head-Seqnum header of the post live DVR streaming URL", e);
        }

        if (isNullOrEmpty(segmentCount)) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: could not get the number of segments of the post live DVR stream");
        }

        final Document document = generateDocumentAndMpdElement(new String[]{streamDuration},
                DeliveryType.LIVE, itagItem, durationSecondsFallback);
        generatePeriodElement(document);
        generateAdaptationSetElement(document, itagItem);
        generateRoleElement(document);
        generateRepresentationElement(document, itagItem);
        if (itagItem.itagType == ItagItem.ItagType.AUDIO) {
            generateAudioChannelConfigurationElement(document, itagItem);
        }
        generateSegmentTemplateElement(document, postLiveStreamDvrStreamingUrl, DeliveryType.LIVE);
        generateSegmentTimelineElement(document);
        generateSegmentElementForPostLiveDvrStreams(document, targetDurationSec, segmentCount);

        return buildResult(originalPostLiveStreamDvrStreamingUrl, document,
                POST_LIVE_STREAMS_MANIFESTS_GENERATED);
    }

    /**
     * Create DASH manifests from a YouTube progressive stream.
     *
     * <p>
     * Progressive is one of the YouTube DASH streams which works with range requests and without
     * the need to get a manifest.
     * </p>
     *
     * <p>
     * They can be found on all videos, and for all streams on videos which comes from a YouTube
     * partner or for videos with a big amount of views.
     * </p>
     *
     * <p>This method needs:
     *     <ul>
     *         <li>the base URL of the stream (which returns the whole stream, after redirects and
     *         if the URL is valid, if you try to access to it);</li>
     *         <li>an {@link ItagItem} which needs to contain the following information:
     *              <ul>
     *                  <li>its type (see {@link ItagItem.ItagType}), to identify if the content is
     *                  an audio or a video stream</li>
     *                  <li>its bitrate;</li>
     *                  <li>its mime type;</li>
     *                  <li>its codec(s);</li>
     *                  <li>for audio streams: its audio channels;</li>
     *                  <li>for video streams: its width and height.</li>
     *              </ul>
     *         </li>
     *         <li>the duration of the video, which will be used if the duration could not be
     *         get from the {@link ItagItem}.</li>
     *     </ul>
     * </p>
     *
     * <p>In order to generate the DASH manifest, this method will:
     *      <ul>
     *          <li>request the base URL of the stream with a HEAD request;</li>
     *          <li>follow its redirection(s), if there is one or more;</li>
     *          <li>save the last URL;</li>
     *          <li>use the information provided in the {@link ItagItem} to generate all
     *          elements of the DASH manifest.</li>
     *      </ul>
     * </p>
     *
     * <p>If the duration could not be extracted, the {@code durationSecondsFallback} value will be
     * used as the stream duration.</p>
     *
     * @param progressiveStreamingBaseUrl the base URL of the progressive stream, which cannot be
     *                                    null
     * @param itagItem                    the {@link ItagItem} corresponding to the stream, which
     *                                    cannot be null
     * @param durationSecondsFallback     the duration of the progressive stream which will be used
     *                                    if the duration could not be extracted from the first
     *                                    sequence
     * @return the manifest generated into a string
     * @throws YoutubeDashManifestCreationException if something went wrong when trying to generate
     *                                              the DASH manifest
     */
    @Nonnull
    public static String createDashManifestFromProgressiveStreamingUrl(
            @Nonnull String progressiveStreamingBaseUrl,
            @Nonnull final ItagItem itagItem,
            final long durationSecondsFallback) throws YoutubeDashManifestCreationException {
        if (PROGRESSIVE_STREAMS_MANIFESTS_GENERATED.containsKey(progressiveStreamingBaseUrl)) {
            return PROGRESSIVE_STREAMS_MANIFESTS_GENERATED.get(progressiveStreamingBaseUrl);
        }

        if (durationSecondsFallback <= 0) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: the durationSecondsFallback value is less than or equal to 0 (" + durationSecondsFallback + ")");
        }

        final Document document = generateDocumentAndMpdElement(new String[]{},
                DeliveryType.PROGRESSIVE, itagItem, durationSecondsFallback);
        generatePeriodElement(document);
        generateAdaptationSetElement(document, itagItem);
        generateRoleElement(document);
        generateRepresentationElement(document, itagItem);
        if (itagItem.itagType == ItagItem.ItagType.AUDIO) {
            generateAudioChannelConfigurationElement(document, itagItem);
        }
        generateBaseUrlElement(document, progressiveStreamingBaseUrl);
        generateSegmentBaseElement(document, itagItem);
        generateInitializationElement(document, itagItem);

        return buildResult(progressiveStreamingBaseUrl, document, PROGRESSIVE_STREAMS_MANIFESTS_GENERATED);
    }

    /**
     * Get the "initialization" {@link Response response} of a stream.
     *
     * <p>
     * This method fetches:
     * <ul>
     *     <li>for progressive streams, the base URL of the stream with a HEAD request;</li>
     *     <li>for OTF streams and for post live DVR streams, the base URL of the stream on which
     *     are appended {@link #SQ_0} and {@link #RN_0} params, with a GET request for streaming
     *     URLs from the WEB client and a POST request for the ones from the Android client;</li>
     *     <li>for post live DVR streams from the Android client, the {@link #HEADM_1} param is
     *     also added;</li>
     *     <li>for streaming URLs from the WEB client, the {@link #ALR_YES} param is also added.
     *     </li>
     * </ul>
     * </p>
     *
     * @param baseStreamingUrl the base URL of the stream, which cannot be null
     * @param itagItem         the {@link ItagItem} of stream, which cannot be null
     * @param deliveryType     the {@link DeliveryType} of the stream
     * @return the "initialization" response, without redirections on the network on which the
     * request/s is/are made
     * @throws YoutubeDashManifestCreationException if something went wrong when fetching the
     *                                              "initialization" response and/or its redirects
     */
    @Nonnull
    private static Response getInitializationResponse(@Nonnull String baseStreamingUrl,
                                                      @Nonnull final ItagItem itagItem,
                                                      final DeliveryType deliveryType)
            throws YoutubeDashManifestCreationException {
        final boolean isAWebStreamingUrl = isWebStreamingUrl(baseStreamingUrl);
        final boolean isAnAndroidStreamingUrl = isAndroidStreamingUrl(baseStreamingUrl);
        final boolean isAnAndroidStreamingUrlAndAPostLiveDvrStream = isAnAndroidStreamingUrl
                && deliveryType == DeliveryType.LIVE;
        if (isAWebStreamingUrl) {
            baseStreamingUrl += ALR_YES;
            baseStreamingUrl = appendRnParamAndSqParamIfNeeded(baseStreamingUrl, deliveryType);
        } else if (isAnAndroidStreamingUrlAndAPostLiveDvrStream) {
            baseStreamingUrl += SQ_0 + RN_0 + HEADM_1;
        } else {
            baseStreamingUrl = appendRnParamAndSqParamIfNeeded(baseStreamingUrl, deliveryType);
        }

        final Downloader downloader = NewPipe.getDownloader();
        if (isAWebStreamingUrl) {
            final String mimeTypeExpected = itagItem.getMediaFormat().getMimeType();
            if (!isNullOrEmpty(mimeTypeExpected)) {
                return getStreamingWebUrlWithoutRedirects(downloader, baseStreamingUrl,
                        mimeTypeExpected, deliveryType);
            }
        } else if (isAnAndroidStreamingUrlAndAPostLiveDvrStream) {
            try {
                final Map<String, List<String>> headers = new HashMap<>();
                headers.put("User-Agent", Collections.singletonList(
                        getYoutubeAndroidAppUserAgent(null)));
                final byte[] emptyBody = "".getBytes(StandardCharsets.UTF_8);
                return downloader.post(baseStreamingUrl, headers, emptyBody);
            } catch (final IOException | ExtractionException e) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: error when trying to get the ANDROID streaming post live DVR URL response", e);
            }
        }

        try {
            final Map<String, List<String>> headers = new HashMap<>();
            if (isAnAndroidStreamingUrl) {
                headers.put("User-Agent", Collections.singletonList(
                        getYoutubeAndroidAppUserAgent(null)));
            }

            return downloader.get(baseStreamingUrl, headers);
        } catch (final IOException | ExtractionException e) {
            if (isAnAndroidStreamingUrl) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: error when trying to get the ANDROID streaming URL response", e);
            } else {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: error when trying to get the streaming URL response", e);
            }
        }
    }

    /**
     * Append {@link #SQ_0} for post live DVR and OTF streams and {@link #RN_0} to all streams.
     *
     * @param baseStreamingUrl the base streaming URL on which appending the param(s)
     * @param deliveryType     the {@link DeliveryType} of the stream
     * @return the base streaming URL on which the param(s) are appended, depending on the
     * {@link DeliveryType} of the stream
     */
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
     * and/or IP used to fetch the streaming URL.
     *
     * <p>
     * This method will follow redirects for web clients, which works in the following way:
     * <ol>
     *     <li>the {@link #ALR_YES} param is appended on all streaming URLs</li>
     *     <li>if no redirection occur, the video server will return the streaming data;</li>
     *     <li>if a redirection occur, the server will respond with a 200 HTTP code and a
     *     text/plain mime type. The redirection URL is the response body;</li>
     *     <li>the redirection URL is requested and the steps above from step 2 are repeated (until
     *     too many redirects are reached of course).</li>
     * </ol>
     * </p>
     *
     * @param downloader               the {@link Downloader} instance to be used
     * @param streamingUrl             the streaming URL on which trying to get a streaming URL
     *                                 without any redirection on the network and/or IP used
     * @param responseMimeTypeExpected the response mime type expected from Google video servers
     * @param deliveryType             the {@link DeliveryType} of the stream
     * @return the response of the stream which should have no redirections
     * @throws YoutubeDashManifestCreationException if something went wrong when trying to get the
     *                                              response without any redirection
     */
    @Nonnull
    private static Response getStreamingWebUrlWithoutRedirects(
            @Nonnull final Downloader downloader,
            @Nonnull String streamingUrl,
            @Nonnull final String responseMimeTypeExpected,
            @Nonnull final DeliveryType deliveryType) throws YoutubeDashManifestCreationException {
        try {
            final Map<String, List<String>> headers = new HashMap<>();
            addClientInfoHeaders(headers);

            String responseMimeType = "";

            int redirectsCount = 0;
            while (!responseMimeType.equals(responseMimeTypeExpected)
                    && redirectsCount < MAXIMUM_REDIRECT_COUNT) {
                final Response response;
                // We can use head requests to reduce the request size, but only for progressive
                // streams
                if (deliveryType == DeliveryType.PROGRESSIVE) {
                    response = downloader.head(streamingUrl, headers);
                } else {
                    response = downloader.get(streamingUrl, headers);
                }

                final int responseCode = response.responseCode();
                if (responseCode != 200) {
                    if (deliveryType == DeliveryType.LIVE) {
                        throw new YoutubeDashManifestCreationException(
                                "Could not generate the DASH manifest: could not get the initialization URL of the post live DVR stream: response code "
                                        + responseCode);
                    } else if (deliveryType == DeliveryType.OTF) {
                        throw new YoutubeDashManifestCreationException(
                                "Could not generate the DASH manifest: could not get the initialization URL of the OTF stream: response code "
                                        + responseCode);
                    } else {
                        throw new YoutubeDashManifestCreationException(
                                "Could not generate the DASH manifest: could not fetch the URL of the progressive stream: response code "
                                        + responseCode);
                    }
                }

                // A valid response must include a Content-Type header, so we can require that
                // the response from video servers has this header.
                try {
                    responseMimeType = Objects.requireNonNull(response.getHeader(
                            "Content-Type"));
                } catch (final NullPointerException e) {
                    throw new YoutubeDashManifestCreationException(
                            "Could not generate the DASH manifest: could not get the Content-Type header from the streaming URL", e);
                }

                // The response body is the redirection URL
                if (responseMimeType.equals("text/plain")) {
                    streamingUrl = response.responseBody();
                    redirectsCount++;
                } else {
                    return response;
                }
            }

            if (redirectsCount >= MAXIMUM_REDIRECT_COUNT) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: too many redirects when trying to get the WEB streaming URL response");
            }

            // This should be never reached but is required because we don't want to return null
            // here
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: error when trying to get the WEB streaming URL response");
        } catch (final IOException | ExtractionException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: error when trying to get the WEB streaming URL response", e);
        }
    }

    /**
     * Collect all segments from an OTF stream, by parsing the string array which contains all the
     * sequences extracted with the regular expression {@link #SEGMENT_DURATION_MS_PATTERN}.
     *
     * @param segmentDuration the string array which contains all the sequences extracted with the
     *                        regular expression
     * @throws YoutubeDashManifestCreationException if something went wrong when trying to collect
     *                                              the segments of the OTF stream
     */
    private static void collectSegmentsData(@Nonnull final String[] segmentDuration)
            throws YoutubeDashManifestCreationException {
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
                SEGMENTS_DURATION.add(segmentLength);
                DURATION_REPETITIONS.add(segmentRepeatCount);
            }
        } catch (final NumberFormatException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: unable to get the segments of the stream", e);
        }
    }

    /**
     * Get the duration of an OTF stream.
     *
     * <p>
     * The duration of OTF streams is not returned into the player response and needs to be
     * calculated by adding the duration of each segment.
     * </p>
     *
     * @param segmentDuration the segment duration object extracted from the initialization
     *                        sequence of the stream
     * @return the duration of the OTF stream
     * @throws YoutubeDashManifestCreationException if something went wrong when parsing the
     *                                              {@code segmentDuration} object
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
     * @param segmentDuration         the segment duration object extracted from the initialization
     *                                sequence of the stream
     * @param deliveryType            the {@link DeliveryType} of the stream, see the enum for
     *                                possible values
     * @param durationSecondsFallback the duration in seconds, extracted from player response, used
     *                                as a fallback
     * @return a {@link Document} object which contains a {@code <MPD>} element
     * @throws YoutubeDashManifestCreationException if something went wrong when generating/
     *                                              appending the {@link Document object} or the
     *                                              {@code <MPD>} element
     */
    private static Document generateDocumentAndMpdElement(@Nonnull final String[] segmentDuration,
                                                          final DeliveryType deliveryType,
                                                          @Nonnull final ItagItem itagItem,
                                                          final long durationSecondsFallback)
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
            profilesAttribute.setValue("urn:mpeg:dash:profile:full:2011");
            mpdElement.setAttributeNode(profilesAttribute);

            final Attr typeAttribute = document.createAttribute("type");
            typeAttribute.setValue("static");
            mpdElement.setAttributeNode(typeAttribute);

            final Attr mediaPresentationDurationAttribute = document.createAttribute(
                    "mediaPresentationDuration");
            final long streamDuration;
            if (deliveryType == DeliveryType.LIVE) {
                streamDuration = Integer.parseInt(segmentDuration[0]);
            } else if (deliveryType == DeliveryType.OTF) {
                streamDuration = getStreamDuration(segmentDuration);
            } else {
                final int itagItemDuration = itagItem.getApproxDurationMs();
                if (itagItemDuration != -1) {
                    streamDuration = itagItemDuration;
                } else {
                    if (durationSecondsFallback > 0) {
                        streamDuration = durationSecondsFallback * 1000;
                    } else {
                        throw new YoutubeDashManifestCreationException(
                                "Could not generate or append to the document the MPD element of the DASH manifest :"
                                        + "the duration of the stream could not be determined and the durationSecondsFallback is less than or equal to 0");
                    }
                }
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
     * {@link #generateDocumentAndMpdElement(String[], DeliveryType, ItagItem, long)}.
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <Period>} element will be
     *                 appended
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the {@code <Period>} element to the
     *                                              document
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
     * {@link #generateDocumentAndMpdElement(String[], DeliveryType, ItagItem, long)}.
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <Period>} element will be
     *                 appended
     * @param itagItem the {@link ItagItem} corresponding to the stream, which cannot be null
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the {@code <Period>} element to the
     *                                              document
     */
    private static void generateAdaptationSetElement(@Nonnull final Document document,
                                                     @Nonnull final ItagItem itagItem)
            throws YoutubeDashManifestCreationException {
        try {
            final Element periodElement = (Element) document.getElementsByTagName("Period").item(0);
            final Element adaptationSetElement = document.createElement("AdaptationSet");

            final Attr idAttribute = document.createAttribute("id");
            idAttribute.setValue("0");
            adaptationSetElement.setAttributeNode(idAttribute);

            final MediaFormat mediaFormat = itagItem.getMediaFormat();
            if (mediaFormat == null || isNullOrEmpty(mediaFormat.mimeType)) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the AdaptationSet element of the DASH manifest: the MediaFormat or the mime type of the MediaFormat of the ItagItem is null or empty");
            }

            final Attr mimeTypeAttribute = document.createAttribute("mimeType");
            mimeTypeAttribute.setValue(mediaFormat.mimeType);
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
     * {@link #generateAdaptationSetElement(Document, ItagItem)}).
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <Role>} element will be
     *                 appended
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the {@code <Role>} element to the document
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
     * @param itagItem the {@link ItagItem} to use, which cannot be null
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the {@code <Representation>} element
     *                                              to the document
     */
    private static void generateRepresentationElement(@Nonnull final Document document,
                                                      @Nonnull final ItagItem itagItem)
            throws YoutubeDashManifestCreationException {
        try {
            final Element adaptationSetElement = (Element) document.getElementsByTagName(
                    "AdaptationSet").item(0);
            final Element representationElement = document.createElement("Representation");

            final int id = itagItem.id;
            if (id <= 0) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the Representation element of the DASH manifest: the id of the ItagItem is less than or equal to 0");
            }
            final Attr idAttribute = document.createAttribute("id");
            idAttribute.setValue(String.valueOf(id));
            representationElement.setAttributeNode(idAttribute);

            final String codec = itagItem.getCodec();
            if (isNullOrEmpty(codec)) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the AdaptationSet element of the DASH manifest: the codecs value is null or empty");
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
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the Representation element of the DASH manifest: the bitrate of the ItagItem is less than or equal to 0");
            }
            final Attr bandwidthAttribute = document.createAttribute("bandwidth");
            bandwidthAttribute.setValue(String.valueOf(bitrate));
            representationElement.setAttributeNode(bandwidthAttribute);

            final ItagItem.ItagType itagType = itagItem.itagType;

            if (itagType == ItagItem.ItagType.VIDEO || itagType == ItagItem.ItagType.VIDEO_ONLY) {
                final int height = itagItem.getHeight();
                final int width = itagItem.getWidth();
                if (height <= 0 && width <= 0) {
                    throw new YoutubeDashManifestCreationException(
                            "Could not generate the Representation element of the DASH manifest: the width and the height of the ItagItem are less than or equal to 0");
                }

                if (width > 0) {
                    final Attr widthAttribute = document.createAttribute("width");
                    widthAttribute.setValue(String.valueOf(width));
                    representationElement.setAttributeNode(widthAttribute);
                }

                final Attr heightAttribute = document.createAttribute("height");
                heightAttribute.setValue(String.valueOf(itagItem.getHeight()));
                representationElement.setAttributeNode(heightAttribute);

                final int fps = itagItem.fps;
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
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the Representation element of the DASH manifest", e);
        }
    }

    /**
     * Generate the {@code <AudioChannelConfiguration>} element, appended as a child of the
     * {@code <Representation>} element.
     *
     * <p>
     * This method is only used when generating DASH manifests of audio streams.
     * </p>
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
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement(Document, ItagItem)}).
     * </p>
     *
     * @param document the {@link Document} on which the {@code <AudioChannelConfiguration>}
     *                 element will be appended
     * @param itagItem the {@link ItagItem} to use, which cannot be null
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the
     *                                              {@code <AudioChannelConfiguration>} element
     *                                              to the document
     */
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

    /**
     * Generate the {@code <BaseURL>} element, appended as a child of the
     * {@code <Representation>} element.
     *
     * <p>
     * This method is only used when generating DASH manifests from progressive streams.
     * </p>
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement(Document, ItagItem)}).
     * </p>
     *
     * @param document the {@link Document} on which the {@code <BaseURL>} element will
     *                 be appended
     * @param baseUrl  the base URL of the stream, which cannot be null and will be set as the
     *                 content of the {@code <BaseURL>} element
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the {@code <BaseURL>} element
     *                                              to the document
     */
    private static void generateBaseUrlElement(@Nonnull final Document document,
                                               @Nonnull final String baseUrl)
            throws YoutubeDashManifestCreationException {
        try {
            final Element representationElement = (Element) document.getElementsByTagName(
                    "Representation").item(0);
            final Element baseURLElement = document.createElement("BaseURL");
            baseURLElement.setTextContent(baseUrl);
            representationElement.appendChild(baseURLElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the BaseURL element of the DASH manifest", e);
        }
    }

    /**
     * Generate the {@code <SegmentBase>} element, appended as a child of the
     * {@code <Representation>} element.
     *
     * <p>
     * This method is only used when generating DASH manifests from progressive streams.
     * </p>
     * <p>
     * It generates the following element:
     * <br>
     * {@code <SegmentBase indexRange="indexStart-indexEnd"></SegmentBase>}
     * <br>
     * (where {@code indexStart} and {@code indexEnd} are gotten from the {@link ItagItem} passed
     * as the second parameter)
     * </p>
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement(Document, ItagItem)}).
     * </p>
     *
     * @param document the {@link Document} on which the {@code <SegmentBase>} element will
     *                 be appended
     * @param itagItem the {@link ItagItem} to use, which cannot be null
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the {@code <SegmentBase>} element
     *                                              to the document
     */
    private static void generateSegmentBaseElement(@Nonnull final Document document,
                                                   @Nonnull final ItagItem itagItem)
            throws YoutubeDashManifestCreationException {
        try {
            final Element representationElement = (Element) document.getElementsByTagName(
                    "Representation").item(0);

            final Element segmentBaseElement = document.createElement("SegmentBase");

            final Attr indexRangeAttribute = document.createAttribute("indexRange");

            final int indexStart = itagItem.getIndexStart();
            if (indexStart < 0) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: the indexStart value of the ItagItem is less than to 0 (" + indexStart + ")");
            }
            final int indexEnd = itagItem.getIndexEnd();
            if (indexEnd < 0) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: the indexEnd value of the ItagItem is less than to 0 (" + indexStart + ")");
            }

            indexRangeAttribute.setValue(indexStart + "-" + indexEnd);
            segmentBaseElement.setAttributeNode(indexRangeAttribute);

            representationElement.appendChild(segmentBaseElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the SegmentBase element of the DASH manifest", e);
        }
    }

    /**
     * Generate the {@code <Initialization>} element, appended as a child of the
     * {@code <SegmentBase>} element.
     *
     * <p>
     * This method is only used when generating DASH manifests from progressive streams.
     * </p>
     * <p>
     * It generates the following element:
     * <br>
     * {@code <Initialization range="initStart-initEnd"></SegmentBase>}
     * <br>
     * (where {@code indexStart} and {@code indexEnd} are gotten from the {@link ItagItem} passed
     * as the second parameter)
     * </p>
     * <p>
     * The {@code <SegmentBase>} element needs to be generated before this element with
     * {@link #generateSegmentBaseElement(Document, ItagItem)}).
     * </p>
     *
     * @param document the {@link Document} on which the {@code <Initialization>} element will
     *                 be appended
     * @param itagItem the {@link ItagItem} to use, which cannot be null
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the {@code <Initialization>} element
     *                                              to the document
     */
    private static void generateInitializationElement(@Nonnull final Document document,
                                                      @Nonnull final ItagItem itagItem)
            throws YoutubeDashManifestCreationException {
        try {
            final Element segmentBaseElement = (Element) document.getElementsByTagName(
                    "SegmentBase").item(0);

            final Element initializationElement = document.createElement("Initialization");

            final Attr rangeAttribute = document.createAttribute("range");

            final int initStart = itagItem.getInitStart();
            if (initStart < 0) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: the initStart value of the ItagItem is less than to 0 (" + initStart + ")");
            }
            final int initEnd = itagItem.getInitEnd();
            if (initEnd < 0) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: the initEnd value of the ItagItem is less than to 0 (" + initEnd + ")");
            }

            rangeAttribute.setValue(initStart + "-" + initEnd);
            initializationElement.setAttributeNode(rangeAttribute);

            segmentBaseElement.appendChild(initializationElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the Initialization element of the DASH manifest", e);
        }
    }

    /**
     * Generate the {@code <SegmentTemplate>} element, appended as a child of the
     * {@code <Representation>} element.
     *
     * <p>
     * This method is only used when generating DASH manifests from OTF and post live DVR streams.
     * </p>
     * <p>
     * It will produce a {@code <SegmentTemplate>} element with the following attributes:
     * <ul>
     *     <li>{@code startNumber}, which takes the value {@code 0} for post live DVR streams and
     *     {@code 1} for OTF streams;</li>
     *     <li>{@code timescale}, which is always {@code 1000};</li>
     *     <li>{@code media}, which is the base URL of the stream on which is appended
     *     {@code &sq=$Number$&rn=$Number$};</li>
     *     <li>{@code initialization} (only for OTF streams), which is the base URL of the stream
     *     on which is appended {@link #SQ_0} and {@link #RN_0}.</li>
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
     * @param baseUrl      the base URL of the OTF/post live DVR stream
     * @param deliveryType the stream {@link DeliveryType delivery type}
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the {@code <SegmentTemplate>} element
     *                                              to the document
     */
    private static void generateSegmentTemplateElement(@Nonnull final Document document,
                                                       @Nonnull final String baseUrl,
                                                       final DeliveryType deliveryType)
            throws YoutubeDashManifestCreationException {
        try {
            final Element representationElement = (Element) document.getElementsByTagName(
                    "Representation").item(0);
            final Element segmentTemplateElement = document.createElement("SegmentTemplate");

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

            // Post live DVR/ended livestreams streams doesn't require an initialization sequence
            if (!isDeliveryTypeLive) {
                final Attr initializationAttribute = document.createAttribute("initialization");
                initializationAttribute.setValue(baseUrl + SQ_0 + RN_0);
                segmentTemplateElement.setAttributeNode(initializationAttribute);
            }

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
     * {@link #generateSegmentTemplateElement(Document, String, DeliveryType)}.
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <SegmentTimeline>} element will
     *                 be appended
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the {@code <SegmentTimeline>} element
     *                                              to the document
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

    /**
     * Generate segment elements for OTF streams.
     *
     * <p>
     * By parsing by the first media sequence, we know how many durations and repetitions they are
     * so we just have to loop into {@link #SEGMENTS_DURATION} and {@link #DURATION_REPETITIONS}
     * to generate for each duration, the following element:
     * <br>
     * {@code <S d="segmentDuration" r="durationRepetition" />}
     * <br>
     * These elements will be appended as children of the {@code <SegmentTimeline>} element.
     * </p>
     * <p>
     * The {@code <SegmentTimeline>} element needs to be generated before this element with
     * {@link #generateSegmentTimelineElement(Document)}.
     * </p>
     *
     * @param document the {@link Document} on which the the {@code <S>} elements will be appended
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the {@code <S>} elements to the
     *                                              document
     */
    private static void generateSegmentElementsForOtfStreams(@Nonnull final Document document)
            throws YoutubeDashManifestCreationException {
        try {
            if (isNullOrEmpty(SEGMENTS_DURATION) || isNullOrEmpty(DURATION_REPETITIONS)) {
                throw new IllegalStateException(
                        "Duration of segments and/or repetition(s) of segments are unknown");
            }
            final Element segmentTimelineElement = (Element) document.getElementsByTagName(
                    "SegmentTimeline").item(0);

            for (int i = 0; i < SEGMENTS_DURATION.size(); i++) {
                final Element sElement = document.createElement("S");

                final int durationRepetition = DURATION_REPETITIONS.get(i);
                if (durationRepetition != 0) {
                    final Attr rAttribute = document.createAttribute("r");
                    rAttribute.setValue(String.valueOf(durationRepetition));
                    sElement.setAttributeNode(rAttribute);
                }

                final Attr dAttribute = document.createAttribute("d");
                dAttribute.setValue(String.valueOf(SEGMENTS_DURATION.get(i)));
                sElement.setAttributeNode(dAttribute);

                segmentTimelineElement.appendChild(sElement);
            }

        } catch (final DOMException | IllegalStateException | IndexOutOfBoundsException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append to the document the Segment elements of the DASH manifest", e);
        }
    }

    /**
     * Generate the segment element for post live DVR streams.
     *
     * <p>
     * We don't know the exact duration of segments for post live DVR streams but an
     * average instead (which is the {@code targetDurationSec} value), so we can use the following
     * structure to generate the segment timeline for DASH manifests of ended livestreams:
     * <br>
     * {@code <S d="targetDurationSecValue" r="segmentCount" />}
     * </p>
     *
     * @param document              the {@link Document} on which the the {@code <S>} element will
     *                              be appended
     * @param targetDurationSeconds the {@code targetDurationSec} value from player response's
     *                              stream
     * @param segmentCount          the number of segments, extracted by the main method which
     *                              generates manifests of post DVR livestreams
     * @throws YoutubeDashManifestCreationException if something went wrong when generating or
     *                                              appending the {@code <S>} element to the
     *                                              document
     */
    private static void generateSegmentElementForPostLiveDvrStreams(
            @Nonnull final Document document,
            final int targetDurationSeconds,
            @Nonnull final String segmentCount) throws YoutubeDashManifestCreationException {
        try {
            final Element segmentTimelineElement = (Element) document.getElementsByTagName(
                    "SegmentTimeline").item(0);
            final Element sElement = document.createElement("S");

            final Attr dAttribute = document.createAttribute("d");
            dAttribute.setValue(String.valueOf(targetDurationSeconds * 1000));
            sElement.setAttributeNode(dAttribute);

            final Attr rAttribute = document.createAttribute("r");
            rAttribute.setValue(segmentCount);
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
     *                                    (which is either {@link #OTF_MANIFESTS_GENERATED} or
     *                                    {@link #POST_LIVE_STREAMS_MANIFESTS_GENERATED})
     * @return the DASH manifest {@link Document document} converted to a string
     * @throws YoutubeDashManifestCreationException if something went wrong when converting the
     *                                              {@link Document document}
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
        } catch (final Exception e) {
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
        return OTF_MANIFESTS_GENERATED.size();
    }

    /**
     * Get the number of cached post live streams manifests.
     *
     * @return the number of cached post live streams manifests
     */
    public static int getPostLiveStreamsCachedManifestsSize() {
        return POST_LIVE_STREAMS_MANIFESTS_GENERATED.size();
    }

    /**
     * Get the number of cached progressive manifests.
     *
     * @return the number of cached progressive manifests
     */
    public static int getProgressiveCachedManifestsSize() {
        return PROGRESSIVE_STREAMS_MANIFESTS_GENERATED.size();
    }

    /**
     * Clear the cached OTF manifests.
     */
    public static void clearOtfCachedManifests() {
        OTF_MANIFESTS_GENERATED.clear();
    }

    /**
     * Clear the cached post live streams manifests.
     */
    public static void clearPostLiveStreamsCachedManifests() {
        POST_LIVE_STREAMS_MANIFESTS_GENERATED.clear();
    }

    /**
     * Clear the cached post live streams manifests.
     */
    public static void clearProgressiveCachedManifests() {
        POST_LIVE_STREAMS_MANIFESTS_GENERATED.clear();
    }

    /**
     * Clear the cached OTF manifests, the cached post live streams manifests and the cached
     * progressive manifests in their respective caches.
     */
    public static void clearManifestsInCaches() {
        OTF_MANIFESTS_GENERATED.clear();
        POST_LIVE_STREAMS_MANIFESTS_GENERATED.clear();
        PROGRESSIVE_STREAMS_MANIFESTS_GENERATED.clear();
    }
}
