package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DashMpdParser;
import org.schabi.newpipe.extractor.stream.DeliveryFormat;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamInfo;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import edu.umd.cs.findbugs.annotations.NonNull;

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

public class YoutubeDashMpdParser extends DashMpdParser {

    public static YoutubeDashMpdParser INSTANCE = new YoutubeDashMpdParser();

    private YoutubeDashMpdParser() {
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
     * @param manifestUrl manifest url of dash stream
     * @see <a href="https://www.brendanlong.com/the-structure-of-an-mpeg-dash-mpd.html">www.brendanlog.com</a>
     */
    public Result getStreams(final String manifestUrl)
            throws DashMpdParsingException, ReCaptchaException {
        final String dashDoc;
        final Downloader downloader = NewPipe.getDownloader();
        try {
            dashDoc = downloader.get(manifestUrl).responseBody();
        } catch (IOException e) {
            throw new DashMpdParsingException("Could not fetch DASH manifest: "
                    + manifestUrl, e);
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
                            audioStreams.add(audioStream);
                    } else {
                        boolean isVideoOnly = itag.itagType.equals(ItagItem.ItagType.VIDEO_ONLY);
                        final VideoStream videoStream = new VideoStream(
                                deliveryFormat, mediaFormat,
                                itag.resolutionString, isVideoOnly);

                        if (isVideoOnly) {
                                videoOnlyStreams.add(videoStream);
                        } else {
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


}
