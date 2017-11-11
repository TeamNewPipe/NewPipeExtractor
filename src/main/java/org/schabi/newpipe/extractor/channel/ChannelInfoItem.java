package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.InfoItem;

/*
 * Created by Christian Schabesberger on 11.02.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * ChannelInfoItem.java is part of NewPipe.
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

public class ChannelInfoItem extends InfoItem {

    public String description;
    public long subscriber_count = -1;
    public long stream_count = -1;


    public ChannelInfoItem(int serviceId, String url, String name) {
        super(InfoType.CHANNEL, serviceId, url, name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getSubscriberCount() {
        return subscriber_count;
    }

    public void setSubscriberCount(long subscriber_count) {
        this.subscriber_count = subscriber_count;
    }

    public long getStreamCount() {
        return stream_count;
    }

    public void setStreamCount(long stream_count) {
        this.stream_count = stream_count;
    }
}
