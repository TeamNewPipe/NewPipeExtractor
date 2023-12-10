package org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators;

import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.ALR_YES;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.RN_0;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.SEGMENT_TIMELINE;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.SQ_0;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.buildAndCacheResult;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.generateDocumentAndDoCommonElementsGeneration;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.generateSegmentTemplateElement;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.generateSegmentTimelineElement;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.getInitializationResponse;
import static org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.setAttribute;
import static org.schabi.newpipe.extractor.utils.Utils.isBlank;

import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.services.youtube.DeliveryType;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache;
import org.schabi.newpipe.extractor.utils.Utils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Class which generates DASH manifests of YouTube {@link DeliveryType#OTF OTF streams}.
 */
public final class YoutubeOtfDashManifestCreator {

    /**
     * Cache of DASH manifests generated for OTF streams.
     */
    private static final ManifestCreatorCache<String, String> OTF_STREAMS_CACHE
            = new ManifestCreatorCache<>();

    private YoutubeOtfDashManifestCreator() {
    }

    /**
     * Create DASH manifests from a YouTube OTF stream.
     *
     * <p>
     * OTF streams are YouTube-DASH specific streams which work with sequences and without the need
     * to get a manifest (even if one is provided, it is not used by official clients).
     * </p>
     *
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
     *          sequence parameter is appended (see {@link YoutubeDashManifestCreatorsUtils#SQ_0}))
     *          with a {@code POST} or {@code GET} request (depending of the client on which the
     *          streaming URL comes from is a mobile one ({@code POST}) or not ({@code GET}));</li>
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
     * @param otfBaseStreamingUrl     the base URL of the OTF stream, which must not be null
     * @param itagItem                the {@link ItagItem} corresponding to the stream, which
     *                                must not be null
     * @param durationSecondsFallback the duration of the video, which will be used if the duration
     *                                could not be extracted from the first sequence
     * @return the manifest generated into a string
     */
    @Nonnull
    public static String fromOtfStreamingUrl(
            @Nonnull final String otfBaseStreamingUrl,
            @Nonnull final ItagItem itagItem,
            final long durationSecondsFallback) throws CreationException {
        if (OTF_STREAMS_CACHE.containsKey(otfBaseStreamingUrl)) {
            return Objects.requireNonNull(OTF_STREAMS_CACHE.get(otfBaseStreamingUrl)).getSecond();
        }

        String realOtfBaseStreamingUrl = otfBaseStreamingUrl;
        // Try to avoid redirects when streaming the content by saving the last URL we get
        // from video servers.
        final Response response = getInitializationResponse(realOtfBaseStreamingUrl,
                itagItem, DeliveryType.OTF);
        realOtfBaseStreamingUrl = response.latestUrl().replace(SQ_0, "")
                .replace(RN_0, "").replace(ALR_YES, "");

        final int responseCode = response.responseCode();
        if (responseCode != 200) {
            throw new CreationException("Could not get the initialization URL: response code "
                    + responseCode);
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
            throw new CreationException("Could not get segment durations", e);
        }

        long streamDuration;
        try {
            streamDuration = getStreamDuration(segmentDuration);
        } catch (final CreationException e) {
            streamDuration = durationSecondsFallback * 1000;
        }

        final Document doc = generateDocumentAndDoCommonElementsGeneration(itagItem,
                streamDuration);

        generateSegmentTemplateElement(doc, realOtfBaseStreamingUrl, DeliveryType.OTF);
        generateSegmentTimelineElement(doc);
        generateSegmentElementsForOtfStreams(segmentDuration, doc);

        return buildAndCacheResult(otfBaseStreamingUrl, doc, OTF_STREAMS_CACHE);
    }

    /**
     * @return the cache of DASH manifests generated for OTF streams
     */
    @Nonnull
    public static ManifestCreatorCache<String, String> getCache() {
        return OTF_STREAMS_CACHE;
    }

    /**
     * Generate segment elements for OTF streams.
     *
     * <p>
     * By parsing by the first media sequence, we know how many durations and repetitions there are
     * so we just have to loop into segment durations to generate the following elements for each
     * duration repeated X times:
     * </p>
     *
     * <p>
     * {@code <S d="segmentDuration" r="durationRepetition" />}
     * </p>
     *
     * <p>
     * If there is no repetition of the duration between two segments, the {@code r} attribute is
     * not added to the {@code S} element, as it is not needed.
     * </p>
     *
     * <p>
     * These elements will be appended as children of the {@code <SegmentTimeline>} element, which
     * needs to be generated before these elements with
     * {@link YoutubeDashManifestCreatorsUtils#generateSegmentTimelineElement(Document)}.
     * </p>
     *
     * @param segmentDurations the sequences "length" or "length(r=repeat_count" extracted with the
     *                         regular expressions
     * @param doc              the {@link Document} on which the {@code <S>} elements will be
     *                         appended
     */
    private static void generateSegmentElementsForOtfStreams(
            @Nonnull final String[] segmentDurations,
            @Nonnull final Document doc) throws CreationException {
        try {
            final Element segmentTimelineElement = (Element) doc.getElementsByTagName(
                    SEGMENT_TIMELINE).item(0);

            for (final String segmentDuration : segmentDurations) {
                final Element sElement = doc.createElement("S");

                final String[] segmentLengthRepeat = segmentDuration.split("\\(r=");
                // make sure segmentLengthRepeat[0], which is the length, is convertible to int
                Integer.parseInt(segmentLengthRepeat[0]);

                // There are repetitions of a segment duration in other segments
                if (segmentLengthRepeat.length > 1) {
                    final int segmentRepeatCount = Integer.parseInt(
                            Utils.removeNonDigitCharacters(segmentLengthRepeat[1]));
                    setAttribute(sElement, doc, "r", String.valueOf(segmentRepeatCount));
                }
                setAttribute(sElement, doc, "d", segmentLengthRepeat[0]);

                segmentTimelineElement.appendChild(sElement);
            }

        } catch (final DOMException | IllegalStateException | IndexOutOfBoundsException
                       | NumberFormatException e) {
            throw CreationException.couldNotAddElement("segment (S)", e);
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
     * @return the duration of the OTF stream, in milliseconds
     */
    private static long getStreamDuration(@Nonnull final String[] segmentDuration)
            throws CreationException {
        try {
            long streamLengthMs = 0;

            for (final String segDuration : segmentDuration) {
                final String[] segmentLengthRepeat = segDuration.split("\\(r=");
                long segmentRepeatCount = 0;

                // There are repetitions of a segment duration in other segments
                if (segmentLengthRepeat.length > 1) {
                    segmentRepeatCount = Long.parseLong(Utils.removeNonDigitCharacters(
                            segmentLengthRepeat[1]));
                }

                final long segmentLength = Integer.parseInt(segmentLengthRepeat[0]);
                streamLengthMs += segmentLength + segmentRepeatCount * segmentLength;
            }

            return streamLengthMs;
        } catch (final NumberFormatException e) {
            throw new CreationException("Could not get stream length from sequences list", e);
        }
    }
}
