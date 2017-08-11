package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.ListExtractor.NextItemsResult;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import java.io.IOException;
import java.util.ArrayList;

/*
 * Created by Christian Schabesberger on 31.07.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * ChannelInfo.java is part of NewPipe.
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

public class ChannelInfo extends ListInfo {

    public static NextItemsResult getMoreItems(ServiceList serviceItem, String nextStreamsUrl) throws IOException, ExtractionException {
        return getMoreItems(serviceItem.getService(), nextStreamsUrl);
    }

    public static NextItemsResult getMoreItems(StreamingService service, String nextStreamsUrl) throws IOException, ExtractionException {
        return service.getChannelExtractor(null, nextStreamsUrl).getNextStreams();
    }

    public static ChannelInfo getInfo(String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static ChannelInfo getInfo(ServiceList serviceItem, String url) throws IOException, ExtractionException {
        return getInfo(serviceItem.getService(), url);
    }

    public static ChannelInfo getInfo(StreamingService service, String url) throws IOException, ExtractionException {
        return getInfo(service.getChannelExtractor(url));
    }

    public static ChannelInfo getInfo(ChannelExtractor extractor) throws ParsingException {
        ChannelInfo info = new ChannelInfo();

        // important data
        info.service_id = extractor.getServiceId();
        info.url = extractor.getCleanUrl();
        info.id = extractor.getId();
        info.name = extractor.getName();

        try {
            info.avatar_url = extractor.getAvatarUrl();
        } catch (Exception e) {
            info.errors.add(e);
        }
        try {
            info.banner_url = extractor.getBannerUrl();
        } catch (Exception e) {
            info.errors.add(e);
        }
        try {
            info.feed_url = extractor.getFeedUrl();
        } catch (Exception e) {
            info.errors.add(e);
        }
        try {
            StreamInfoItemCollector c = extractor.getStreams();
            info.related_streams = c.getItemList();
            info.errors.addAll(c.getErrors());
        } catch (Exception e) {
            info.errors.add(e);
        }
        try {
            info.subscriber_count = extractor.getSubscriberCount();
        } catch (Exception e) {
            info.errors.add(e);
        }
        try {
            info.description = extractor.getDescription();
        } catch (Exception e) {
            info.errors.add(e);
        }

        // Lists can be null if a exception was thrown during extraction
        if (info.related_streams == null) info.related_streams = new ArrayList<>();

        info.has_more_streams = extractor.hasMoreStreams();
        info.next_streams_url = extractor.getNextStreamsUrl();
        return info;
    }

    public String avatar_url;
    public String banner_url;
    public String feed_url;
    public long subscriber_count = -1;
    public String description;
}
