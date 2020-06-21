package org.schabi.newpipe.extractor.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryFormat;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by Christian Schabesberger on 02.02.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * DashMpdParser.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class DashMpdParser {

    private DashMpdParser() {
    }

    public static class DashMpdParsingException extends ParsingException {
        DashMpdParsingException(String message, Exception e) {
            super(message, e);
        }
    }

    public static class Result {
        private final List<VideoStream> videoStreams;
        private final List<VideoStream> videoOnlyStreams;
        private final List<AudioStream> audioStreams;


        public Result(List<VideoStream> videoStreams,
                      List<VideoStream> videoOnlyStreams,
                      List<AudioStream> audioStreams) {
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

    // TODO: Make this class generic and decouple from YouTube's ItagItem class.

    /**
     * Will try to download and parse the DASH manifest (using {@link StreamInfo#getDashMpdUrl()}),
     * adding items that are listed in the {@link ItagItem} class.
     * <p>
     * It has video, video only and audio streams and will only add to the list if it don't
     * find a similar stream in the respective lists (calling {@link Stream#equalStats}).
     * <p>
     * Info about dash MPD can be found here
     *
     * @param streamInfo where the parsed streams will be added
     * @see <a href="https://www.brendanlong.com/the-structure-of-an-mpeg-dash-mpd.html">www.brendanlog.com</a>
     */
    public static Result getStreams(final StreamInfo streamInfo)
            throws DashMpdParsingException, ReCaptchaException {
        final String dashDoc;
        final Downloader downloader = NewPipe.getDownloader();
        try {
            dashDoc = downloader.get(streamInfo.getDashMpdUrl()).responseBody();
        } catch (IOException e) {
            throw new DashMpdParsingException("Could not fetch DASH manifest: "
                    + streamInfo.getDashMpdUrl(), e);
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
                    final String url = representation.getElementsByTagName("BaseURL")
                            .item(0).getTextContent();
                    final ItagItem itag = ItagItem.getItag(Integer.parseInt(id));
                    final Element segmentationList = (Element) representation
                            .getElementsByTagName("SegmentList").item(0);

                    if (segmentationList == null) {
                        continue;
                    }

                    boolean isUrlRangeBased = false;
                    boolean isUrlSegmentsBased = false;
                    final Element initialization = (Element) segmentationList
                            .getElementsByTagName("Initialization").item(0);

                    if (initialization != null && initialization.hasAttributes()) {
                        final Node sourceURLNode = initialization.getAttributes()
                                .getNamedItem("sourceURL");
                        if (sourceURLNode != null) {
                            final String initializationSourceUrl = sourceURLNode.getNodeValue();

                            isUrlRangeBased = initializationSourceUrl != null &&
                                    initializationSourceUrl.startsWith("range/");
                            isUrlSegmentsBased = initializationSourceUrl != null &&
                                    initializationSourceUrl.startsWith("sq/");
                        }
                    }

                    final DeliveryFormat deliveryFormat;
                    if (isUrlRangeBased) {
                        deliveryFormat = DeliveryFormat.direct(url);
                    } else if (isUrlSegmentsBased) {
                        deliveryFormat = DeliveryFormat.manualDASH(url,
                                manualDashFromRepresentation(doc, representation));
                    } else {
                        continue;
                    }

                    final MediaFormat mediaFormat = MediaFormat.getFromMimeType(mimeType);

                    if (itag.itagType.equals(ItagItem.ItagType.AUDIO)) {
                        final AudioStream audioStream = new AudioStream(
                                deliveryFormat, mediaFormat, itag.avgBitrate);
                        if (!Stream.containSimilarStream(audioStream,
                                streamInfo.getAudioStreams())) {
                            audioStreams.add(audioStream);
                        }
                    } else {
                        boolean isVideoOnly = itag.itagType.equals(ItagItem.ItagType.VIDEO_ONLY);
                        final VideoStream videoStream = new VideoStream(
                                deliveryFormat, mediaFormat,
                                itag.resolutionString, isVideoOnly);

                        if (isVideoOnly) {
                            if (!Stream.containSimilarStream(videoStream,
                                    streamInfo.getVideoOnlyStreams())) {
                                videoOnlyStreams.add(videoStream);
                            }
                        } else if (!Stream.containSimilarStream(videoStream,
                                streamInfo.getVideoStreams())) {
                            videoStreams.add(videoStream);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            return new Result(videoStreams, videoOnlyStreams, audioStreams);
        } catch (Exception e) {
            throw new DashMpdParsingException("Could not parse Dash mpd", e);
        }
    }

    @NonNull
    private static String manualDashFromRepresentation(Document document, Element representation)
            throws TransformerException {

        final Element mpdElement = (Element) document.getElementsByTagName("MPD").item(0);

        // Clone element so we can freely modify it
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

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + nodeToString(newMpdRootElement);
    }

    private static String nodeToString(Node node) throws TransformerException {
        final StringWriter result = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node), new StreamResult(result));
        return result.toString();
    }
}
