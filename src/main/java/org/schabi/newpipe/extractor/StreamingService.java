package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.user.UserExtractor;

import java.io.IOException;

public abstract class StreamingService {
    public class ServiceInfo {
        public final String name;

        public ServiceInfo(String name) {
            this.name = name;
        }
    }

    public enum LinkType {
        NONE,
        STREAM,
        USER,
        PLAYLIST
    }

    private final int serviceId;
    private final ServiceInfo serviceInfo;

    public StreamingService(int id, String name) {
        this.serviceId = id;
        this.serviceInfo = new ServiceInfo(name);
    }

    public final int getServiceId() {
        return serviceId;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public abstract UrlIdHandler getStreamUrlIdHandler();
    public abstract UrlIdHandler getUserUrlIdHandler();
    public abstract UrlIdHandler getPlaylistUrlIdHandler();
    public abstract SearchEngine getSearchEngine();
    public abstract SuggestionExtractor getSuggestionExtractor();
    public abstract StreamExtractor getStreamExtractor(String url) throws IOException, ExtractionException;
    public abstract UserExtractor getUserExtractor(String url, String nextStreamsUrl) throws IOException, ExtractionException;
    public abstract PlaylistExtractor getPlaylistExtractor(String url, String nextStreamsUrl) throws IOException, ExtractionException;

    public UserExtractor getUserExtractor(String url) throws IOException, ExtractionException {
        return getUserExtractor(url, null);
    }

    public PlaylistExtractor getPlaylistExtractor(String url) throws IOException, ExtractionException {
        return getPlaylistExtractor(url, null);
    }

    /**
     * figure out where the link is pointing to (a user, video, playlist, etc.)
     */
    public final LinkType getLinkTypeByUrl(String url) {
        UrlIdHandler sH = getStreamUrlIdHandler();
        UrlIdHandler cH = getUserUrlIdHandler();
        UrlIdHandler pH = getPlaylistUrlIdHandler();

        if (sH.acceptUrl(url)) {
            return LinkType.STREAM;
        } else if (cH.acceptUrl(url)) {
            return LinkType.USER;
        } else if (pH.acceptUrl(url)) {
            return LinkType.PLAYLIST;
        } else {
            return LinkType.NONE;
        }
    }
}
