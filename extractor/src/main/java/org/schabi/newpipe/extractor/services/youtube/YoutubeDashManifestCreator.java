package org.schabi.newpipe.extractor.services.youtube;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.addClientInfoHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getAndroidUserAgent;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getIosUserAgent;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isAndroidStreamingUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isIosStreamingUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isTvHtml5SimplyEmbeddedPlayerStreamingUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isWebStreamingUrl;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.isBlank;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache;
import org.schabi.newpipe.extractor.utils.Utils;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

/**
 * Class to generate DASH manifests from YouTube OTF, progressive and ended/post-live-DVR streams.
 * It relies on external classes from the {@link org.w3c.dom} and {@link javax.xml} packages.
 */
public final class YoutubeDashManifestCreator {

    /**
     * URL parameter of the first sequence for live, post-live-DVR and OTF streams.
     */
    private static final String SQ_0 = "&sq=0";

    /**
     * URL parameter of the first stream request made by official clients.
     */
    private static final String RN_0 = "&rn=0";

    /**
     * URL parameter specific to web clients. When this param is added, if a redirection occurs,
     * the server will not redirect clients to the redirect URL. Instead, it will provide this URL
     * as the response body.
     */
    private static final String ALR_YES = "&alr=yes";

    /**
     * The redirect count limit that this class uses, which is the same limit as OkHttp.
     */
    private static final int MAXIMUM_REDIRECT_COUNT = 20;

    /**
     * Cache of DASH manifests generated for OTF streams.
     */
    private static final ManifestCreatorCache<String, String> OTF_CACHE
            = new ManifestCreatorCache<>();

    /**
     * Cache of DASH manifests generated for post-live-DVR streams.
     */
    private static final ManifestCreatorCache<String, String> POST_LIVE_DVR_CACHE
            = new ManifestCreatorCache<>();

    /**
     * Cache of DASH manifests generated for progressive streams.
     */
    private static final ManifestCreatorCache<String, String> PROGRESSIVE_CACHE
            = new ManifestCreatorCache<>();

    /**
     * Enum of streaming format types used by YouTube in their streams.
     */
    private enum DeliveryType {

        /**
         * YouTube's progressive delivery method, which works with HTTP range headers.
         * (Note that official clients use the corresponding parameter instead.)
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
         * The first sequence (which can be fetched with the {@link #SQ_0} param) contains all the
         * metadata needed to build the stream source (sidx boxes, segment length, segment count,
         * duration, ...)
         * </p>
         *
         * <p>
         * Only used for videos; mostly those with a small amount of views, or ended livestreams
         * which have just been re-encoded as normal videos.
         * </p>
         */
        OTF,

        /**
         * YouTube's delivery method for livestreams which uses a sequence parameter to get
         * segments of streams.
         *
         * <p>
         * Each sequence (which can be fetched with the {@link #SQ_0} param) contains its own
         * metadata (sidx boxes, segment length, ...), which make no need of an initialization
         * segment.
         * </p>
         *
         * <p>
         * Only used for livestreams (ended or running).
         * </p>
         */
        LIVE
    }

    private YoutubeDashManifestCreator() {
    }

