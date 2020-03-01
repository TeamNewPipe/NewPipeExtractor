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
            info.setThumbnails(extractor.getThumbnails());
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
            info.setUploaderAvatars(extractor.getUploaderAvatars());
        } catch (Exception e) {
            info.setUploaderAvatars(null);
            uploaderParsingErrors.add(e);
        }
        try {
            info.setBanners(extractor.getBanners());
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

    private List<Image> thumbnails;
    private List<Image> banners;
    private String uploaderUrl;
    private String uploaderName;
    private List<Image> uploaderAvatars;
    private long streamCount = 0;

    public List<Image> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(List<Image> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public List<Image> getBanners() {
        return banners;
    }

    public void setBanners(List<Image> banners) {
        this.banners = banners;
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

    public List<Image> getUploaderAvatars() {
        return uploaderAvatars;
    }

    public void setUploaderAvatars(List<Image> uploaderAvatars) {
        this.uploaderAvatars = uploaderAvatars;
    }

    public long getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(long streamCount) {
        this.streamCount = streamCount;
    }
}
