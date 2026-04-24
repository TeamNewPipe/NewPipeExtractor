/*
 * Created by Christian Schabesberger on 11.02.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * InfoItem.java is part of NewPipe Extractor.
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

package org.schabi.newpipe.extractor;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

public abstract class InfoItem implements Serializable {
    private final InfoType infoType;
    private final int serviceId;
    private final String url;
    private final String name;
    @Nonnull
    private List<Image> thumbnails = List.of();

    public InfoItem(final InfoType infoType,
                    final int serviceId,
                    final String url,
                    final String name) {
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

    public void setThumbnails(@Nonnull final List<Image> thumbnails) {
        this.thumbnails = thumbnails;
    }

    @Nonnull
    public List<Image> getThumbnails() {
        return thumbnails;
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
