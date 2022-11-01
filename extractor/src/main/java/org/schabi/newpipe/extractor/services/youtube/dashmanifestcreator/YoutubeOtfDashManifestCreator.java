package org.schabi.newpipe.extractor.services.youtube.dashmanifestcreator;

import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.SEGMENT_TIMELINE;
import static org.schabi.newpipe.extractor.utils.Utils.isBlank;
import static org.schabi.newpipe.extractor.utils.Utils.removeNonDigitCharacters;

import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.services.youtube.itag.info.ItagInfo;
import org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreationException;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class YoutubeOtfDashManifestCreator extends AbstractYoutubeDashManifestCreator {

    public YoutubeOtfDashManifestCreator(@Nonnull final ItagInfo<?> itagInfo,
                                         final long durationSecondsFallback) {
        super(itagInfo, durationSecondsFallback);
    }

    @Nonnull
    @Override
    public String generateManifest() {
        // Try to avoid redirects when streaming the content by saving the last URL we get
        // from video servers.
        final Response response = getInitializationResponse(itagInfo.getStreamUrl());
        final String otfBaseStreamingUrl = response.latestUrl()
                .replace(SQ_0, "")
                .replace(RN_0, "")
                .replace(ALR_YES, "");

        final int responseCode = response.responseCode();
        if (responseCode != 200) {
            throw new DashManifestCreationException("Could not get the initialization URL: "
                    + "response code " + responseCode);
        }

        final String[] segmentDurations;

        try {
            final String[] segmentsAndDurationsResponseSplit = response.responseBody()
                    // Get the lines with the durations and the following
                    .split("Segment-Durations-Ms: ")[1]
                    // Remove the other lines
                    .split("\n")[0]
                    // Get all durations and repetitions which are separated by a comma
                    .split(",");
            final int lastIndex = segmentsAndDurationsResponseSplit.length - 1;
            segmentDurations = isBlank(segmentsAndDurationsResponseSplit[lastIndex])
                    ? Arrays.copyOf(segmentsAndDurationsResponseSplit, lastIndex)
                    : segmentsAndDurationsResponseSplit;
        } catch (final Exception e) {
            throw new DashManifestCreationException("Could not get segment durations", e);
        }

        long streamDurationMs;
        try {
            streamDurationMs = getStreamDuration(segmentDurations);
        } catch (final DashManifestCreationException e) {
            streamDurationMs = durationSecondsFallback * 1000;
        }

        generateDocumentAndCommonElements(streamDurationMs);
        generateSegmentTemplateElement(otfBaseStreamingUrl);
        generateSegmentTimelineElement();
        generateSegmentElementsForOtfStreams(segmentDurations);

        return documentToXmlSafe();
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
     * {@link #generateSegmentTimelineElement()}.
     * </p>
     *
     * @param segmentDurations the sequences "length" or "length(r=repeat_count" extracted with the
     *                         regular expressions
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateSegmentElementsForOtfStreams(@Nonnull final String[] segmentDurations) {
        try {
            final Element segmentTimelineElement = getFirstElementByName(SEGMENT_TIMELINE);
            streamAndSplitSegmentDurations(segmentDurations)
                    .map(segmentLengthRepeat -> {
                        final Element sElement = createElement("S");
                        // There are repetitions of a segment duration in other segments
                        if (segmentLengthRepeat.length > 1) {
                            appendNewAttrWithValue(sElement, "r", Integer.parseInt(
                                    removeNonDigitCharacters(segmentLengthRepeat[1])));
                        }

                        appendNewAttrWithValue(
                                sElement, "d", Integer.parseInt(segmentLengthRepeat[0]));
                        return sElement;
                    })
                    .forEach(segmentTimelineElement::appendChild);
        } catch (final Exception e) {
            throw DashManifestCreationException.couldNotAddElement("segment (S)", e);
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
     * @param segmentDurations the segment duration object extracted from the initialization
     *                         sequence of the stream
     * @return the duration of the OTF stream, in milliseconds
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected long getStreamDuration(@Nonnull final String[] segmentDurations) {
        try {
            return streamAndSplitSegmentDurations(segmentDurations)
                    .mapToLong(segmentLengthRepeat -> {
                        final long segmentLength = Integer.parseInt(segmentLengthRepeat[0]);
                        final long segmentRepeatCount = segmentLengthRepeat.length > 1
                                ? Long.parseLong(removeNonDigitCharacters(segmentLengthRepeat[1]))
                                : 0;
                        return segmentLength + segmentRepeatCount * segmentLength;
                    })
                    .sum();
        } catch (final NumberFormatException e) {
            throw new DashManifestCreationException(
                    "Could not get stream length from sequences list", e);
        }
    }

    protected Stream<String[]> streamAndSplitSegmentDurations(@Nonnull final String[] durations) {
        return Stream.of(durations)
                .map(segDuration -> segDuration.split("\\(r="));
    }
}
