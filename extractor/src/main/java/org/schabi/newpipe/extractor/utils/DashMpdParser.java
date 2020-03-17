package org.schabi.newpipe.extractor.utils;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public static class ParserResult {
        private final List<VideoStream> videoStreams;
        private final List<AudioStream> audioStreams;
        private final List<VideoStream> videoOnlyStreams;

        private final List<VideoStream> segmentedVideoStreams;
        private final List<AudioStream> segmentedAudioStreams;
        private final List<VideoStream> segmentedVideoOnlyStreams;


        public ParserResult(List<VideoStream> videoStreams,
                            List<AudioStream> audioStreams,
                            List<VideoStream> videoOnlyStreams,
                            List<VideoStream> segmentedVideoStreams,
                            List<AudioStream> segmentedAudioStreams,
                            List<VideoStream> segmentedVideoOnlyStreams) {
            this.videoStreams = videoStreams;
            this.audioStreams = audioStreams;
            this.videoOnlyStreams = videoOnlyStreams;
            this.segmentedVideoStreams = segmentedVideoStreams;
            this.segmentedAudioStreams = segmentedAudioStreams;
            this.segmentedVideoOnlyStreams = segmentedVideoOnlyStreams;
        }

        public List<VideoStream> getVideoStreams() {
            return videoStreams;
        }

        public List<AudioStream> getAudioStreams() {
            return audioStreams;
        }

        public List<VideoStream> getVideoOnlyStreams() {
            return videoOnlyStreams;
        }

        public List<VideoStream> getSegmentedVideoStreams() {
            return segmentedVideoStreams;
        }

        public List<AudioStream> getSegmentedAudioStreams() {
            return segmentedAudioStreams;
        }

        public List<VideoStream> getSegmentedVideoOnlyStreams() {
            return segmentedVideoOnlyStreams;
        }
    }

    /**
     * Will try to download (using {@link StreamInfo#getDashMpdUrl()}) and parse the dash manifest,
     * then it will search for any stream that the ItagItem has (by the id).
     * <p>
     * It has video, video only and audio streams and will only add to the list if it don't
     * find a similar stream in the respective lists (calling {@link Stream#equalStats}).
     * <p>
     * Info about dash MPD can be found here
     *
     * @param streamInfo where the parsed streams will be added
     * @see <a href="https://www.brendanlong.com/the-structure-of-an-mpeg-dash-mpd.html">www.brendanlog.com</a>
     */
    public static ParserResult getStreams(final StreamInfo streamInfo)
            throws DashMpdParsingException, ReCaptchaException {
        String dashDoc;
        Downloader downloader = NewPipe.getDownloader();
        try {
            dashDoc = downloader.get(streamInfo.getDashMpdUrl()).responseBody();
        } catch (IOException ioe) {
            throw new DashMpdParsingException("Could not get dash mpd: " + streamInfo.getDashMpdUrl(), ioe);
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

            final List<VideoStream> segmentedVideoStreams = new ArrayList<>();
            final List<AudioStream> segmentedAudioStreams = new ArrayList<>();
            final List<VideoStream> segmentedVideoOnlyStreams = new ArrayList<>();

            for (int i = 0; i < representationList.getLength(); i++) {
                final Element representation = (Element) representationList.item(i);
                try {
                    final String mimeType = ((Element) representation.getParentNode()).getAttribute("mimeType");
                    final String id = representation.getAttribute("id");
                    final String url = representation.getElementsByTagName("BaseURL").item(0).getTextContent();
                    final ItagItem itag = ItagItem.getItag(Integer.parseInt(id));
                    final Node segmentationList = representation.getElementsByTagName("SegmentList").item(0);

                    // if SegmentList is not null this means that BaseUrl is not representing the url to the stream.
                    // instead we need to add the "media=" value from the <SegementURL/> tags inside the <SegmentList/>
                    // tag in order to get a full working url. However each of these is just pointing to a part of the
                    // video, so we can not return a URL with a working stream here.
                    // Instead of putting those streams into the list of regular stream urls wie put them in a
                    // for example "segmentedVideoStreams" list.
                    if (itag != null) {
                        final MediaFormat mediaFormat = MediaFormat.getFromMimeType(mimeType);

                        if (itag.itagType.equals(ItagItem.ItagType.AUDIO)) {
                            if (segmentationList == null) {
                                final AudioStream audioStream = new AudioStream(url, mediaFormat, itag.avgBitrate);
                                if (!Stream.containSimilarStream(audioStream, streamInfo.getAudioStreams())) {
                                    audioStreams.add(audioStream);
                                }
                            } else {
                                segmentedAudioStreams.add(
                                        new AudioStream(id, mediaFormat, itag.avgBitrate));
                            }
                        } else {
                            boolean isVideoOnly = itag.itagType.equals(ItagItem.ItagType.VIDEO_ONLY);

                            if (segmentationList == null) {
                                final VideoStream videoStream = new VideoStream(url,
                                        mediaFormat,
                                        itag.resolutionString,
                                        isVideoOnly);

                                if (isVideoOnly) {
                                    if (!Stream.containSimilarStream(videoStream, streamInfo.getVideoOnlyStreams())) {
                                        videoOnlyStreams.add(videoStream);
                                    }
                                } else if (!Stream.containSimilarStream(videoStream, streamInfo.getVideoStreams())) {
                                    videoStreams.add(videoStream);
                                }
                            } else {
                                final VideoStream videoStream = new VideoStream(id,
                                        mediaFormat,
                                        itag.resolutionString,
                                        isVideoOnly);

                                if (isVideoOnly) {
                                    segmentedVideoOnlyStreams.add(videoStream);
                                } else {
                                    segmentedVideoStreams.add(videoStream);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            return new ParserResult(
                    videoStreams,
                    audioStreams,
                    videoOnlyStreams,
                    segmentedVideoStreams,
                    segmentedAudioStreams,
                    segmentedVideoOnlyStreams);
        } catch (Exception e) {
            throw new DashMpdParsingException("Could not parse Dash mpd", e);
        }
    }
}
