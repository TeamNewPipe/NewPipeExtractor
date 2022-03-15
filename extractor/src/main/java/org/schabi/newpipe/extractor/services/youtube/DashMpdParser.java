/*
 * Created by Christian Schabesberger on 02.02.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * DashMpdParser.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Class to extract streams from a DASH manifest.
 *
 * <p>
 * Note that this class relies on the YouTube's {@link ItagItem} class and should be made generic
 * in order to be used on other services.
 * </p>
 *
 * <p>
 * This class is not used by the extractor itself, as all streams are supported by the extractor.
 * </p>
 */
public final class DashMpdParser {
    private DashMpdParser() {
    }

    /**
     * Exception class which is thrown when something went wrong when using
     * {@link DashMpdParser#getStreams(String)}.
     */
    public static class DashMpdParsingException extends ParsingException {

        DashMpdParsingException(final String message, final Exception e) {
            super(message, e);
        }
    }

    /**
     * Class which represents the result of a DASH MPD file parsing by {@link DashMpdParser}.
     *
     * <p>
     * The result contains video, video-only and audio streams.
     * </p>
     */
    public static class Result {
        private final List<VideoStream> videoStreams;
        private final List<VideoStream> videoOnlyStreams;
        private final List<AudioStream> audioStreams;

        Result(final List<VideoStream> videoStreams,
               final List<VideoStream> videoOnlyStreams,
               final List<AudioStream> audioStreams) {
            this.videoStreams = videoStreams;
            this.videoOnlyStreams = videoOnlyStreams;
            this.audioStreams = audioStreams;
        }

        public List<VideoStream> getVideoStreams() {
            return videoStreams;
        }

        public List<VideoStream> getVideoOnlyStreams() {
            return videoOnlyStreams;
        }

        public List<AudioStream> getAudioStreams() {
            return audioStreams;
        }
    }

    /**
     * This method will try to download and parse the YouTube DASH MPD manifest URL provided to get
     * supported {@link AudioStream}s and {@link VideoStream}s.
     *
     * <p>
     * The parser supports video, video-only and audio streams.
     * </p>
     *
     * @param dashMpdUrl the URL of the DASH MPD manifest
     * @return a {@link Result} which contains all video, video-only and audio streams extracted
     * and supported by the extractor (so the ones for which {@link ItagItem#isSupported(int)}
     * returns {@code true}).
     * @throws DashMpdParsingException if something went wrong when downloading or parsing the
     * manifest
     * @see <a href="https://www.brendanlong.com/the-structure-of-an-mpeg-dash-mpd.html">
     *     www.brendanlong.com's page about the structure of an MPEG-DASH MPD manifest</a>
     */
    @Nonnull
    public static Result getStreams(final String dashMpdUrl)
            throws DashMpdParsingException, ReCaptchaException {
        final String dashDoc;
        final Downloader downloader = NewPipe.getDownloader();
        try {
            dashDoc = downloader.get(dashMpdUrl).responseBody();
        } catch (final IOException e) {
            throw new DashMpdParsingException("Could not fetch DASH manifest: " + dashMpdUrl, e);
        }

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final InputStream stream = new ByteArrayInputStream(dashDoc.getBytes());

            final Document doc = builder.parse(stream);
            final NodeList representationList = doc.getElementsByTagName("Representation");

            final List<VideoStream> videoStreams = new ArrayList<>();
            final List<AudioStream> audioStreams = new ArrayList<>();
            final List<VideoStream> videoOnlyStreams = new ArrayList<>();

            for (int i = 0; i < representationList.getLength(); i++) {
                final Element representation = (Element) representationList.item(i);
                try {
                    final String mimeType = ((Element) representation.getParentNode())
                            .getAttribute("mimeType");
                    final String id = representation.getAttribute("id");
                    final ItagItem itag = ItagItem.getItag(Integer.parseInt(id));
                    final Element segmentationList = (Element) representation
                            .getElementsByTagName("SegmentList").item(0);

                    if (segmentationList == null) {
                        continue;
                    }

                    final MediaFormat mediaFormat = MediaFormat.getFromMimeType(mimeType);

                    if (itag.itagType.equals(ItagItem.ItagType.AUDIO)) {
                        audioStreams.add(new AudioStream.Builder()
                                .setId(String.valueOf(itag.id))
                                .setContent(manualDashFromRepresentation(doc, representation),
                                        false)
                                .setMediaFormat(mediaFormat)
                                .setDeliveryMethod(DeliveryMethod.DASH)
                                .setAverageBitrate(itag.getAverageBitrate())
                                .setBaseUrl(dashMpdUrl)
                                .setItagItem(itag)
                                .build());
                    } else {
                        final boolean isVideoOnly = itag.itagType == ItagItem.ItagType.VIDEO_ONLY;
                        final VideoStream videoStream = new VideoStream.Builder()
                                .setId(String.valueOf(itag.id))
                                .setContent(manualDashFromRepresentation(doc, representation),
                                        false)
                                .setMediaFormat(mediaFormat)
                                .setDeliveryMethod(DeliveryMethod.DASH)
                                .setResolution(Objects.requireNonNull(itag.getResolutionString()))
                                .setIsVideoOnly(isVideoOnly)
                                .setBaseUrl(dashMpdUrl)
                                .setItagItem(itag)
                                .build();
                        if (isVideoOnly) {
                            videoOnlyStreams.add(videoStream);
                        } else {
                            videoStreams.add(videoStream);
                        }
                    }
                } catch (final Exception ignored) {
                }
            }
            return new Result(videoStreams, videoOnlyStreams, audioStreams);
        } catch (final Exception e) {
            throw new DashMpdParsingException("Could not parse DASH MPD", e);
        }
    }

    @Nonnull
    private static String manualDashFromRepresentation(@Nonnull final Document document,
                                                       @Nonnull final Element representation)
            throws TransformerException {
        final Element mpdElement = (Element) document.getElementsByTagName("MPD").item(0);

        // Clone the element so we can freely modify it
        final Element adaptationSet = (Element) representation.getParentNode();
        final Element adaptationSetClone = (Element) adaptationSet.cloneNode(true);

        // Remove other representations from the adaptation set
        final NodeList representations = adaptationSetClone.getElementsByTagName("Representation");
        for (int i = representations.getLength() - 1; i >= 0; i--) {
            final Node item = representations.item(i);
            if (!item.isEqualNode(representation)) {
                adaptationSetClone.removeChild(item);
            }
        }

        final Element newMpdRootElement = (Element) mpdElement.cloneNode(false);
        final Element periodElement = newMpdRootElement.getOwnerDocument().createElement("Period");
        periodElement.appendChild(adaptationSetClone);
        newMpdRootElement.appendChild(periodElement);

        return nodeToString(newMpdRootElement);
    }

    private static String nodeToString(final Node node) throws TransformerException {
        final StringWriter result = new StringWriter();
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(node), new StreamResult(result));
        return result.toString();
    }
}
