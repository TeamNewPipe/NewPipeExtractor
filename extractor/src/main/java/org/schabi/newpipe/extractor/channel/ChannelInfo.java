/*
 * Created by Christian Schabesberger on 31.07.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * ChannelInfo.java is part of NewPipe Extractor.
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

package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

public class ChannelInfo extends Info {

    public ChannelInfo(final int serviceId,
                       final String id,
                       final String url,
                       final String originalUrl,
                       final String name) {
        super(serviceId, id, url, originalUrl, name);
    }

    public static ChannelInfo getInfo(final String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static ChannelInfo getInfo(final StreamingService service, final String url)
            throws IOException, ExtractionException {
        final ChannelExtractor extractor = service.getChannelExtractor(url);
        extractor.fetchPage();
        return getInfo(extractor);
    }

    public static ChannelInfo getInfo(final ChannelExtractor extractor)
            throws IOException, ExtractionException {

        final int serviceId = extractor.getServiceId();
        final String id = extractor.getId();
        final String url = extractor.getUrl();
        final String originalUrl = extractor.getOriginalUrl();
        final String name = extractor.getName();

        final ChannelInfo info = new ChannelInfo(serviceId, id, url, originalUrl, name);

        try {
            info.setAvatars(extractor.getAvatars());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setBanners(extractor.getBanners());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setFeedUrl(extractor.getFeedUrl());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setSubscriberCount(extractor.getSubscriberCount());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setDescription(extractor.getDescription());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setParentChannelName(extractor.getParentChannelName());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setParentChannelUrl(extractor.getParentChannelUrl());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setParentChannelAvatars(extractor.getParentChannelAvatars());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setVerified(extractor.isVerified());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setTabs(extractor.getTabs());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setTags(extractor.getTags());
        } catch (final Exception e) {
            info.addError(e);
        }

        return info;
    }

    private String parentChannelName;
    private String parentChannelUrl;
    private String feedUrl;
    private long subscriberCount = -1;
    private String description;
    private String[] donationLinks;
    @Nonnull
    private List<Image> avatars = List.of();
    @Nonnull
    private List<Image> banners = List.of();
    @Nonnull
    private List<Image> parentChannelAvatars = List.of();
    private boolean verified;
    private List<ListLinkHandler> tabs = List.of();
    private List<String> tags = List.of();

    public String getParentChannelName() {
        return parentChannelName;
    }

    public void setParentChannelName(final String parentChannelName) {
        this.parentChannelName = parentChannelName;
    }

    public String getParentChannelUrl() {
        return parentChannelUrl;
    }

    public void setParentChannelUrl(final String parentChannelUrl) {
        this.parentChannelUrl = parentChannelUrl;
    }

    @Nonnull
    public List<Image> getParentChannelAvatars() {
        return parentChannelAvatars;
    }

    public void setParentChannelAvatars(@Nonnull final List<Image> parentChannelAvatars) {
        this.parentChannelAvatars = parentChannelAvatars;
    }

    @Nonnull
    public List<Image> getAvatars() {
        return avatars;
    }

    public void setAvatars(@Nonnull final List<Image> avatars) {
        this.avatars = avatars;
    }

    @Nonnull
    public List<Image> getBanners() {
        return banners;
    }

    public void setBanners(@Nonnull final List<Image> banners) {
        this.banners = banners;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(final String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public long getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(final long subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String[] getDonationLinks() {
        return donationLinks;
    }

    public void setDonationLinks(final String[] donationLinks) {
        this.donationLinks = donationLinks;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(final boolean verified) {
        this.verified = verified;
    }

    @Nonnull
    public List<ListLinkHandler> getTabs() {
        return tabs;
    }

    public void setTabs(@Nonnull final List<ListLinkHandler> tabs) {
        this.tabs = tabs;
    }

    @Nonnull
    public List<String> getTags() {
        return tags;
    }

    public void setTags(@Nonnull final List<String> tags) {
        this.tags = tags;
    }
}
