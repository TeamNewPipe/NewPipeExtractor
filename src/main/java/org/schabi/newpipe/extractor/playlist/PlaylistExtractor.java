package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;

public abstract class PlaylistExtractor extends ListExtractor {

    public PlaylistExtractor(StreamingService service, String url, String nextStreamsUrl) throws IOException, ExtractionException {
        super(service, url, nextStreamsUrl);
    }

    @Override
    protected UrlIdHandler getUrlIdHandler() throws ParsingException {
        return getService().getPlaylistUrlIdHandler();
    }

    public abstract String getPlaylistId() throws ParsingException;
    public abstract String getPlaylistName() throws ParsingException;
    public abstract String getAvatarUrl() throws ParsingException;
    public abstract String getBannerUrl() throws ParsingException;
    public abstract String getUploaderUrl() throws ParsingException;
    public abstract String getUploaderName() throws ParsingException;
    public abstract String getUploaderAvatarUrl() throws ParsingException;
    public abstract long getStreamCount() throws ParsingException;
}
