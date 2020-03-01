package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistInfo extends ListInfo<StreamInfoItem> {

    private PlaylistInfo(int serviceId, ListLinkHandler linkHandler, String name) throws ParsingException {
        super(serviceId, linkHandler, name);
    }

    public static PlaylistInfo getInfo(String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static PlaylistInfo getInfo(StreamingService service, String url) throws IOException, ExtractionException {
        PlaylistExtractor extractor = service.getPlaylistExtractor(url);
        extractor.fetchPage();
        return getInfo(extractor);
    }

    public static InfoItemsPage<StreamInfoItem> getMoreItems(StreamingService service,
                                                             String url,
                                                             String pageUrl) throws IOException, ExtractionException {
        return service.getPlaylistExtractor(url).getPage(pageUrl);
    }

    /**
     * Get PlaylistInfo from PlaylistExtractor
     *
     * @param extractor an extractor where fetchPage() was already got called on.
     */
    public static PlaylistInfo getInfo(PlaylistExtractor extractor) throws ExtractionException {

        final PlaylistInfo info = new PlaylistInfo(
                extractor.getServiceId(),
                extractor.getLinkHandler(),
                extractor.getName());
        // collect uploader extraction failures until we are sure this is not
        // just a playlist without an uploader
        List<Throwable> uploaderParsingErrors = new ArrayList<Throwable>(3);

        try {
            info.setOriginalUrl(extractor.getOriginalUrl());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setStreamCount(extractor.getStreamCount());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setThumbnail(extractor.getThumbnail());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setUploaderUrl(extractor.getUploaderUrl());
        } catch (Exception e) {
            info.setUploaderUrl("");
            uploaderParsingErrors.add(e);
        }
        try {
            info.setUploaderName(extractor.getUploaderName());
        } catch (Exception e) {
            info.setUploaderName("");
            uploaderParsingErrors.add(e);
        }
        try {
            info.setUploaderAvatar(extractor.getUploaderAvatar());
        } catch (Exception e) {
            info.setUploaderAvatar(null);
            uploaderParsingErrors.add(e);
        }
        try {
            info.setBanner(extractor.getBanner());
        } catch (Exception e) {
            info.addError(e);
        }
        // do not fail if everything but the uploader infos could be collected
        if (uploaderParsingErrors.size() > 0 &&
                (!info.getErrors().isEmpty() || uploaderParsingErrors.size() < 3)) {
            info.addAllErrors(uploaderParsingErrors);
        }

        final InfoItemsPage<StreamInfoItem> itemsPage = ExtractorHelper.getItemsPageOrLogError(info, extractor);
        info.setRelatedItems(itemsPage.getItems());
        info.setNextPageUrl(itemsPage.getNextPageUrl());

        return info;
    }

    private Image thumbnail;
    private Image banner;
    private String uploaderUrl;
    private String uploaderName;
    private Image uploaderAvatar;
    private long streamCount = 0;

    public Image getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Image getBanner() {
        return banner;
    }

    public void setBanner(Image banner) {
        this.banner = banner;
    }

    public String getUploaderUrl() {
        return uploaderUrl;
    }

    public void setUploaderUrl(String uploaderUrl) {
        this.uploaderUrl = uploaderUrl;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public Image getUploaderAvatar() {
        return uploaderAvatar;
    }

    public void setUploaderAvatar(Image uploaderAvatar) {
        this.uploaderAvatar = uploaderAvatar;
    }

    public long getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(long streamCount) {
        this.streamCount = streamCount;
    }
}
