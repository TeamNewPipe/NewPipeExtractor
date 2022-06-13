package org.schabi.newpipe.extractor.services.youtube.dashmanifestcreator;

import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.BASE_URL;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.INITIALIZATION;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.MPD;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.REPRESENTATION;
import static org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreatorConstants.SEGMENT_BASE;

import org.schabi.newpipe.extractor.services.youtube.itag.info.ItagInfo;
import org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator.DashManifestCreationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;

public class YoutubeProgressiveDashManifestCreator extends AbstractYoutubeDashManifestCreator {

    public YoutubeProgressiveDashManifestCreator(@Nonnull final ItagInfo<?> itagInfo,
                                                 final long durationSecondsFallback) {
        super(itagInfo, durationSecondsFallback);
    }

    @Nonnull
    @Override
    public String generateManifest() {
        final long streamDurationMs;
        if (itagInfo.getApproxDurationMs() != null) {
            streamDurationMs = itagInfo.getApproxDurationMs();
        } else if (durationSecondsFallback > 0) {
            streamDurationMs = durationSecondsFallback * 1000;
        } else {
            throw DashManifestCreationException.couldNotAddElement(MPD, "the duration of the " +
                    "stream could not be determined and durationSecondsFallback is <= 0");
        }

        generateDocumentAndCommonElements(streamDurationMs);
        generateSegmentBaseElement();
        generateInitializationElement();

        return documentToXmlSafe();
    }

    /**
     * Generate the {@code <BaseURL>} element, appended as a child of the
     * {@code <Representation>} element.
     *
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement()}).
     * </p>
     *
     * @param baseUrl the base URL of the stream, which must not be null and will be set as the
     *                content of the {@code <BaseURL>} element
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateBaseUrlElement(@Nonnull final String baseUrl)
            throws DashManifestCreationException {
        try {
            final Element representationElement = getFirstElementByName(REPRESENTATION);

            appendNewAttrWithValue(representationElement, BASE_URL, baseUrl);
        } catch (final DOMException e) {
            throw DashManifestCreationException.couldNotAddElement(BASE_URL, e);
        }
    }

    /**
     * Generate the {@code <SegmentBase>} element, appended as a child of the
     * {@code <Representation>} element.
     *
     * <p>
     * It generates the following element:
     * <br>
     * {@code <SegmentBase indexRange="indexStart-indexEnd" />}
     * <br>
     * (where {@code indexStart} and {@code indexEnd} are gotten from the {@link ItagInfo} passed
     * as the second parameter)
     * </p>
     *
     * <p>
     * The {@code <Representation>} element needs to be generated before this element with
     * {@link #generateRepresentationElement()}),
     * and the {@code BaseURL} element with {@link #generateBaseUrlElement(String)}
     * should be generated too.
     * </p>
     *
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateSegmentBaseElement() {
        try {
            final Element segmentBaseElement = createElement(SEGMENT_BASE);

            if (itagInfo.getIndexRange() == null
                    || itagInfo.getIndexRange().start() < 0
                    || itagInfo.getIndexRange().end() < 0) {
                throw DashManifestCreationException.couldNotAddElement(SEGMENT_BASE,
                        "invalid index-range: " + itagInfo.getIndexRange());
            }

            appendNewAttrWithValue(
                    segmentBaseElement,
                    "indexRange",
                    itagInfo.getIndexRange().start() + "-" + itagInfo.getIndexRange().end());

            getFirstElementByName(REPRESENTATION).appendChild(segmentBaseElement);
        } catch (final DOMException e) {
            throw DashManifestCreationException.couldNotAddElement(SEGMENT_BASE, e);
        }
    }

    /**
     * Generate the {@code <Initialization>} element, appended as a child of the
     * {@code <SegmentBase>} element.
     *
     * <p>
     * It generates the following element:
     * <br>
     * {@code <Initialization range="initStart-initEnd"/>}
     * <br>
     * (where {@code indexStart} and {@code indexEnd} are gotten from the {@link ItagInfo} passed
     * as the second parameter)
     * </p>
     *
     * <p>
     * The {@code <SegmentBase>} element needs to be generated before this element with
     * {@link #generateSegmentBaseElement()}).
     * </p>
     *
     * @throws DashManifestCreationException May throw a CreationException
     */
    protected void generateInitializationElement() {
        try {
            final Element initializationElement = createElement(INITIALIZATION);

            if (itagInfo.getInitRange() == null
                    || itagInfo.getInitRange().start() < 0
                    || itagInfo.getInitRange().end() < 0) {
                throw DashManifestCreationException.couldNotAddElement(SEGMENT_BASE,
                        "invalid (init)-range: " + itagInfo.getInitRange());
            }

            appendNewAttrWithValue(
                    initializationElement,
                    "range",
                    itagInfo.getInitRange().start() + "-" + itagInfo.getInitRange().end());

            getFirstElementByName(SEGMENT_BASE).appendChild(initializationElement);
        } catch (final DOMException e) {
            throw DashManifestCreationException.couldNotAddElement(INITIALIZATION, e);
        }
    }


    @Nonnull
    @Override
    protected String appendBaseStreamingUrlParams(@Nonnull final String baseStreamingUrl) {
        return baseStreamingUrl + RN_0;
    }
}
