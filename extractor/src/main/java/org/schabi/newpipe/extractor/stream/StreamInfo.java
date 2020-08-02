package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.utils.DashMpdParser;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

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
public class StreamInfo extends Info {

    public static class StreamExtractException extends ExtractionException {
        StreamExtractException(String message) {
            super(message);
        }
    }

    public StreamInfo(int serviceId, String url, String originalUrl, StreamType streamType, String id, String name,
                      int ageLimit) {
        super(serviceId, id, url, originalUrl, name);
        this.streamType = streamType;
        this.ageLimit = ageLimit;
    }

    public static StreamInfo getInfo(String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static StreamInfo getInfo(StreamingService service, String url) throws IOException, ExtractionException {
        return getInfo(service.getStreamExtractor(url));
    }

    private static StreamInfo getInfo(StreamExtractor extractor) throws ExtractionException, IOException {
        extractor.fetchPage();
        StreamInfo streamInfo;
        try {
            streamInfo = extractImportantData(extractor);
            streamInfo = extractStreams(streamInfo, extractor);
            streamInfo = extractOptionalData(streamInfo, extractor);
        } catch (ExtractionException e) {
            // Currently YouTube does not distinguish between age restricted videos and
            // videos blocked
            // by country. This means that during the initialisation of the extractor, the
            // extractor
            // will assume that a video is age restricted while in reality it it blocked by
            // country.
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
        // if one of these is not available an exception is meant to be thrown directly
        // into the frontend.

        int serviceId = extractor.getServiceId();
        String url = extractor.getUrl();
        String originalUrl = extractor.getOriginalUrl();
        StreamType streamType = extractor.getStreamType();
        String id = extractor.getId();
        String name = extractor.getName();
        int ageLimit = extractor.getAgeLimit();

        if ((streamType == StreamType.NONE) || isNullOrEmpty(url) || (isNullOrEmpty(id))
                || (name == null /* streamInfo.title can be empty of course */) || (ageLimit == -1)) {
            throw new ExtractionException("Some important stream information was not given.");
        }

        return new StreamInfo(serviceId, url, originalUrl, streamType, id, name, ageLimit);
    }

    private static StreamInfo extractStreams(StreamInfo streamInfo, StreamExtractor extractor)
            throws ExtractionException {
        /* ---- stream extraction goes here ---- */
        // At least one type of stream has to be available,
        // otherwise an exception will be thrown directly into the frontend.

        try {
            streamInfo.setDashMpdUrl(extractor.getDashMpdUrl());
        } catch (Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get Dash manifest", e));
        }

        try {
            streamInfo.setHlsUrl(extractor.getHlsUrl());
        } catch (Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get HLS manifest", e));
        }

        /* Load and extract audio */
        try {
            streamInfo.setAudioStreams(extractor.getAudioStreams());
        } catch (ContentNotSupportedException e) {
            throw e;
        } catch (Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get audio streams", e));
        }
        /* Extract video stream url */
        try {
            streamInfo.setVideoStreams(extractor.getVideoStreams());
        } catch (Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get video streams", e));
        }
        /* Extract video only stream url */
        try {
            streamInfo.setVideoOnlyStreams(extractor.getVideoOnlyStreams());
        } catch (Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get video only streams", e));
        }

        // Lists can be null if a exception was thrown during extraction
        if (streamInfo.getVideoStreams() == null)
            streamInfo.setVideoStreams(new ArrayList<VideoStream>());
        if (streamInfo.getVideoOnlyStreams() == null)
            streamInfo.setVideoOnlyStreams(new ArrayList<VideoStream>());
        if (streamInfo.getAudioStreams() == null)
            streamInfo.setAudioStreams(new ArrayList<AudioStream>());

        Exception dashMpdError = null;
        if (!isNullOrEmpty(streamInfo.getDashMpdUrl())) {
            try {
                DashMpdParser.ParserResult result = DashMpdParser.getStreams(streamInfo);
                streamInfo.getVideoOnlyStreams().addAll(result.getVideoOnlyStreams());
                streamInfo.getAudioStreams().addAll(result.getAudioStreams());
                streamInfo.getVideoStreams().addAll(result.getVideoStreams());
                streamInfo.segmentedVideoOnlyStreams = result.getSegmentedVideoOnlyStreams();
                streamInfo.segmentedAudioStreams = result.getSegmentedAudioStreams();
                streamInfo.segmentedVideoStreams = result.getSegmentedVideoStreams();
            } catch (Exception e) {
                // Sometimes we receive 403 (forbidden) error when trying to download the
                // manifest (similar to what happens with youtube-dl),
                // just skip the exception (but store it somewhere), as we later check if we
                // have streams anyway.
                dashMpdError = e;
            }
        }

        // Either audio or video has to be available, otherwise we didn't get a stream
        // (since videoOnly are optional, they don't count).
        if ((streamInfo.videoStreams.isEmpty()) && (streamInfo.audioStreams.isEmpty())) {

            if (dashMpdError != null) {
                // If we don't have any video or audio and the dashMpd 'errored', add it to the
                // error list
                // (it's optional and it don't get added automatically, but it's good to have
                // some additional error context)
                streamInfo.addError(dashMpdError);
            }

            throw new StreamExtractException("Could not get any stream. See error variable to get further details.");
        }

        return streamInfo;
    }

    private static StreamInfo extractOptionalData(StreamInfo streamInfo, StreamExtractor extractor) {
        /* ---- optional data goes here: ---- */
        // If one of these fails, the frontend needs to handle that they are not
        // available.
        // Exceptions are therefore not thrown into the frontend, but stored into the
        // error List,
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
            streamInfo.setUploaderAvatarUrl(extractor.getUploaderAvatarUrl());
        } catch (Exception e) {
            streamInfo.addError(e);
        }

        try {
            streamInfo.setSubChannelName(extractor.getSubChannelName());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setSubChannelUrl(extractor.getSubChannelUrl());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setSubChannelAvatarUrl(extractor.getSubChannelAvatarUrl());
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
            streamInfo.setTextualUploadDate(extractor.getTextualUploadDate());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setUploadDate(extractor.getUploadDate());
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
            streamInfo.setSubtitles(extractor.getSubtitlesDefault());
        } catch (Exception e) {
            streamInfo.addError(e);
        }

        //additional info
        try {
            streamInfo.setHost(extractor.getHost());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setPrivacy(extractor.getPrivacy());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setCategory(extractor.getCategory());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setLicence(extractor.getLicence());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setLanguageInfo(extractor.getLanguageInfo());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setTags(extractor.getTags());
        } catch (Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setSupportInfo(extractor.getSupportInfo());
        } catch (Exception e) {
            streamInfo.addError(e);
        }

        streamInfo.setRelatedStreams(ExtractorHelper.getRelatedVideosOrLogError(streamInfo, extractor));

        return streamInfo;
    }

    private StreamType streamType;
    private String thumbnailUrl = "";
    private String textualUploadDate;
    private DateWrapper uploadDate;
    private long duration = -1;
    private int ageLimit = -1;
    private Description description;

    private long viewCount = -1;
    private long likeCount = -1;
    private long dislikeCount = -1;

    private String uploaderName = "";
    private String uploaderUrl = "";
    private String uploaderAvatarUrl = "";

    private String subChannelName = "";
    private String subChannelUrl = "";
    private String subChannelAvatarUrl = "";

    private List<VideoStream> videoStreams = new ArrayList<>();
    private List<AudioStream> audioStreams = new ArrayList<>();
    private List<VideoStream> videoOnlyStreams = new ArrayList<>();

    private String dashMpdUrl = "";
    private List<VideoStream> segmentedVideoStreams = new ArrayList<>();
    private List<AudioStream> segmentedAudioStreams = new ArrayList<>();
    private List<VideoStream> segmentedVideoOnlyStreams = new ArrayList<>();


    private String hlsUrl = "";
    private List<InfoItem> relatedStreams = new ArrayList<>();

    private long startPosition = 0;
    private List<SubtitlesStream> subtitles = new ArrayList<>();

    private String host = "";
    private String privacy = "";
    private String category = "";
    private String licence = "";
    private String support = "";
    private Locale language = null;
    private List<String> tags = new ArrayList<>();

    /**
     * Get the stream type
     *
     * @return the stream type
     */
    public StreamType getStreamType() {
        return streamType;
    }

    public void setStreamType(StreamType streamType) {
        this.streamType = streamType;
    }

    /**
     * Get the thumbnail url
     *
     * @return the thumbnail url as a string
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTextualUploadDate() {
        return textualUploadDate;
    }

    public void setTextualUploadDate(String textualUploadDate) {
        this.textualUploadDate = textualUploadDate;
    }

    public DateWrapper getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(DateWrapper uploadDate) {
        this.uploadDate = uploadDate;
    }

    /**
     * Get the duration in seconds
     *
     * @return the duration in seconds
     */
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getAgeLimit() {
        return ageLimit;
    }

    public void setAgeLimit(int ageLimit) {
        this.ageLimit = ageLimit;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    /**
     * Get the number of likes.
     *
     * @return The number of likes or -1 if this information is not available
     */
    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    /**
     * Get the number of dislikes.
     *
     * @return The number of likes or -1 if this information is not available
     */
    public long getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(long dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderUrl() {
        return uploaderUrl;
    }

    public void setUploaderUrl(String uploaderUrl) {
        this.uploaderUrl = uploaderUrl;
    }

    public String getUploaderAvatarUrl() {
        return uploaderAvatarUrl;
    }

    public void setUploaderAvatarUrl(String uploaderAvatarUrl) {
        this.uploaderAvatarUrl = uploaderAvatarUrl;
    }

    public String getSubChannelName() {
        return subChannelName;
    }

    public void setSubChannelName(String subChannelName) {
        this.subChannelName = subChannelName;
    }

    public String getSubChannelUrl() {
        return subChannelUrl;
    }

    public void setSubChannelUrl(String subChannelUrl) {
        this.subChannelUrl = subChannelUrl;
    }

    public String getSubChannelAvatarUrl() {
        return subChannelAvatarUrl;
    }

    public void setSubChannelAvatarUrl(String subChannelAvatarUrl) {
        this.subChannelAvatarUrl = subChannelAvatarUrl;
    }

    public List<VideoStream> getVideoStreams() {
        return videoStreams;
    }

    public void setVideoStreams(List<VideoStream> videoStreams) {
        this.videoStreams = videoStreams;
    }

    public List<AudioStream> getAudioStreams() {
        return audioStreams;
    }

    public void setAudioStreams(List<AudioStream> audioStreams) {
        this.audioStreams = audioStreams;
    }

    public List<VideoStream> getVideoOnlyStreams() {
        return videoOnlyStreams;
    }

    public void setVideoOnlyStreams(List<VideoStream> videoOnlyStreams) {
        this.videoOnlyStreams = videoOnlyStreams;
    }

    public String getDashMpdUrl() {
        return dashMpdUrl;
    }

    public void setDashMpdUrl(String dashMpdUrl) {
        this.dashMpdUrl = dashMpdUrl;
    }

    public List<VideoStream> getSegmentedVideoStreams() {
        return segmentedVideoStreams;
    }

    public void setSegmentedVideoStreams(List<VideoStream> segmentedVideoStreams) {
        this.segmentedVideoStreams = segmentedVideoStreams;
    }

    public List<AudioStream> getSegmentedAudioStreams() {
        return segmentedAudioStreams;
    }

    public void setSegmentedAudioStreams(List<AudioStream> segmentedAudioStreams) {
        this.segmentedAudioStreams = segmentedAudioStreams;
    }

    public List<VideoStream> getSegmentedVideoOnlyStreams() {
        return segmentedVideoOnlyStreams;
    }

    public void setSegmentedVideoOnlyStreams(List<VideoStream> segmentedVideoOnlyStreams) {
        this.segmentedVideoOnlyStreams = segmentedVideoOnlyStreams;
    }

    public String getHlsUrl() {
        return hlsUrl;
    }

    public void setHlsUrl(String hlsUrl) {
        this.hlsUrl = hlsUrl;
    }

    public List<InfoItem> getRelatedStreams() {
        return relatedStreams;
    }

    public void setRelatedStreams(List<InfoItem> relatedStreams) {
        this.relatedStreams = relatedStreams;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public List<SubtitlesStream> getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(List<SubtitlesStream> subtitles) {
        this.subtitles = subtitles;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String str) {
        this.host = str;
    }

    public String getPrivacy() {
        return this.privacy;
    }

    public void setPrivacy(String str) {
        this.privacy = str;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String cat) {
        this.category = cat;
    }

    public String getLicence() {
        return this.licence;
    }

    public void setLicence(String str) {
        this.licence = str;
    }

    public Locale getLanguageInfo() {
        return this.language;
    }

    public void setLanguageInfo(Locale lang) {
        this.language = lang;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setSupportInfo(String support) {
        this.support = support;
    }

    public String getSupportInfo() {
        return this.support;
    }
}
