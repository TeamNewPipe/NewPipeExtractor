package org.schabi.newpipe.extractor.playlist;

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

public class PlaylistInfo extends ListInfo {

    public static NextItemsResult getMoreItems(ServiceList serviceItem, String nextStreamsUrl) throws IOException, ExtractionException {
        return getMoreItems(serviceItem.getService(), nextStreamsUrl);
    }

    public static NextItemsResult getMoreItems(StreamingService service, String nextStreamsUrl) throws IOException, ExtractionException {
        return service.getPlaylistExtractor(null, nextStreamsUrl).getNextStreams();
    }

    public static PlaylistInfo getInfo(String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static PlaylistInfo getInfo(ServiceList serviceItem, String url) throws IOException, ExtractionException {
        return getInfo(serviceItem.getService(), url);
    }

    public static PlaylistInfo getInfo(StreamingService service, String url) throws IOException, ExtractionException {
        return getInfo(service.getPlaylistExtractor(url));
    }

    public static PlaylistInfo getInfo(PlaylistExtractor extractor) throws ParsingException {
        PlaylistInfo info = new PlaylistInfo();

        info.service_id = extractor.getServiceId();
        info.url = extractor.getCleanUrl();
        info.id = extractor.getPlaylistId();
        info.name = extractor.getPlaylistName();

        try {
            info.stream_count = extractor.getStreamCount();
        } catch (Exception e) {
            info.errors.add(e);
        }
        try {
            info.avatar_url = extractor.getAvatarUrl();
        } catch (Exception e) {
            info.errors.add(e);
        }
        try {
            info.uploader_url = extractor.getUploaderUrl();
        } catch (Exception e) {
            info.errors.add(e);
        }
        try {
            info.uploader_name = extractor.getUploaderName();
        } catch (Exception e) {
            info.errors.add(e);
        }
        try {
            info.uploader_avatar_url = extractor.getUploaderAvatarUrl();
        } catch (Exception e) {
            info.errors.add(e);
        }
        try {
            info.banner_url = extractor.getBannerUrl();
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

        // Lists can be null if a exception was thrown during extraction
        if (info.related_streams == null) info.related_streams = new ArrayList<>();

        info.has_more_streams = extractor.hasMoreStreams();
        info.next_streams_url = extractor.getNextStreamsUrl();
        return info;
    }

    public String avatar_url;
    public String banner_url;
    public String uploader_url;
    public String uploader_name;
    public String uploader_avatar_url;
    public long stream_count = 0;
}
