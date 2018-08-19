package org.schabi.newpipe.extractor;

/*
 * Created by Christian Schabesberger on 11.02.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * InfoItem.java is part of NewPipe.
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

import java.io.Serializable;

public abstract class InfoItem implements Serializable {
    private final InfoType infoType;
    private final int serviceId;
    private final String url;
    private final String name;
    private String thumbnailUrl;

    public InfoItem(InfoType infoType, int serviceId, String url, String name) {
        this.infoType = infoType;
        this.serviceId = serviceId;
        this.url = url;
        this.name = name;
    }

    public InfoType getInfoType() {
        return infoType;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[url=\"" + url + "\", name=\"" + name + "\"]";
    }

    public enum InfoType {
        STREAM,
        PLAYLIST,
        CHANNEL,
        COMMENT
    }
}
