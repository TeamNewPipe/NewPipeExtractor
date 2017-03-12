package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream_info.StreamInfoItemCollector;

import java.util.List;
import java.util.Vector;

public class PlayListInfo {

    public void addException(Exception e) {
        errors.add(e);
    }

    public static PlayListInfo getInfo(PlayListExtractor extractor) throws ParsingException {
        PlayListInfo info = new PlayListInfo();

        info.playList_name = extractor.getName();
        info.hasNextPage = extractor.hasNextPage();

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
            StreamInfoItemCollector c = extractor.getStreams();
            info.related_streams = c.getItemList();
            info.errors.addAll(c.getErrors());
        } catch(Exception e) {
            info.errors.add(e);
        }

        return info;
    }

    public int service_id = -1;
    public String playList_name = "";
    public String avatar_url = "";
    public String banner_url = "";
    public List<InfoItem> related_streams = null;
    public boolean hasNextPage = false;

    public List<Throwable> errors = new Vector<>();
}