    /**
     * Exception that is thrown when the {@link YoutubeDashManifestCreator} encounters a problem
     * while creating a manifest.
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
     * OTF streams are YouTube-DASH specific streams which work with sequences and without the need
     * to get a manifest (even if one is provided, it is not used by official clients).
     * </p>
     * <p>
     * They can be found only on videos; mostly those with a small amount of views, or ended
     * livestreams which have just been re-encoded as normal videos.
     * </p>
     *
     * <p>This method needs:
     *     <ul>
     *         <li>the base URL of the stream (which, if you try to access to it, returns HTTP
     *         status code 404 after redirects, and if the URL is valid);</li>
     *         <li>an {@link ItagItem}, which needs to contain the following information:
     *              <ul>
     *                  <li>its type (see {@link ItagItem.ItagType}), to identify if the content is
     *                  an audio or a video stream;</li>
     *                  <li>its bitrate;</li>
     *                  <li>its mime type;</li>
     *                  <li>its codec(s);</li>
     *                  <li>for an audio stream: its audio channels;</li>
     *                  <li>for a video stream: its width and height.</li>
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
     *          sequence parameter is appended (see {@link #SQ_0})) with a POST or GET request
     *          (depending of the client on which the streaming URL comes from);
     *          </li>
     *          <li>follow its redirection(s), if any;</li>
     *          <li>save the last URL, remove the first sequence parameter;</li>
     *          <li>use the information provided in the {@link ItagItem} to generate all
     *          elements of the DASH manifest.</li>
     *      </ul>
     * </p>
     *
     * <p>
     * If the duration cannot be extracted, the {@code durationSecondsFallback} value will be used
     * as the stream duration.
     * </p>
     *
     * @param otfBaseStreamingUrl     the base URL of the OTF stream, which cannot be null
     * @param itagItem                the {@link ItagItem} corresponding to the stream, which
     *                                cannot be null
     * @param durationSecondsFallback the duration of the video, which will be used if the duration
     *                                could not be extracted from the first sequence
     * @return the manifest generated into a string
     */
    @Nonnull
    public static String fromOtfStreamingUrl(
            @Nonnull final String otfBaseStreamingUrl,
            @Nonnull final ItagItem itagItem,
            final long durationSecondsFallback) throws YoutubeDashManifestCreationException {
        if (OTF_CACHE.containsKey(otfBaseStreamingUrl)) {
            return Objects.requireNonNull(OTF_CACHE.get(otfBaseStreamingUrl)).getSecond();
        }

        String realOtfBaseStreamingUrl = otfBaseStreamingUrl;
        // Try to avoid redirects when streaming the content by saving the last URL we get
        // from video servers.
        final Response response = getInitializationResponse(realOtfBaseStreamingUrl,
                itagItem, DeliveryType.OTF);
        realOtfBaseStreamingUrl = response.latestUrl().replace(SQ_0, EMPTY_STRING)
                .replace(RN_0, EMPTY_STRING).replace(ALR_YES, EMPTY_STRING);

        final int responseCode = response.responseCode();
        if (responseCode != 200) {
            throw new YoutubeDashManifestCreationException(
                    "Unable to create the DASH manifest: could not get the initialization URL of "
                            + "the OTF stream: response code " + responseCode);
        }

        final String[] segmentDuration;

        try {
            final String[] segmentsAndDurationsResponseSplit = response.responseBody()
                    // Get the lines with the durations and the following
                    .split("Segment-Durations-Ms: ")[1]
                    // Remove the other lines
                    .split("\n")[0]
                    // Get all durations and repetitions which are separated by a comma
                    .split(",");
            final int lastIndex = segmentsAndDurationsResponseSplit.length - 1;
            if (isBlank(segmentsAndDurationsResponseSplit[lastIndex])) {
                segmentDuration = Arrays.copyOf(segmentsAndDurationsResponseSplit, lastIndex);
            } else {
                segmentDuration = segmentsAndDurationsResponseSplit;
            }
        } catch (final Exception e) {
            throw new YoutubeDashManifestCreationException(
                    "Unable to generate the DASH manifest: could not get the duration of segments",
                    e);
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
        generateSegmentTemplateElement(document, realOtfBaseStreamingUrl, DeliveryType.OTF);
        generateSegmentTimelineElement(document);
        generateSegmentElementsForOtfStreams(segmentDuration, document);

        return buildAndCacheResult(otfBaseStreamingUrl, document, OTF_CACHE);
    }

    /**
     * Create DASH manifests from a YouTube post-live-DVR stream/ended livestream.
     *
     * <p>
     * Post-live-DVR streams/ended livestreams are one of the YouTube DASH specific streams which
     * works with sequences and without the need to get a manifest (even if one is provided but not
     * used by main clients (and is complete for big ended livestreams because it doesn't return
     * the full stream)).
     * </p>
     *
     * <p>
     * They can be found only on livestreams which have ended very recently (a few hours, most of
     * the time)
     * </p>
     *
     * <p>This method needs:
     *     <ul>
     *         <li>the base URL of the stream (which, if you try to access to it, returns HTTP
     *         status code 404 after redirects, and if the URL is valid);</li>
     *         <li>an {@link ItagItem}, which needs to contain the following information:
     *              <ul>
     *                  <li>its type (see {@link ItagItem.ItagType}), to identify if the content is
     *                  an audio or a video stream;</li>
     *                  <li>its bitrate;</li>
     *                  <li>its mime type;</li>
     *                  <li>its codec(s);</li>
     *                  <li>for an audio stream: its audio channels;</li>
     *                  <li>for a video stream: its width and height.</li>
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
     *          sequence parameter is appended (see {@link #SQ_0})) with a POST or GET request
     *          (depending of the client on which the streaming URL comes from);
     *          </li>
     *          <li>follow its redirection(s), if any;</li>
     *          <li>save the last URL, remove the first sequence parameters;</li>
     *          <li>use the information provided in the {@link ItagItem} to generate all
     *          elements of the DASH manifest.</li>
     *      </ul>
     * </p>
     *
     * <p>
     * If the duration cannot be extracted, the {@code durationSecondsFallback} value will be used
     * as the stream duration.
     * </p>
     *
     * @param postLiveStreamDvrStreamingUrl the base URL of the post-live-DVR stream/ended
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
     */
    @Nonnull
    public static String fromPostLiveStreamDvrStreamingUrl(
            @Nonnull final String postLiveStreamDvrStreamingUrl,
            @Nonnull final ItagItem itagItem,
            final int targetDurationSec,
            final long durationSecondsFallback) throws YoutubeDashManifestCreationException {
        if (POST_LIVE_DVR_CACHE.containsKey(postLiveStreamDvrStreamingUrl)) {
            return Objects.requireNonNull(POST_LIVE_DVR_CACHE.get(postLiveStreamDvrStreamingUrl))
                    .getSecond();
        }
        String realPostLiveStreamDvrStreamingUrl = postLiveStreamDvrStreamingUrl;
        final String streamDuration;
        final String segmentCount;

        if (targetDurationSec <= 0) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: the targetDurationSec value is less "
                            + "than or equal to 0 (" + targetDurationSec + ")");
        }

