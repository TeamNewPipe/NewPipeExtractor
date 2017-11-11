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
    public final InfoType info_type;
    public int service_id = -1;
    public String url;
    public String name;
    public String thumbnail_url;

    public InfoItem(InfoType infoType) {
        this.info_type = infoType;
    }

    public InfoType getInfoType() {
        return info_type;
    }

    public int getServiceId() {
        return service_id;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getThumbnailUrl() {
        return thumbnail_url;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[url=\"" + url + "\", name=\"" + name + "\"]";
    }

    public enum InfoType {
        STREAM,
        PLAYLIST,
        CHANNEL
    }
}
