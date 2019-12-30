package org.schabi.newpipe.extractor.services.peertube.extractors;

import java.io.IOException;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

public class PeertubePlaylistExtractor extends PlaylistExtractor{

    public PeertubePlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getStreamCount() throws ParsingException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getName() throws ParsingException {
        // TODO Auto-generated method stub
        return null;
    }

}
