package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.utils.DashMpdParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by Christian Schabesberger on 26.08.15.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * StreamInfo.java is part of NewPipe.
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

/**
 * Info object for opened videos, ie the video ready to play.
 */
@SuppressWarnings("WeakerAccess")
public class StreamInfo extends Info {

    /**
     * Get the stream type
     * @return the stream type
     */
    public StreamType getStreamType() {
        return stream_type;
    }

    /**
     * Get the thumbnail url
     * @return the thumbnail url as a string
     */
    public String getThumbnailUrl() {
       return thumbnail_url;
    }

    public String getUploadDate() {
        return upload_date;
    }

    /**
     * Get the duration in seconds
     * @return the duration in seconds
     */
    public long getDuration() {
        return duration;
    }

    public int getAgeLimit() {
        return age_limit;
    }

    public String getDescription() {
        return description;
    }

    public long getViewCount() {
        return view_count;
    }

    /**
     * Get the number of likes.
     * @return The number of likes or -1 if this information is not available
     */
    public long getLikeCount() {
        return like_count;
    }

    /**
     * Get the number of dislikes.
     * @return The number of likes or -1 if this information is not available
     */
    public long getDislikeCount() {
        return dislike_count;
    }

    public String getUploaderName() {
        return uploader_name;
    }

    public String getUploaderUrl() {
        return uploader_url;
    }

    public String getUploaderAvatarUrl() {
        return uploader_avatar_url;
    }

    public List<VideoStream> getVideoStreams() {
        return video_streams;
    }

    public List<AudioStream> getAudioStreams() {
        return audio_streams;
    }

    public List<VideoStream> getVideoOnlyStreams() {
        return video_only_streams;
    }

    public String getDashMpdUrl() {
        return dashMpdUrl;
    }

    public StreamInfoItem getNextVideo() {
        return next_video;
    }

    public List<InfoItem> getRelatedStreams() {
        return related_streams;
    }

    public long getStartPosition() {
        return start_position;
    }

    public static class StreamExtractException extends ExtractionException {
        StreamExtractException(String message) {
            super(message);
        }
    }

    public StreamInfo() {
    }

