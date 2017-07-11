package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import java.util.List;

public class PlaylistInfo extends Info {

    public static PlaylistInfo getInfo(PlaylistExtractor extractor) throws ParsingException {
        PlaylistInfo info = new PlaylistInfo();

        info.service_id = extractor.getServiceId();
        info.url = extractor.getUrl();
        info.id = extractor.getPlaylistId();
        info.name = extractor.getPlaylistName();
        info.has_more_streams = extractor.hasMoreStreams();

        try {
            info.streams_count = extractor.getStreamsCount();
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

        return info;
    }

    public String avatar_url;
    public String banner_url;
    public String uploader_url;
    public String uploader_name;
    public String uploader_avatar_url;
    public long streams_count = 0;
    public List<InfoItem> related_streams;
    public boolean has_more_streams;
}