        try {
            // Try to avoid redirects when streaming the content by saving the latest URL we get
            // from video servers.
            final Response response = getInitializationResponse(realPostLiveStreamDvrStreamingUrl,
                    itagItem, DeliveryType.LIVE);
            realPostLiveStreamDvrStreamingUrl = response.latestUrl().replace(SQ_0, EMPTY_STRING)
                    .replace(RN_0, EMPTY_STRING).replace(ALR_YES, EMPTY_STRING);

            final int responseCode = response.responseCode();
            if (responseCode != 200) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: could not get the initialization "
                                + "segment of the post-live-DVR stream: response code "
                                + responseCode);
            }

            final Map<String, List<String>> responseHeaders = response.responseHeaders();
            streamDuration = responseHeaders.get("X-Head-Time-Millis").get(0);
            segmentCount = responseHeaders.get("X-Head-Seqnum").get(0);
        } catch (final IndexOutOfBoundsException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: could not get the value of the "
                            + "X-Head-Time-Millis or the X-Head-Seqnum header of the post-live-DVR"
                            + "streaming URL", e);
        }

        if (isNullOrEmpty(segmentCount)) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: could not get the number of segments of"
                            + "the post-live-DVR stream");
        }

        final Document document = generateDocumentAndMpdElement(new String[] {streamDuration},
                DeliveryType.LIVE, itagItem, durationSecondsFallback);
        generatePeriodElement(document);
        generateAdaptationSetElement(document, itagItem);
        generateRoleElement(document);
        generateRepresentationElement(document, itagItem);
        if (itagItem.itagType == ItagItem.ItagType.AUDIO) {
            generateAudioChannelConfigurationElement(document, itagItem);
        }
        generateSegmentTemplateElement(document, realPostLiveStreamDvrStreamingUrl,
                DeliveryType.LIVE);
        generateSegmentTimelineElement(document);
        generateSegmentElementForPostLiveDvrStreams(document, targetDurationSec, segmentCount);

        return buildAndCacheResult(postLiveStreamDvrStreamingUrl, document,
                POST_LIVE_DVR_CACHE);
    }

    /**
     * Create DASH manifests from a YouTube progressive stream.
     *
     * <p>
     * Progressive streams are YouTube DASH streams which work with range requests and without the
     * need to get a manifest.
     * </p>
     *
     * <p>
     * They can be found on all videos, and for all streams for most of videos which come from a
     * YouTube partner, and on videos with a large number of views.
     * </p>
     *
     * <p>This method needs:
     *     <ul>
     *         <li>the base URL of the stream (which, if you try to access to it, returns the whole
     *         stream, after redirects, and if the URL is valid);</li>
     *         <li>an {@link ItagItem}, which needs to contain the following information:
     *              <ul>
     *                  <li>its type (see {@link ItagItem.ItagType}), to identify if the content is
     *                  an audio or a video stream;</li>
     *                  <li>its bitrate;</li>
     *                  <li>its mime type;</li>
     *                  <li>its codec(s);</li>
     *                  <li>for an audio stream: its audio channels;</li>
     *                  <li>for a video stream: its width and height.</li>
     *              </ul>
     *         </li>
     *         <li>the duration of the video, which will be used if the duration could not be
     *         parsed from the {@link ItagItem}.</li>
     *     </ul>
     * </p>
     *
     * <p>In order to generate the DASH manifest, this method will:
     *      <ul>
     *          <li>request the base URL of the stream with a HEAD request;</li>
     *          <li>follow its redirection(s), if any;</li>
     *          <li>save the last URL;</li>
     *          <li>use the information provided in the {@link ItagItem} to generate all
     *          elements of the DASH manifest.</li>
     *      </ul>
     * </p>
     *
     * <p>
     * If the duration cannot be extracted, the {@code durationSecondsFallback} value will be used
     * as the stream duration.
     * </p>
     *
     * @param progressiveStreamingBaseUrl the base URL of the progressive stream, which cannot be
     *                                    null
     * @param itagItem                    the {@link ItagItem} corresponding to the stream, which
     *                                    cannot be null
     * @param durationSecondsFallback     the duration of the progressive stream which will be used
     *                                    if the duration could not be extracted from the first
     *                                    sequence
     * @return the manifest generated into a string
     */
    @Nonnull
    public static String fromProgressiveStreamingUrl(
            @Nonnull final String progressiveStreamingBaseUrl,
            @Nonnull final ItagItem itagItem,
            final long durationSecondsFallback) throws YoutubeDashManifestCreationException {
        if (PROGRESSIVE_CACHE.containsKey(progressiveStreamingBaseUrl)) {
            return Objects.requireNonNull(PROGRESSIVE_CACHE.get(progressiveStreamingBaseUrl))
                    .getSecond();
        }

        if (durationSecondsFallback <= 0) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: the durationSecondsFallback value is"
                            + "less than or equal to 0 (" + durationSecondsFallback + ")");
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

        return buildAndCacheResult(progressiveStreamingBaseUrl, document,
                PROGRESSIVE_CACHE);
    }

    /**
     * Get the "initialization" {@link Response response} of a stream.
     *
     * <p>
     * This method fetches, for OTF streams and for post-live-DVR streams:
     * <ul>
     *     <li>the base URL of the stream, to which are appended {@link #SQ_0} and {@link #RN_0}
     *     parameters, with a GET request for streaming URLs from the WEB client and a POST request
     *     for the ones from the Android client;</li>
     *     <li>for streaming URLs from the WEB client, the {@link #ALR_YES} param is also added.
     *     </li>
     * </ul>
     * </p>
     *
     * @param baseStreamingUrl the base URL of the stream, which cannot be null
     * @param itagItem         the {@link ItagItem} of stream, which cannot be null
     * @param deliveryType     the {@link DeliveryType} of the stream
     * @return the "initialization" response, without redirections on the network on which the
     *         request(s) is/are made
     */
    @SuppressWarnings("checkstyle:FinalParameters")
    @Nonnull
    private static Response getInitializationResponse(@Nonnull String baseStreamingUrl,
                                                      @Nonnull final ItagItem itagItem,
                                                      final DeliveryType deliveryType)
            throws YoutubeDashManifestCreationException {
        final boolean isAHtml5StreamingUrl = isWebStreamingUrl(baseStreamingUrl)
                || isTvHtml5SimplyEmbeddedPlayerStreamingUrl(baseStreamingUrl);
        final boolean isAnAndroidStreamingUrl = isAndroidStreamingUrl(baseStreamingUrl);
        final boolean isAnIosStreamingUrl = isIosStreamingUrl(baseStreamingUrl);
        if (isAHtml5StreamingUrl) {
            baseStreamingUrl += ALR_YES;
        }
        baseStreamingUrl = appendRnParamAndSqParamIfNeeded(baseStreamingUrl, deliveryType);

        final Downloader downloader = NewPipe.getDownloader();
        if (isAHtml5StreamingUrl) {
            final String mimeTypeExpected = itagItem.getMediaFormat().getMimeType();
            if (!isNullOrEmpty(mimeTypeExpected)) {
                return getStreamingWebUrlWithoutRedirects(downloader, baseStreamingUrl,
                        mimeTypeExpected, deliveryType);
            }
        } else if (isAnAndroidStreamingUrl || isAnIosStreamingUrl) {
            try {
                final Map<String, List<String>> headers = new HashMap<>();
                headers.put("User-Agent", Collections.singletonList(
                        isAnAndroidStreamingUrl ? getAndroidUserAgent(null)
                                : getIosUserAgent(null)));
                final byte[] emptyBody = "".getBytes(StandardCharsets.UTF_8);
                return downloader.post(baseStreamingUrl, headers, emptyBody);
            } catch (final IOException | ExtractionException e) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: error when trying to get the "
                                + (isAnIosStreamingUrl ? "ANDROID" : "IOS")
                                + " streaming URL response", e);
            }
        }

        try {
            return downloader.get(baseStreamingUrl);
        } catch (final IOException | ExtractionException e) {
                throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: error when trying to get the streaming "
                            + "URL response", e);
        }
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
     * and/or IP used to fetch the streaming URL.
     *
     * <p>
     * This method will follow redirects for web clients, which works in the following way:
     * <ol>
     *     <li>the {@link #ALR_YES} param is appended to all streaming URLs</li>
     *     <li>if no redirection occurs, the video server will return the streaming data;</li>
     *     <li>if a redirection occurs, the server will respond with HTTP status code 200 and a
     *     text/plain mime type. The redirection URL is the response body;</li>
     *     <li>the redirection URL is requested and the steps above from step 2 are repeated (until
     *     too many redirects are reached, of course).</li>
     * </ol>
     * </p>
     *
     * @param downloader               the {@link Downloader} instance to be used
     * @param streamingUrl             the streaming URL which we are trying to get a streaming URL
     *                                 without any redirection on the network and/or IP used
     * @param responseMimeTypeExpected the response mime type expected from Google video servers
     * @param deliveryType             the {@link DeliveryType} of the stream
     * @return the response of the stream which should have no redirections
     */
    @SuppressWarnings("checkstyle:FinalParameters")
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
                                "Could not generate the DASH manifest: could not get the "
                                        + "initialization URL of the post-live-DVR stream: "
                                        + "response code " + responseCode);
                    } else if (deliveryType == DeliveryType.OTF) {
                        throw new YoutubeDashManifestCreationException(
                                "Could not generate the DASH manifest: could not get the "
                                        + "initialization URL of the OTF stream: response code "
                                        + responseCode);
                    } else {
                        throw new YoutubeDashManifestCreationException(
                                "Could not generate the DASH manifest: could not fetch the URL of "
                                        + "the progressive stream: response code " + responseCode);
                    }
                }

                // A valid response must include a Content-Type header, so we can require that
                // the response from video servers has this header.
                try {
                    responseMimeType = Objects.requireNonNull(response.getHeader(
                            "Content-Type"));
                } catch (final NullPointerException e) {
                    throw new YoutubeDashManifestCreationException(
                            "Could not generate the DASH manifest: could not get the Content-Type "
                                    + "header from the streaming URL", e);
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
                        "Could not generate the DASH manifest: too many redirects when trying to "
                                + "get the WEB streaming URL response");
            }

            // This should never be reached, but is required because we don't want to return null
            // here
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: error when trying to get the WEB "
                            + "streaming URL response");
        } catch (final IOException | ExtractionException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate the DASH manifest: error when trying to get the WEB "
                            + "streaming URL response", e);
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
                    "Could not generate the DASH manifest: unable to get the length of the stream",
                    e);
        }
    }

    /**
     * Create a {@link Document} object and generate the {@code <MPD>} element of the manifest.
     *
     * <p>
     * The generated {@code <MPD>} element looks like the manifest returned into the player
     * response of videos with OTF streams:
     * </p>
     *
     * <p>
     * {@code <MPD xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xmlns="urn:mpeg:DASH:schema:MPD:2011"
     * xsi:schemaLocation="urn:mpeg:DASH:schema:MPD:2011 DASH-MPD.xsd" minBufferTime="PT1.500S"
     * profiles="urn:mpeg:dash:profile:isoff-main:2011" type="static"
     * mediaPresentationDuration="PT$duration$S">}
     * (where {@code $duration$} represents the duration in seconds (a number with 3 digits after
     * the decimal point)
     * </p>
     *
     * <p>
     * If the duration is an integer or a double with less than 3 digits after the decimal point,
     * it will be converted into a double with 3 digits after the decimal point.
     * </p>
     *
     * @param segmentDuration         the segment duration object extracted from the initialization
     *                                sequence of the stream
     * @param deliveryType            the {@link DeliveryType} of the stream, see the enum for
     *                                possible values
     * @param itagItem                the {@link ItagItem} which will be used to get the duration
     *                                of progressive streams
     * @param durationSecondsFallback the duration in seconds, extracted from player response, used
     *                                as a fallback if the duration could not be determined
     * @return a {@link Document} object which contains a {@code <MPD>} element
     */
    private static Document generateDocumentAndMpdElement(@Nonnull final String[] segmentDuration,
                                                          final DeliveryType deliveryType,
                                                          @Nonnull final ItagItem itagItem,
                                                          final long durationSecondsFallback)
            throws YoutubeDashManifestCreationException {
        try {
            final Document document = newDocument();

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
                final long itagItemDuration = itagItem.getApproxDurationMs();
                if (itagItemDuration != -1) {
                    streamDuration = itagItemDuration;
                } else {
                    if (durationSecondsFallback > 0) {
                        streamDuration = durationSecondsFallback * 1000;
                    } else {
                        throw new YoutubeDashManifestCreationException(
                                "Could not generate or append the MPD element of the DASH "
                                        + "manifest to the document: the duration of the stream "
                                        + "could not be determined and the "
                                        + "durationSecondsFallback is less than or equal to 0");
                    }
                }
            }
            final double duration = streamDuration / 1000.0;
            final String durationSeconds = String.format(Locale.ENGLISH, "%.3f", duration);
            mediaPresentationDurationAttribute.setValue("PT" + durationSeconds + "S");
            mpdElement.setAttributeNode(mediaPresentationDurationAttribute);

            return document;
        } catch (final Exception e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append the MPD element of the DASH manifest to the "
                            + "document", e);
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
     */
    private static void generatePeriodElement(@Nonnull final Document document)
            throws YoutubeDashManifestCreationException {
        try {
            final Element mpdElement = (Element) document.getElementsByTagName("MPD").item(0);
            final Element periodElement = document.createElement("Period");
            mpdElement.appendChild(periodElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append the Period element of the DASH manifest to the "
                            + "document", e);
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
     */
    private static void generateAdaptationSetElement(@Nonnull final Document document,
                                                     @Nonnull final ItagItem itagItem)
            throws YoutubeDashManifestCreationException {
        try {
            final Element periodElement = (Element) document.getElementsByTagName("Period")
                    .item(0);
            final Element adaptationSetElement = document.createElement("AdaptationSet");

            final Attr idAttribute = document.createAttribute("id");
            idAttribute.setValue("0");
            adaptationSetElement.setAttributeNode(idAttribute);

            final MediaFormat mediaFormat = itagItem.getMediaFormat();
            if (mediaFormat == null || isNullOrEmpty(mediaFormat.mimeType)) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the AdaptationSet element of the DASH manifest to the "
                                + "document: the MediaFormat or the mime type of the MediaFormat "
                                + "of the ItagItem is null or empty");
            }

            final Attr mimeTypeAttribute = document.createAttribute("mimeType");
            mimeTypeAttribute.setValue(mediaFormat.mimeType);
            adaptationSetElement.setAttributeNode(mimeTypeAttribute);

            final Attr subsegmentAlignmentAttribute = document.createAttribute(
                    "subsegmentAlignment");
            subsegmentAlignmentAttribute.setValue("true");
            adaptationSetElement.setAttributeNode(subsegmentAlignmentAttribute);

            periodElement.appendChild(adaptationSetElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append the AdaptationSet element of the DASH manifest "
                            + "to the document", e);
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
                    "Could not generate or append the Role element of the DASH manifest to the "
                            + "document", e);
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
                        "Could not generate the Representation element of the DASH manifest to "
                                + "the document: the id of the ItagItem is less than or equal to "
                                + "0");
            }
            final Attr idAttribute = document.createAttribute("id");
            idAttribute.setValue(String.valueOf(id));
            representationElement.setAttributeNode(idAttribute);

            final String codec = itagItem.getCodec();
            if (isNullOrEmpty(codec)) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the AdaptationSet element of the DASH manifest to the "
                                + "document: the codec value is null or empty");
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
                        "Could not generate the Representation element of the DASH manifest to "
                                + "the document: the bitrate of the ItagItem is less than or "
                                + "equal to 0");
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
                            "Could not generate the Representation element of the DASH manifest "
                                    + "to the document: the width and the height of the ItagItem "
                                    + "are less than or equal to 0");
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
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append the Representation element of the DASH manifest "
                            + "to the document", e);
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
     * @param itagItem the {@link ItagItem} to use, which cannot be null
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
                        "Could not generate the DASH manifest: the audioChannels value is less "
                                + "than or equal to 0 (" + audioChannels + ")");
            }
            valueAttribute.setValue(String.valueOf(itagItem.getAudioChannels()));
            audioChannelConfigurationElement.setAttributeNode(valueAttribute);

            representationElement.appendChild(audioChannelConfigurationElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append the AudioChannelConfiguration element of the "
                            + "DASH manifest to the document", e);
        }
    }

    /**
     * Generate the {@code <BaseURL>} element, appended as a child of the
     * {@code <Representation>} element.
     *
     * <p>
     * This method is only used when generating DASH manifests from progressive streams.
     * </p>
     *
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement(Document, ItagItem)}).
     * </p>
     *
     * @param document the {@link Document} on which the {@code <BaseURL>} element will
     *                 be appended
     * @param baseUrl  the base URL of the stream, which cannot be null and will be set as the
     *                 content of the {@code <BaseURL>} element
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
                    "Could not generate or append the BaseURL element of the DASH manifest to the "
                            + "document", e);
        }
    }

    /**
     * Generate the {@code <SegmentBase>} element, appended as a child of the
     * {@code <Representation>} element.
     *
     * <p>
     * This method is only used when generating DASH manifests from progressive streams.
     * </p>
     *
     * <p>
     * It generates the following element:
     * <br>
     * {@code <SegmentBase indexRange="indexStart-indexEnd"></SegmentBase>}
     * <br>
     * (where {@code indexStart} and {@code indexEnd} are gotten from the {@link ItagItem} passed
     * as the second parameter)
     * </p>
     *
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement(Document, ItagItem)}).
     * </p>
     *
     * @param document the {@link Document} on which the {@code <SegmentBase>} element will
     *                 be appended
     * @param itagItem the {@link ItagItem} to use, which cannot be null
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
                        "Could not generate the DASH manifest: the indexStart value of the "
                                + "ItagItem is less than to 0 (" + indexStart + ")");
            }
            final int indexEnd = itagItem.getIndexEnd();
            if (indexEnd < 0) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: the indexEnd value of the ItagItem "
                                + "is less than to 0 (" + indexStart + ")");
            }

            indexRangeAttribute.setValue(indexStart + "-" + indexEnd);
            segmentBaseElement.setAttributeNode(indexRangeAttribute);

            representationElement.appendChild(segmentBaseElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append the SegmentBase element of the DASH manifest to "
                            + "the document", e);
        }
    }

    /**
     * Generate the {@code <Initialization>} element, appended as a child of the
     * {@code <SegmentBase>} element.
     *
     * <p>
     * This method is only used when generating DASH manifests from progressive streams.
     * </p>
     *
     * <p>
     * It generates the following element:
     * <br>
     * {@code <Initialization range="initStart-initEnd"/>}
     * <br>
     * (where {@code indexStart} and {@code indexEnd} are gotten from the {@link ItagItem} passed
     * as the second parameter)
     * </p>
     *
     * <p>
     * The {@code <SegmentBase>} element needs to be generated before this element with
     * {@link #generateSegmentBaseElement(Document, ItagItem)}).
     * </p>
     *
     * @param document the {@link Document} on which the {@code <Initialization>} element will
     *                 be appended
     * @param itagItem the {@link ItagItem} to use, which cannot be null
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
                        "Could not generate the DASH manifest: the initStart value of the "
                                + "ItagItem is less than to 0 (" + initStart + ")");
            }
            final int initEnd = itagItem.getInitEnd();
            if (initEnd < 0) {
                throw new YoutubeDashManifestCreationException(
                        "Could not generate the DASH manifest: the initEnd value of the ItagItem "
                                + "is less than to 0 (" + initEnd + ")");
            }

            rangeAttribute.setValue(initStart + "-" + initEnd);
            initializationElement.setAttributeNode(rangeAttribute);

            segmentBaseElement.appendChild(initializationElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append the Initialization element of the DASH manifest "
                            + "to the document", e);
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
     * @param deliveryType the stream {@link DeliveryType delivery type}
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
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append the SegmentTemplate element of the DASH "
                            + "manifest to the document", e);
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
    private static void generateSegmentTimelineElement(@Nonnull final Document document)
            throws YoutubeDashManifestCreationException {
        try {
            final Element segmentTemplateElement = (Element) document.getElementsByTagName(
                    "SegmentTemplate").item(0);
            final Element segmentTimelineElement = document.createElement("SegmentTimeline");

            segmentTemplateElement.appendChild(segmentTimelineElement);
        } catch (final DOMException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append the SegmentTimeline element of the DASH "
                            + "manifest to the document", e);
        }
    }

    /**
     * Generate segment elements for OTF streams.
     *
     * <p>
     * By parsing by the first media sequence, we know how many durations and repetitions there are
     * so we just have to loop into segment durations to generate the following elements for each:
     * </p>
     *
     * <p>
     * {@code <S d="segmentDuration" r="durationRepetition" />}
     * </p>
     *
     * <p>
     * If there is no repetition of the duration between two segments, the {@code r} attribute is
     * not added to the {@code S} element.
     * </p>
     *
     * <p>
     * These elements will be appended as children of the {@code <SegmentTimeline>} element.
     * </p>
     *
     * <p>
     * The {@code <SegmentTimeline>} element needs to be generated before this element with
     * {@link #generateSegmentTimelineElement(Document)}.
     * </p>
     *
     * @param segmentDurations the sequences "length" or "length(r=repeat_count" extracted with the
     *                         regexes
     * @param document the {@link Document} on which the the {@code <S>} elements will be appended
     */
    private static void generateSegmentElementsForOtfStreams(final String[] segmentDurations,
                                                             final Document document)
            throws YoutubeDashManifestCreationException {

        try {
            final Element segmentTimelineElement = (Element) document.getElementsByTagName(
                    "SegmentTimeline").item(0);

            for (final String segmentDuration : segmentDurations) {
                final Element sElement = document.createElement("S");

                final String[] segmentLengthRepeat = segmentDuration.split("\\(r=");
                // make sure segmentLengthRepeat[0], which is the length, is convertible to int
                Integer.parseInt(segmentLengthRepeat[0]);

                // There are repetitions of a segment duration in other segments
                if (segmentLengthRepeat.length > 1) {
                    final int segmentRepeatCount = Integer.parseInt(
                            Utils.removeNonDigitCharacters(segmentLengthRepeat[1]));
                    final Attr rAttribute = document.createAttribute("r");
                    rAttribute.setValue(String.valueOf(segmentRepeatCount));
                    sElement.setAttributeNode(rAttribute);
                }

                final Attr dAttribute = document.createAttribute("d");
                dAttribute.setValue(segmentLengthRepeat[0]);
                sElement.setAttributeNode(dAttribute);

                segmentTimelineElement.appendChild(sElement);
            }

        } catch (final DOMException | IllegalStateException | IndexOutOfBoundsException
                | NumberFormatException e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not generate or append the segment (S) elements of the DASH manifest "
                            + "to the document", e);
        }
    }

    /**
     * Generate the segment element for post-live-DVR streams.
     *
     * <p>
     * We don't know the exact duration of segments for post-live-DVR streams but an
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
                    "Could not generate or append the segment (S) elements of the DASH manifest "
                            + "to the document", e);
        }
    }

    /**
     * Convert a DASH manifest {@link Document document} to a string and cache it.
     *
     * @param originalBaseStreamingUrl the original base URL of the stream
     * @param document                 the document to be converted
     * @param manifestCreatorCache     the {@link ManifestCreatorCache} on which store the
     *                                 string generated (use either {@link #OTF_CACHE},
     *                                 {@link #POST_LIVE_DVR_CACHE} or {@link #PROGRESSIVE_CACHE})
     * @return the DASH manifest {@link Document document} converted to a string
     */
    private static String buildAndCacheResult(
            @Nonnull final String originalBaseStreamingUrl,
            @Nonnull final Document document,
            @Nonnull final ManifestCreatorCache<String, String> manifestCreatorCache)
            throws YoutubeDashManifestCreationException {

        try {
            final String documentXml = documentToXml(document);
            manifestCreatorCache.put(originalBaseStreamingUrl, documentXml);
            return documentXml;
        } catch (final Exception e) {
            throw new YoutubeDashManifestCreationException(
                    "Could not convert the DASH manifest generated to a string", e);
        }
    }

    /**
     * Securing against XEE is done by passing {@code false} to {@link
     * DocumentBuilderFactory#setExpandEntityReferences(boolean)}, also see
     * <a href="https://github.com/ChuckerTeam/chucker/pull/201">ChuckerTeam/chucker#201</a>.
     *
     * @return an instance of document secured against XEE attacks, that should then be convertible
     *         to an XML string without security problems
     * @see #documentToXml(Document) Use documentToXml to convert the created document to XML, which
     *                               is also secured against XEE!
     */
    private static Document newDocument() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory
                = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setExpandEntityReferences(false);

        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.newDocument();
    }

    /**
     * Securing against XEE is done by setting {@link XMLConstants#FEATURE_SECURE_PROCESSING} to
     * {@code true} in the {@link TransformerFactory}, also see
     * <a href="https://github.com/ChuckerTeam/chucker/pull/201">ChuckerTeam/chucker#201</a>.
     * The best way to do this would be setting the attributes {@link
     * XMLConstants#ACCESS_EXTERNAL_DTD} and {@link XMLConstants#ACCESS_EXTERNAL_STYLESHEET}, but
     * unfortunately the engine on Android does not support them.
     *
     * @param document the document to convert; must have been created using {@link #newDocument()}
     *                 to properly prevent XEE attacks!
     * @return the document converted to an XML string, making sure there can't be XEE attacks
     */
    private static String documentToXml(@Nonnull final Document document)
            throws TransformerException {

        @SuppressWarnings("java:S2755") // see javadoc: this is actually taken care of
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        final Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        final StringWriter result = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(result));

        return result.toString();
    }

    /**
     * @return the cache of DASH manifests generated for OTF streams
     */
    public static ManifestCreatorCache<String, String> getOtfManifestsCache() {
        return OTF_CACHE;
    }

    /**
     * @return the cache of DASH manifests generated for post-live-DVR streams
     */
    public static ManifestCreatorCache<String, String> getPostLiveDvrManifestsCache() {
        return POST_LIVE_DVR_CACHE;
    }

    /**
     * @return the cache of DASH manifests generated for progressive streams
     */
    public static ManifestCreatorCache<String, String> getProgressiveManifestsCache() {
        return PROGRESSIVE_CACHE;
    }
}