    public static StreamInfo getInfo(String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static StreamInfo getInfo(ServiceList serviceItem, String url) throws IOException, ExtractionException {
        return getInfo(serviceItem.getService(), url);
    }

    public static StreamInfo getInfo(StreamingService service, String url) throws IOException, ExtractionException {
        return getInfo(service.getStreamExtractor(url));
    }

    /**
     * Fills out the video info fields which are common to all services.
     * Probably needs to be overridden by subclasses
     */
    public static StreamInfo getInfo(StreamExtractor extractor) throws ExtractionException {
        StreamInfo streamInfo = new StreamInfo();

        try {
            streamInfo = extractImportantData(streamInfo, extractor);
            streamInfo = extractStreams(streamInfo, extractor);
            streamInfo = extractOptionalData(streamInfo, extractor);
        } catch (ExtractionException e) {
            // Currently YouTube does not distinguish between age restricted videos and videos blocked
            // by country.  This means that during the initialisation of the extractor, the extractor
            // will assume that a video is age restricted while in reality it it blocked by country.
            //
            // We will now detect whether the video is blocked by country or not.
            String errorMsg = extractor.getErrorMessage();

            if (errorMsg != null) {
                throw new ContentNotAvailableException(errorMsg);
            } else {
                throw e;
            }
        }

        return streamInfo;
    }

    private static StreamInfo extractImportantData(StreamInfo streamInfo, StreamExtractor extractor) throws ExtractionException {
        /* ---- important data, without the video can't be displayed goes here: ---- */
        // if one of these is not available an exception is meant to be thrown directly into the frontend.

        streamInfo.service_id = extractor.getServiceId();
        streamInfo.url = extractor.getCleanUrl();
        streamInfo.stream_type = extractor.getStreamType();
        streamInfo.id = extractor.getId();
        streamInfo.name = extractor.getName();
        streamInfo.age_limit = extractor.getAgeLimit();

        if ((streamInfo.stream_type == StreamType.NONE)
                || (streamInfo.url == null || streamInfo.url.isEmpty())
                || (streamInfo.id == null || streamInfo.id.isEmpty())
                || (streamInfo.name == null /* streamInfo.title can be empty of course */)
                || (streamInfo.age_limit == -1)) {
            throw new ExtractionException("Some important stream information was not given.");
        }

        return streamInfo;
    }

    private static StreamInfo extractStreams(StreamInfo streamInfo, StreamExtractor extractor) throws ExtractionException {
        /* ---- stream extraction goes here ---- */
        // At least one type of stream has to be available,
        // otherwise an exception will be thrown directly into the frontend.

        try {
            streamInfo.dashMpdUrl = extractor.getDashMpdUrl();
        } catch (Exception e) {
            streamInfo.addException(new ExtractionException("Couldn't get Dash manifest", e));
        }

        /*  Load and extract audio */
        try {
            streamInfo.audio_streams = extractor.getAudioStreams();
        } catch (Exception e) {
            streamInfo.addException(new ExtractionException("Couldn't get audio streams", e));
        }
        /* Extract video stream url*/
        try {
            streamInfo.video_streams = extractor.getVideoStreams();
        } catch (Exception e) {
            streamInfo.addException(new ExtractionException("Couldn't get video streams", e));
        }
        /* Extract video only stream url*/
        try {
            streamInfo.video_only_streams = extractor.getVideoOnlyStreams();
        } catch (Exception e) {
            streamInfo.addException(new ExtractionException("Couldn't get video only streams", e));
        }

        // Lists can be null if a exception was thrown during extraction
        if (streamInfo.video_streams == null) streamInfo.video_streams = new ArrayList<>();
        if (streamInfo.video_only_streams == null) streamInfo.video_only_streams = new ArrayList<>();
        if (streamInfo.audio_streams == null) streamInfo.audio_streams = new ArrayList<>();

        Exception dashMpdError = null;
        if (streamInfo.dashMpdUrl != null && !streamInfo.dashMpdUrl.isEmpty()) {
            try {
                DashMpdParser.getStreams(streamInfo);
            } catch (Exception e) {
                // Sometimes we receive 403 (forbidden) error when trying to download the manifest (similar to what happens with youtube-dl),
                // just skip the exception (but store it somewhere), as we later check if we have streams anyway.
                dashMpdError = e;
            }
        }

        // Either audio or video has to be available, otherwise we didn't get a stream (since videoOnly are optional, they don't count).
        if ((streamInfo.video_streams == null || streamInfo.video_streams.isEmpty())
                && (streamInfo.audio_streams == null || streamInfo.audio_streams.isEmpty())) {

            if (dashMpdError != null) {
                // If we don't have any video or audio and the dashMpd 'errored', add it to the error list
                // (it's optional and it don't get added automatically, but it's good to have some additional error context)
                streamInfo.addException(dashMpdError);
            }

            throw new StreamExtractException("Could not get any stream. See error variable to get further details.");
        }

        return streamInfo;
    }

    private static StreamInfo extractOptionalData(StreamInfo streamInfo, StreamExtractor extractor) {
        /*  ---- optional data goes here: ---- */
        // If one of these fails, the frontend needs to handle that they are not available.
        // Exceptions are therefore not thrown into the frontend, but stored into the error List,
        // so the frontend can afterwards check where errors happened.

        try {
            streamInfo.thumbnail_url = extractor.getThumbnailUrl();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.duration = extractor.getLength();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.uploader_name = extractor.getUploaderName();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.uploader_url = extractor.getUploaderUrl();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.description = extractor.getDescription();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.view_count = extractor.getViewCount();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.upload_date = extractor.getUploadDate();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.uploader_avatar_url = extractor.getUploaderAvatarUrl();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.start_position = extractor.getTimeStamp();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.like_count = extractor.getLikeCount();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.dislike_count = extractor.getDislikeCount();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.next_video = extractor.getNextVideo();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            StreamInfoItemCollector c = extractor.getRelatedVideos();
            streamInfo.related_streams = c.getItemList();
            streamInfo.errors.addAll(c.getErrors());
        } catch (Exception e) {
            streamInfo.addException(e);
        }

        if (streamInfo.related_streams == null) streamInfo.related_streams = new ArrayList<>();

        return streamInfo;
    }

    public void addException(Exception e) {
        errors.add(e);
    }

    public StreamType stream_type;
    public String thumbnail_url;
    public String upload_date;
    public long duration = -1;
    public int age_limit = -1;
    public String description;

    public long view_count = -1;
    public long like_count = -1;
    public long dislike_count = -1;

    public String uploader_name;
    public String uploader_url;
    public String uploader_avatar_url;

    public List<VideoStream> video_streams;
    public List<AudioStream> audio_streams;
    public List<VideoStream> video_only_streams;
    // video streams provided by the dash mpd do not need to be provided as VideoStream.
    // Later on this will also apply to audio streams. Since dash mpd is standarized,
    // crawling such a file is not service dependent. Therefore getting audio only streams by yust
    // providing the dash mpd file will be possible in the future.
    public String dashMpdUrl;

    public StreamInfoItem next_video;
    public List<InfoItem> related_streams;
    //in seconds. some metadata is not passed using a StreamInfo object!
    public long start_position = 0;
}
