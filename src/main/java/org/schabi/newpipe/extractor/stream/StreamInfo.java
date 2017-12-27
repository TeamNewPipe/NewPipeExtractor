package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.utils.DashMpdParser;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;
import java.util.Collections;
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

    public StreamInfo(int serviceId, String url, StreamType streamType, String id, String name, int ageLimit) {
        super(serviceId, id, url, name);
        this.stream_type = streamType;
        this.age_limit = ageLimit;
    }

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

    public void setStreamType(StreamType stream_type) {
        this.stream_type = stream_type;
    }

    public void setThumbnailUrl(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

    public void setUploadDate(String upload_date) {
        this.upload_date = upload_date;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setAgeLimit(int age_limit) {
        this.age_limit = age_limit;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setViewCount(long view_count) {
        this.view_count = view_count;
    }

    public void setLikeCount(long like_count) {
        this.like_count = like_count;
    }

    public void setDislikeCount(long dislike_count) {
        this.dislike_count = dislike_count;
    }

    public void setUploaderName(String uploader_name) {
        this.uploader_name = uploader_name;
    }

    public void setUploaderUrl(String uploader_url) {
        this.uploader_url = uploader_url;
    }

    public void setUploaderAvatarUrl(String uploader_avatar_url) {
        this.uploader_avatar_url = uploader_avatar_url;
    }

    public void setVideoStreams(List<VideoStream> video_streams) {
        this.video_streams = video_streams;
    }

    public void setAudioStreams(List<AudioStream> audio_streams) {
        this.audio_streams = audio_streams;
    }

    public void setVideoOnlyStreams(List<VideoStream> video_only_streams) {
        this.video_only_streams = video_only_streams;
    }

    public void setDashMpdUrl(String dashMpdUrl) {
        this.dashMpdUrl = dashMpdUrl;
    }

    public void setNextVideo(StreamInfoItem next_video) {
        this.next_video = next_video;
    }

    public void setRelatedStreams(List<InfoItem> related_streams) {
        this.related_streams = related_streams;
    }

    public void setStartPosition(long start_position) {
        this.start_position = start_position;
    }

    public static class StreamExtractException extends ExtractionException {
        StreamExtractException(String message) {
            super(message);
        }
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
    private static StreamInfo getInfo(StreamExtractor extractor) throws ExtractionException, IOException {
        extractor.fetchPage();
        StreamInfo streamInfo;
        try {
            streamInfo = extractImportantData(extractor);
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

    private static StreamInfo extractImportantData(StreamExtractor extractor) throws ExtractionException {
        /* ---- important data, without the video can't be displayed goes here: ---- */
        // if one of these is not available an exception is meant to be thrown directly into the frontend.

        int serviceId = extractor.getServiceId();
        String url = extractor.getCleanUrl();
        StreamType streamType = extractor.getStreamType();
        String id = extractor.getId();
        String name = extractor.getName();
        int ageLimit = extractor.getAgeLimit();

        if ((streamType == StreamType.NONE)
                || (url == null || url.isEmpty())
                || (id == null || id.isEmpty())
                || (name == null /* streamInfo.title can be empty of course */)
                || (ageLimit == -1)) {
            throw new ExtractionException("Some important stream information was not given.");
        }

        return new StreamInfo(serviceId, url, streamType, id, name, ageLimit);
    }

    private static StreamInfo extractStreams(StreamInfo streamInfo, StreamExtractor extractor) throws ExtractionException {
        /* ---- stream extraction goes here ---- */
        // At least one type of stream has to be available,
        // otherwise an exception will be thrown directly into the frontend.

        try {
            streamInfo.setDashMpdUrl(extractor.getDashMpdUrl());
        } catch (Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get Dash manifest", e));
        }

        /*  Load and extract audio */
        try {
            streamInfo.setAudioStreams(extractor.getAudioStreams());
        } catch (Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get audio streams", e));
        }
        /* Extract video stream url*/
        try {
            streamInfo.setVideoStreams(extractor.getVideoStreams());
        } catch (Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get video streams", e));
        }
        /* Extract video only stream url*/
        try {
            streamInfo.setVideoOnlyStreams(extractor.getVideoOnlyStreams());
        } catch (Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get video only streams", e));
        }

        // Lists can be null if a exception was thrown during extraction
        if (streamInfo.getVideoStreams() == null) streamInfo.setVideoStreams(Collections.<VideoStream>emptyList());
        if (streamInfo.getVideoOnlyStreams()== null) streamInfo.setVideoOnlyStreams(Collections.<VideoStream>emptyList());
        if (streamInfo.getAudioStreams() == null) streamInfo.setAudioStreams(Collections.<AudioStream>emptyList());

        Exception dashMpdError = null;
        if (streamInfo.getDashMpdUrl() != null && !streamInfo.getDashMpdUrl().isEmpty()) {
            try {
                DashMpdParser.getStreams(streamInfo);
            } catch (Exception e) {
                // Sometimes we receive 403 (forbidden) error when trying to download the manifest (similar to what happens with youtube-dl),
                // just skip the exception (but store it somewhere), as we later check if we have streams anyway.
                dashMpdError = e;
            }
        }

        // Either audio or video has to be available, otherwise we didn't get a stream (since videoOnly are optional, they don't count).
        if ((streamInfo.video_streams.isEmpty())
                && (streamInfo.audio_streams.isEmpty())) {

            if (dashMpdError != null) {
                // If we don't have any video or audio and the dashMpd 'errored', add it to the error list
                // (it's optional and it don't get added automatically, but it's good to have some additional error context)
                streamInfo.addError(dashMpdError);
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
            streamInfo.setThumbnailUrl(extractor.getThumbnailUrl());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setDuration(extractor.getLength());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setUploaderName(extractor.getUploaderName());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setUploaderUrl(extractor.getUploaderUrl());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setDescription(extractor.getDescription());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setViewCount(extractor.getViewCount());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setUploadDate(extractor.getUploadDate());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setUploaderAvatarUrl(extractor.getUploaderAvatarUrl());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setStartPosition(extractor.getTimeStamp());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setLikeCount(extractor.getLikeCount());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setDislikeCount(extractor.getDislikeCount());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setNextVideo(extractor.getNextVideo());
        } catch (Exception e) {
            streamInfo.addError(e);
        }

        streamInfo.setRelatedStreams(ExtractorHelper.getRelatedVideosOrLogError(streamInfo, extractor));
        return streamInfo;
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
