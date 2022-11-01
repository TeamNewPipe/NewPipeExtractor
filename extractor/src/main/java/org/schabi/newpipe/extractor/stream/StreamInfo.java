package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/*
 * Created by Christian Schabesberger on 26.08.15.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * StreamInfo.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Info object for opened contents, i.e. the content ready to play.
 */
public class StreamInfo extends Info {

    public static class StreamExtractException extends ExtractionException {
        StreamExtractException(final String message) {
            super(message);
        }
    }

    public StreamInfo(final int serviceId,
                      final String url,
                      final String originalUrl,
                      final StreamType streamType,
                      final String id,
                      final String name,
                      final int ageLimit) {
        super(serviceId, id, url, originalUrl, name);
        this.streamType = streamType;
        this.ageLimit = ageLimit;
    }

    public static StreamInfo getInfo(final String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static StreamInfo getInfo(@Nonnull final StreamingService service,
                                     final String url) throws IOException, ExtractionException {
        return getInfo(service.getStreamExtractor(url));
    }

    public static StreamInfo getInfo(@Nonnull final StreamExtractor extractor)
            throws ExtractionException, IOException {
        extractor.fetchPage();
        final StreamInfo streamInfo;
        try {
            streamInfo = extractImportantData(extractor);
            extractStreams(streamInfo, extractor);
            extractOptionalData(streamInfo, extractor);
            return streamInfo;

        } catch (final ExtractionException e) {
            // Currently, YouTube does not distinguish between age restricted videos and videos
            // blocked by country. This means that during the initialisation of the extractor, the
            // extractor will assume that a video is age restricted while in reality it is blocked
            // by country.
            //
            // We will now detect whether the video is blocked by country or not.

            final String errorMessage = extractor.getErrorMessage();
            if (isNullOrEmpty(errorMessage)) {
                throw e;
            } else {
                throw new ContentNotAvailableException(errorMessage, e);
            }
        }
    }

    @Nonnull
    private static StreamInfo extractImportantData(@Nonnull final StreamExtractor extractor)
            throws ExtractionException {
        // Important data, without it the content can't be displayed.
        // If one of these is not available, the frontend will receive an exception directly.

        final int serviceId = extractor.getServiceId();
        final String url = extractor.getUrl();
        final String originalUrl = extractor.getOriginalUrl();
        final StreamType streamType = extractor.getStreamType();
        final String id = extractor.getId();
        final String name = extractor.getName();
        final int ageLimit = extractor.getAgeLimit();

        // Suppress always-non-null warning as here we double-check it really is not null
        //noinspection ConstantConditions
        if (streamType == StreamType.NONE
                || isNullOrEmpty(url)
                || isNullOrEmpty(id)
                || name == null /* but it can be empty of course */
                || ageLimit == -1) {
            throw new ExtractionException("Some important stream information was not given.");
        }

        return new StreamInfo(extractor.getServiceId(), url, extractor.getOriginalUrl(),
                streamType, id, name, ageLimit);
    }


    private static void extractStreams(final StreamInfo streamInfo,
                                       final StreamExtractor extractor)
            throws ExtractionException {
        /* ---- Stream extraction goes here ---- */
        // At least one type of stream has to be available, otherwise an exception will be thrown
        // directly into the frontend.

        try {
            streamInfo.setDashMpdUrl(extractor.getDashMpdUrl());
        } catch (final Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get DASH manifest", e));
        }

        try {
            streamInfo.setHlsUrl(extractor.getHlsUrl());
        } catch (final Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get HLS manifest", e));
        }

        /* Load and extract audio */
        try {
            streamInfo.setAudioStreams(extractor.getAudioStreams());
        } catch (final ContentNotSupportedException e) {
            throw e;
        } catch (final Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get audio streams", e));
        }

        /* Extract video stream url */
        try {
            streamInfo.setVideoStreams(extractor.getVideoStreams());
        } catch (final Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get video streams", e));
        }

        /* Extract video only stream url */
        try {
            streamInfo.setVideoOnlyStreams(extractor.getVideoOnlyStreams());
        } catch (final Exception e) {
            streamInfo.addError(new ExtractionException("Couldn't get video only streams", e));
        }

        // Lists can be null if an exception was thrown during extraction
        if (streamInfo.getVideoStreams() == null) {
            streamInfo.setVideoStreams(Collections.emptyList());
        }
        if (streamInfo.getVideoOnlyStreams() == null) {
            streamInfo.setVideoOnlyStreams(Collections.emptyList());
        }
        if (streamInfo.getAudioStreams() == null) {
            streamInfo.setAudioStreams(Collections.emptyList());
        }

        // Either audio or video has to be available, otherwise we didn't get a stream (since
        // videoOnly are optional, they don't count).
        if ((streamInfo.videoStreams.isEmpty()) && (streamInfo.audioStreams.isEmpty())) {
            throw new StreamExtractException(
                    "Could not get any stream. See error variable to get further details.");
        }
    }

    @SuppressWarnings("MethodLength")
    private static void extractOptionalData(final StreamInfo streamInfo,
                                            final StreamExtractor extractor) {
        /* ---- Optional data goes here: ---- */
        // If one of these fails, the frontend needs to handle that they are not available.
        // Exceptions are therefore not thrown into the frontend, but stored into the error list,
        // so the frontend can afterwards check where errors happened.

        try {
            streamInfo.setThumbnailUrl(extractor.getThumbnailUrl());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setDuration(extractor.getLength());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setUploaderName(extractor.getUploaderName());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setUploaderUrl(extractor.getUploaderUrl());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setUploaderAvatarUrl(extractor.getUploaderAvatarUrl());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setUploaderVerified(extractor.isUploaderVerified());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setUploaderSubscriberCount(extractor.getUploaderSubscriberCount());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }

        try {
            streamInfo.setSubChannelName(extractor.getSubChannelName());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setSubChannelUrl(extractor.getSubChannelUrl());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setSubChannelAvatarUrl(extractor.getSubChannelAvatarUrl());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }

        try {
            streamInfo.setDescription(extractor.getDescription());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setViewCount(extractor.getViewCount());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setTextualUploadDate(extractor.getTextualUploadDate());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setUploadDate(extractor.getUploadDate());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setStartPosition(extractor.getTimeStamp());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setLikeCount(extractor.getLikeCount());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setDislikeCount(extractor.getDislikeCount());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setSubtitles(extractor.getSubtitlesDefault());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }

        // Additional info
        try {
            streamInfo.setHost(extractor.getHost());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setPrivacy(extractor.getPrivacy());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setCategory(extractor.getCategory());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setLicence(extractor.getLicence());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setLanguageInfo(extractor.getLanguageInfo());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setTags(extractor.getTags());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setSupportInfo(extractor.getSupportInfo());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setStreamSegments(extractor.getStreamSegments());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setMetaInfo(extractor.getMetaInfo());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setPreviewFrames(extractor.getFrames());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }
        try {
            streamInfo.setShortFormContent(extractor.isShortFormContent());
        } catch (final Exception e) {
            streamInfo.addError(e);
        }

        streamInfo.setRelatedItems(ExtractorHelper.getRelatedItemsOrLogError(streamInfo,
                extractor));
    }

    private StreamType streamType;
    private String thumbnailUrl = "";
    private String textualUploadDate;
    private DateWrapper uploadDate;
    private long duration = -1;
    private int ageLimit;
    private Description description;

    private long viewCount = -1;
    private long likeCount = -1;
    private long dislikeCount = -1;

    private String uploaderName = "";
    private String uploaderUrl = "";
    private String uploaderAvatarUrl = "";
    private boolean uploaderVerified = false;
    private long uploaderSubscriberCount = -1;

    private String subChannelName = "";
    private String subChannelUrl = "";
    private String subChannelAvatarUrl = "";

    private List<VideoStream> videoStreams = new ArrayList<>();
    private List<AudioStream> audioStreams = new ArrayList<>();
    private List<VideoStream> videoOnlyStreams = new ArrayList<>();

    private String dashMpdUrl = "";
    private String hlsUrl = "";
    private List<InfoItem> relatedItems = new ArrayList<>();

    private long startPosition = 0;
    private List<SubtitlesStream> subtitles = new ArrayList<>();

    private String host = "";
    private StreamExtractor.Privacy privacy;
    private String category = "";
    private String licence = "";
    private String supportInfo = "";
    private Locale language = null;
    private List<String> tags = new ArrayList<>();
    private List<StreamSegment> streamSegments = new ArrayList<>();
    private List<MetaInfo> metaInfo = new ArrayList<>();
    private boolean shortFormContent = false;

    /**
     * Preview frames, e.g. for the storyboard / seekbar thumbnail preview
     */
    private List<Frameset> previewFrames = Collections.emptyList();

    /**
     * Get the stream type
     *
     * @return the stream type
     */
    public StreamType getStreamType() {
        return streamType;
    }

    public void setStreamType(final StreamType streamType) {
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

    public void setThumbnailUrl(final String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTextualUploadDate() {
        return textualUploadDate;
    }

    public void setTextualUploadDate(final String textualUploadDate) {
        this.textualUploadDate = textualUploadDate;
    }

    public DateWrapper getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(final DateWrapper uploadDate) {
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

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    public int getAgeLimit() {
        return ageLimit;
    }

    public void setAgeLimit(final int ageLimit) {
        this.ageLimit = ageLimit;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(final Description description) {
        this.description = description;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(final long viewCount) {
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

    public void setLikeCount(final long likeCount) {
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

    public void setDislikeCount(final long dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(final String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderUrl() {
        return uploaderUrl;
    }

    public void setUploaderUrl(final String uploaderUrl) {
        this.uploaderUrl = uploaderUrl;
    }

    public String getUploaderAvatarUrl() {
        return uploaderAvatarUrl;
    }

    public void setUploaderAvatarUrl(final String uploaderAvatarUrl) {
        this.uploaderAvatarUrl = uploaderAvatarUrl;
    }

    public boolean isUploaderVerified() {
        return uploaderVerified;
    }

    public void setUploaderVerified(final boolean uploaderVerified) {
        this.uploaderVerified = uploaderVerified;
    }

    public long getUploaderSubscriberCount() {
        return uploaderSubscriberCount;
    }

    public void setUploaderSubscriberCount(final long uploaderSubscriberCount) {
        this.uploaderSubscriberCount = uploaderSubscriberCount;
    }

    public String getSubChannelName() {
        return subChannelName;
    }

    public void setSubChannelName(final String subChannelName) {
        this.subChannelName = subChannelName;
    }

    public String getSubChannelUrl() {
        return subChannelUrl;
    }

    public void setSubChannelUrl(final String subChannelUrl) {
        this.subChannelUrl = subChannelUrl;
    }

    public String getSubChannelAvatarUrl() {
        return subChannelAvatarUrl;
    }

    public void setSubChannelAvatarUrl(final String subChannelAvatarUrl) {
        this.subChannelAvatarUrl = subChannelAvatarUrl;
    }

    public List<VideoStream> getVideoStreams() {
        return videoStreams;
    }

    public void setVideoStreams(final List<VideoStream> videoStreams) {
        this.videoStreams = videoStreams;
    }

    public List<AudioStream> getAudioStreams() {
        return audioStreams;
    }

    public void setAudioStreams(final List<AudioStream> audioStreams) {
        this.audioStreams = audioStreams;
    }

    public List<VideoStream> getVideoOnlyStreams() {
        return videoOnlyStreams;
    }

    public void setVideoOnlyStreams(final List<VideoStream> videoOnlyStreams) {
        this.videoOnlyStreams = videoOnlyStreams;
    }

    public String getDashMpdUrl() {
        return dashMpdUrl;
    }

    public void setDashMpdUrl(final String dashMpdUrl) {
        this.dashMpdUrl = dashMpdUrl;
    }

    public String getHlsUrl() {
        return hlsUrl;
    }

    public void setHlsUrl(final String hlsUrl) {
        this.hlsUrl = hlsUrl;
    }

    public List<InfoItem> getRelatedItems() {
        return relatedItems;
    }

    /**
     * @deprecated Use {@link #getRelatedItems()}
     */
    @Deprecated
    public List<InfoItem> getRelatedStreams() {
        return getRelatedItems();
    }

    public void setRelatedItems(final List<InfoItem> relatedItems) {
        this.relatedItems = relatedItems;
    }

    /**
     * @deprecated Use {@link #setRelatedItems(List)}
     */
    @Deprecated
    public void setRelatedStreams(final List<InfoItem> relatedItemsToSet) {
        setRelatedItems(relatedItemsToSet);
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(final long startPosition) {
        this.startPosition = startPosition;
    }

    public List<SubtitlesStream> getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(final List<SubtitlesStream> subtitles) {
        this.subtitles = subtitles;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public StreamExtractor.Privacy getPrivacy() {
        return this.privacy;
    }

    public void setPrivacy(final StreamExtractor.Privacy privacy) {
        this.privacy = privacy;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getLicence() {
        return this.licence;
    }

    public void setLicence(final String licence) {
        this.licence = licence;
    }

    public Locale getLanguageInfo() {
        return this.language;
    }

    public void setLanguageInfo(final Locale locale) {
        this.language = locale;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    public void setSupportInfo(final String support) {
        this.supportInfo = support;
    }

    public String getSupportInfo() {
        return this.supportInfo;
    }

    public List<StreamSegment> getStreamSegments() {
        return streamSegments;
    }

    public void setStreamSegments(final List<StreamSegment> streamSegments) {
        this.streamSegments = streamSegments;
    }

    public void setMetaInfo(final List<MetaInfo> metaInfo) {
        this.metaInfo = metaInfo;
    }

    public List<Frameset> getPreviewFrames() {
        return previewFrames;
    }

    public void setPreviewFrames(final List<Frameset> previewFrames) {
        this.previewFrames = previewFrames;
    }

    @Nonnull
    public List<MetaInfo> getMetaInfo() {
        return this.metaInfo;
    }

    public boolean isShortFormContent() {
        return shortFormContent;
    }

    public void setShortFormContent(final boolean isShortFormContent) {
        this.shortFormContent = isShortFormContent;
    }
}
