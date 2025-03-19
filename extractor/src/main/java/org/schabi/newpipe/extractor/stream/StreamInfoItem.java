/*
 * Created by Christian Schabesberger on 26.08.15.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * StreamInfoItem.java is part of NewPipe Extractor.
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
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.localization.DateWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;

/**
 * Info object for previews of unopened videos, e.g. search results, related videos.
 */
public class StreamInfoItem extends InfoItem {
    private final StreamType streamType;

    private String uploaderName;
    private String shortDescription;
    private String textualUploadDate;
    @Nullable
    private DateWrapper uploadDate;
    private long viewCount = -1;
    @Nonnull
    private Duration duration = Duration.ZERO;

    private String uploaderUrl = null;
    @Nonnull
    private List<Image> uploaderAvatars = List.of();
    private boolean uploaderVerified = false;
    private boolean shortFormContent = false;

    public StreamInfoItem(final int serviceId,
                          final String url,
                          final String name,
                          final StreamType streamType) {
        super(InfoType.STREAM, serviceId, url, name);
        this.streamType = streamType;
    }

    public StreamType getStreamType() {
        return streamType;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(final String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(final long viewCount) {
        this.viewCount = viewCount;
    }

    @Nonnull
    public Duration getDurationObject() {
        return duration;
    }

    public void setDurationObject(@Nonnull final Duration durationObject) {
        this.duration = durationObject;
    }

    public long getDuration() {
        return duration.toSeconds();
    }

    public void setDuration(final long duration) {
        this.duration = Duration.ofSeconds(duration);
    }

    public String getUploaderUrl() {
        return uploaderUrl;
    }

    public void setUploaderUrl(final String uploaderUrl) {
        this.uploaderUrl = uploaderUrl;
    }

    @Nonnull
    public List<Image> getUploaderAvatars() {
        return uploaderAvatars;
    }

    public void setUploaderAvatars(@Nonnull final List<Image> uploaderAvatars) {
        this.uploaderAvatars = uploaderAvatars;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(final String shortDescription) {
        this.shortDescription = shortDescription;
    }

    @Nullable
    public String getTextualUploadDate() {
        return textualUploadDate;
    }

    public void setTextualUploadDate(final String textualUploadDate) {
        this.textualUploadDate = textualUploadDate;
    }

    @Nullable
    public DateWrapper getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(@Nullable final DateWrapper uploadDate) {
        this.uploadDate = uploadDate;
    }

    public boolean isUploaderVerified() {
        return uploaderVerified;
    }

    public void setUploaderVerified(final boolean uploaderVerified) {
        this.uploaderVerified = uploaderVerified;
    }

    public boolean isShortFormContent() {
        return shortFormContent;
    }

    public void setShortFormContent(final boolean shortFormContent) {
        this.shortFormContent = shortFormContent;
    }

    @Override
    public String toString() {
        return "StreamInfoItem{"
                + "streamType=" + streamType
                + ", uploaderName='" + uploaderName + '\''
                + ", textualUploadDate='" + textualUploadDate + '\''
                + ", viewCount=" + viewCount
                + ", duration=" + duration
                + ", uploaderUrl='" + uploaderUrl + '\''
                + ", infoType=" + getInfoType()
                + ", serviceId=" + getServiceId()
                + ", url='" + getUrl() + '\''
                + ", name='" + getName() + '\''
                + ", thumbnails='" + getThumbnails() + '\''
                + ", uploaderVerified='" + isUploaderVerified() + '\''
                + '}';
    }
}
