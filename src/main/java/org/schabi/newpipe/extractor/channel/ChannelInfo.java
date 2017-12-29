package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.ListExtractor.NextItemsResult;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;

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

    public ChannelInfo(int serviceId, String url, String id, String name) {
        super(serviceId, id, url, name);
    }

    public static NextItemsResult getMoreItems(ServiceList serviceItem, String url, String nextStreamsUrl) throws IOException, ExtractionException {
        return getMoreItems(serviceItem.getService(), url, nextStreamsUrl);
    }

    public static NextItemsResult getMoreItems(StreamingService service, String url, String nextStreamsUrl) throws IOException, ExtractionException {
        return service.getChannelExtractor(url, nextStreamsUrl).getNextStreams();
    }

    public static ChannelInfo getInfoFromFeed(String url) throws IOException, ExtractionException {
        return getInfoFromFeed(NewPipe.getServiceByUrl(url), url);
    }

    public static ChannelInfo getInfoFromFeed(ServiceList serviceItem, String url) throws IOException, ExtractionException {
        return getInfoFromFeed(serviceItem.getService(), url);
    }

    public static ChannelInfo getInfoFromFeed(StreamingService service, String url) throws IOException, ExtractionException {
        FeedExtractor extractor = service.getFeedExtractor(url);
        extractor.fetchPage();
        return getInfoFromFeed(service.getFeedExtractor(url));
    }

    public static ChannelInfo getInfoFromFeed(FeedExtractor extractor) throws ParsingException {

        // important data
        int serviceId = extractor.getServiceId();
        String url = extractor.getCleanUrl();
        String id = extractor.getId();
        String name = extractor.getName();

        ChannelInfo info = new ChannelInfo(serviceId, url, id, name);


        try {
            info.setAvatarUrl(extractor.getAvatarUrl());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setBannerUrl(extractor.getBannerUrl());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setFeedUrl(extractor.getFeedUrl());
        } catch (Exception e) {
            info.addError(e);
        }

        info.setRelatedStreams(ExtractorHelper.getStreamsOrLogError(info, extractor));

        try {
            info.setSubscriberCount(extractor.getSubscriberCount());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setDescription(extractor.getDescription());
        } catch (Exception e) {
            info.addError(e);
        }

        info.setHasMoreStreams(extractor.hasMoreStreams());
        info.setNextStreamsUrl(extractor.getNextStreamsUrl());
        return info;
    }


    public static ChannelInfo getInfo(String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static ChannelInfo getInfo(ServiceList serviceItem, String url) throws IOException, ExtractionException {
        return getInfo(serviceItem.getService(), url);
    }

    public static ChannelInfo getInfo(StreamingService service, String url) throws IOException, ExtractionException {
        ChannelExtractor extractor = service.getChannelExtractor(url);
        extractor.fetchPage();
        return getInfo(extractor);
    }

    public static ChannelInfo getInfo(ChannelExtractor extractor) throws ParsingException {

        // important data
        int serviceId = extractor.getServiceId();
        String url = extractor.getCleanUrl();
        String id = extractor.getId();
        String name = extractor.getName();

        ChannelInfo info = new ChannelInfo(serviceId, url, id, name);


        try {
            info.setAvatarUrl(extractor.getAvatarUrl());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setBannerUrl(extractor.getBannerUrl());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setFeedUrl(extractor.getFeedUrl());
        } catch (Exception e) {
            info.addError(e);
        }

        info.setRelatedStreams(ExtractorHelper.getStreamsOrLogError(info, extractor));

        try {
            info.setSubscriberCount(extractor.getSubscriberCount());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setDescription(extractor.getDescription());
        } catch (Exception e) {
            info.addError(e);
        }

        info.setHasMoreStreams(extractor.hasMoreStreams());
        info.setNextStreamsUrl(extractor.getNextStreamsUrl());
        return info;
    }

    public String avatar_url;
    public String banner_url;
    public String feed_url;
    public long subscriber_count = -1;
    public String description;

    public String getAvatarUrl() {
        return avatar_url;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatar_url = avatarUrl;
    }

    public String getBannerUrl() {
        return banner_url;
    }

    public void setBannerUrl(String bannerUrl) {
        this.banner_url = bannerUrl;
    }

    public String getFeedUrl() {
        return feed_url;
    }

    public void setFeedUrl(String feedUrl) {
        this.feed_url = feedUrl;
    }

    public long getSubscriberCount() {
        return subscriber_count;
    }

    public void setSubscriberCount(long subscriberCount) {
        this.subscriber_count = subscriberCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
