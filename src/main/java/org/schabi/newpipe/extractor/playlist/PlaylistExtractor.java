package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import java.io.IOException;

public abstract class PlaylistExtractor extends ListExtractor {

    public PlaylistExtractor(UrlIdHandler urlIdHandler, String url, int serviceId) throws ExtractionException, IOException {
        super(urlIdHandler, serviceId, url);
    }

    public abstract String getPlaylistId() throws ParsingException;
    public abstract String getPlaylistName() throws ParsingException;
    public abstract String getAvatarUrl() throws ParsingException;
    public abstract String getBannerUrl() throws ParsingException;
    public abstract String getUploaderUrl() throws ParsingException;
    public abstract String getUploaderName() throws ParsingException;
    public abstract String getUploaderAvatarUrl() throws ParsingException;
    public abstract StreamInfoItemCollector getStreams() throws ParsingException, ReCaptchaException, IOException;
    public abstract long getStreamsCount() throws ParsingException;
}
