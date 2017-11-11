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

/**
 * Info object for previews of unopened videos, eg search results, related videos
 */
public class StreamInfoItem extends InfoItem {
    public final StreamType stream_type;

    public String uploader_name;
    public String upload_date;
    public long view_count = -1;
    public long duration = -1;

    private String uploaderUrl = null;

    public StreamInfoItem(int serviceId, String url, String name, StreamType streamType) {
        super(InfoType.STREAM, serviceId, url, name);
        this.stream_type = streamType;
    }

    public void setUploaderUrl(String uploaderUrl) {
        this.uploaderUrl = uploaderUrl;
    }

    public String getUploaderUrl() {
        return uploaderUrl;
    }

    public StreamType getStreamType() {
        return stream_type;
    }

    public String getUploaderName() {
        return uploader_name;
    }

    public String getUploadDate() {
        return upload_date;
    }

    public long getViewCount() {
        return view_count;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setUploaderName(String uploader_name) {
        this.uploader_name = uploader_name;
    }

    public void setUploadDate(String upload_date) {
        this.upload_date = upload_date;
    }

    public void setViewCount(long view_count) {
        this.view_count = view_count;
    }
}