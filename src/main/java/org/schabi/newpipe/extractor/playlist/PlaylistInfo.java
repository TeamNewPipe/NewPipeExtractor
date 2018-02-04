package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.ListExtractor.NextItemsResult;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;

import static org.schabi.newpipe.extractor.utils.ExtractorHelper.getStreamsOrLogError;

public class PlaylistInfo extends ListInfo {

    public PlaylistInfo(int serviceId, String id, String url, String name) {
        super(serviceId, id, url, name);
    }

    public static NextItemsResult getMoreItems(ServiceList serviceItem, String url, String nextStreamsUrl) throws IOException, ExtractionException {
        return getMoreItems(serviceItem.getService(), url, nextStreamsUrl);
    }

    public static NextItemsResult getMoreItems(StreamingService service, String url, String nextStreamsUrl) throws IOException, ExtractionException {
        return service.getPlaylistExtractor(url, nextStreamsUrl).getNextStreams();
    }

    public static PlaylistInfo getInfo(String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static PlaylistInfo getInfo(ServiceList serviceItem, String url) throws IOException, ExtractionException {
        return getInfo(serviceItem.getService(), url);
    }

    public static PlaylistInfo getInfo(StreamingService service, String url) throws IOException, ExtractionException {
        PlaylistExtractor extractor = service.getPlaylistExtractor(url);
        extractor.fetchPage();
        return getInfo(extractor);
    }

    /**
     * Get PlaylistInfo from PlaylistExtractor
     *
     * @param extractor an extractor where fetchPage() was already got called on.
     */
    public static PlaylistInfo getInfo(PlaylistExtractor extractor) throws ParsingException {

        int serviceId = extractor.getServiceId();
        String url = extractor.getCleanUrl();
        String id = extractor.getId();
        String name = extractor.getName();
        PlaylistInfo info = new PlaylistInfo(serviceId, id, url, name);

        try {
            info.setStreamCount(extractor.getStreamCount());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setThumbnailUrl(extractor.getThumbnailUrl());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setUploaderUrl(extractor.getUploaderUrl());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setUploaderName(extractor.getUploaderName());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setUploaderAvatarUrl(extractor.getUploaderAvatarUrl());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setBannerUrl(extractor.getBannerUrl());
        } catch (Exception e) {
            info.addError(e);
        }

        info.setRelatedStreams(getStreamsOrLogError(info, extractor));
        info.setHasMoreStreams(extractor.hasMoreStreams());
        info.setNextStreamsUrl(extractor.getNextStreamsUrl());
        return info;
    }

    public String thumbnail_url;
    public String banner_url;
    public String uploader_url;
    public String uploader_name;
    public String uploader_avatar_url;
    public long stream_count = 0;

    public String getThumbnailUrl() {
        return thumbnail_url;
    }

    public String getBannerUrl() {
        return banner_url;
    }

    public String getUploaderUrl() {
        return uploader_url;
    }

    public String getUploaderName() {
        return uploader_name;
    }

    public String getUploaderAvatarUrl() {
        return uploader_avatar_url;
    }

    public long getStreamCount() {
        return stream_count;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnail_url = thumbnailUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.banner_url = bannerUrl;
    }

    public void setUploaderUrl(String uploaderUrl) {
        this.uploader_url = uploaderUrl;
    }

    public void setUploaderName(String uploaderName) {
        this.uploader_name = uploaderName;
    }

    public void setUploaderAvatarUrl(String uploaderAvatarUrl) {
        this.uploader_avatar_url = uploaderAvatarUrl;
    }

    public void setStreamCount(long streamCount) {
        this.stream_count = streamCount;
    }
}
