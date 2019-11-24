package org.schabi.newpipe.extractor.stream;

/*
 * Created by Christian Schabesberger on 26.08.15.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * StreamInfoItem.java is part of NewPipe.
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

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.localization.DateWrapper;

import javax.annotation.Nullable;

/**
 * Info object for previews of unopened videos, eg search results, related videos
 */
public class StreamInfoItem extends InfoItem {
    private final StreamType streamType;

    private String uploaderName;
    private String textualUploadDate;
    @Nullable private DateWrapper uploadDate;
    private long viewCount = -1;
    private long duration = -1;

    private String uploaderUrl = null;

    public StreamInfoItem(int serviceId, String url, String name, StreamType streamType) {
        super(InfoType.STREAM, serviceId, url, name);
        this.streamType = streamType;
    }

    public StreamType getStreamType() {
        return streamType;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploader_name) {
        this.uploaderName = uploader_name;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long view_count) {
        this.viewCount = view_count;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getUploaderUrl() {
        return uploaderUrl;
    }

    public void setUploaderUrl(String uploaderUrl) {
        this.uploaderUrl = uploaderUrl;
    }

    @Nullable
    public String getTextualUploadDate() {
        return textualUploadDate;
    }

    public void setTextualUploadDate(String uploadDate) {
        this.textualUploadDate = uploadDate;
    }

    @Nullable
    public DateWrapper getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(@Nullable DateWrapper uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Override
    public String toString() {
        return "StreamInfoItem{" +
                "streamType=" + streamType +
                ", uploaderName='" + uploaderName + '\'' +
                ", textualUploadDate='" + textualUploadDate + '\'' +
                ", viewCount=" + viewCount +
                ", duration=" + duration +
                ", uploaderUrl='" + uploaderUrl + '\'' +
                ", infoType=" + getInfoType() +
                ", serviceId=" + getServiceId() +
                ", url='" + getUrl() + '\'' +
                ", name='" + getName() + '\'' +
                ", thumbnailUrl='" + getThumbnailUrl() + '\'' +
                '}';
    }
}