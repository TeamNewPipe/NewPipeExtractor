package org.schabi.newpipe.extractor.services.youtube.dashmanifestcreator;

import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.SEGMENT_TIMELINE;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.services.youtube.itag.info.ItagInfo;
import org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreationException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class YoutubePostLiveStreamDvrDashManifestCreator extends AbstractYoutubeDashManifestCreator {

    public YoutubePostLiveStreamDvrDashManifestCreator(@Nonnull final ItagInfo<?> itagInfo,
                                                       final long durationSecondsFallback) {
        super(itagInfo, durationSecondsFallback);
    }

    @Override
    protected boolean isLiveDelivery() {
        return true;
    }

    @Nonnull
    @Override
    public String generateManifest() {
        final Integer targetDurationSec = itagInfo.getTargetDurationSec();
        if (targetDurationSec == null || targetDurationSec <= 0) {
            throw new DashManifestCreationException(
                    "Invalid value for 'targetDurationSec'=" + targetDurationSec);
        }

        // Try to avoid redirects when streaming the content by saving the latest URL we get
        // from video servers.
        final Response response = getInitializationResponse(itagInfo.getStreamUrl());
        final String realPostLiveStreamDvrStreamingUrl = response.latestUrl()
                .replace(SQ_0, "")
                .replace(RN_0, "")
                .replace(ALR_YES, "");

        final int responseCode = response.responseCode();
        if (responseCode != 200) {
            throw new DashManifestCreationException(
                    "Could not get the initialization sequence: response code " + responseCode);
        }

        final String streamDurationMsString;
        final String segmentCount;
        try {
            final Map<String, List<String>> responseHeaders = response.responseHeaders();
            streamDurationMsString = responseHeaders.get("X-Head-Time-Millis").get(0);
            segmentCount = responseHeaders.get("X-Head-Seqnum").get(0);
        } catch (final IndexOutOfBoundsException e) {
            throw new DashManifestCreationException(
                    "Could not get the value of the X-Head-Time-Millis or the X-Head-Seqnum header",
                    e);
        }

        if (isNullOrEmpty(segmentCount)) {
            throw new DashManifestCreationException("Could not get the number of segments");
        }

        long streamDurationMs;
        try {
            streamDurationMs = Long.parseLong(streamDurationMsString);
        } catch (final NumberFormatException e) {
            streamDurationMs = durationSecondsFallback * 1000;
        }

        generateDocumentAndCommonElements(streamDurationMs);

        generateSegmentTemplateElement(realPostLiveStreamDvrStreamingUrl);
        generateSegmentTimelineElement();
        generateSegmentElementForPostLiveDvrStreams(targetDurationSec, segmentCount);

        return documentToXmlSafe();
    }

    /**
     * Generate the segment ({@code <S>}) element.
     *
     * <p>
     * We don't know the exact duration of segments for post-live-DVR streams but an
     * average instead (which is the {@code targetDurationSec} value), so we can use the following
     * structure to generate the segment timeline for DASH manifests of ended livestreams:
     * <br>
     * {@code <S d="targetDurationSecValue" r="segmentCount" />}
     * </p>
     *
     * @param targetDurationSeconds the {@code targetDurationSec} value from YouTube player
     *                              response's stream
     * @param segmentCount          the number of segments
     * @throws DashManifestCreationException May throw a CreationException
     */
    private void generateSegmentElementForPostLiveDvrStreams(
            final int targetDurationSeconds,
            @Nonnull final String segmentCount
    ) {
        try {
            final Element sElement = document.createElement("S");

            appendNewAttrWithValue(sElement, "d", targetDurationSeconds * 1000);

            final Attr rAttribute = document.createAttribute("r");
            rAttribute.setValue(segmentCount);
            sElement.setAttributeNode(rAttribute);

            appendNewAttrWithValue(sElement, "r", segmentCount);

            getFirstElementByName(SEGMENT_TIMELINE).appendChild(sElement);
        } catch (final DOMException e) {
            throw DashManifestCreationException.couldNotAddElement("segment (S)", e);
        }
    }
}
