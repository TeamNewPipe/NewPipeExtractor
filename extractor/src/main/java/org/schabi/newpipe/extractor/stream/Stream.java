package org.schabi.newpipe.extractor.stream;

import java.io.Serializable;
import java.util.List;

import org.schabi.newpipe.extractor.MediaFormat;

public abstract class Stream implements Serializable {
    private final MediaFormat mediaFormat;
    public final String url;
    public final String torrentUrl;

    /**
     * @deprecated Use {@link #getFormat()}  or {@link #getFormatId()}
     */
    @Deprecated
    public final int format;

    public Stream(String url, MediaFormat format) {
        this(url, null, format);
    }
    
    public Stream(String url, String torrentUrl, MediaFormat format) {
        this.url = url;
        this.torrentUrl = torrentUrl;
        this.format = format.id;
        this.mediaFormat = format;
    }

    /**
     * Reveals whether two streams have the same stats (format and bitrate, for example)
     */
    public boolean equalStats(Stream cmp) {
        return cmp != null && getFormatId() == cmp.getFormatId();
    }

    /**
     * Reveals whether two Streams are equal
     */
    public boolean equals(Stream cmp) {
        return equalStats(cmp) && url.equals(cmp.url);
    }

    /**
     * Check if the list already contains one stream with equals stats
     */
    public static boolean containSimilarStream(Stream stream, List<? extends Stream> streamList) {
        if (stream == null || streamList == null) return false;
        for (Stream cmpStream : streamList) {
            if (stream.equalStats(cmpStream)) return true;
        }
        return false;
    }

    public String getUrl() {
        return url;
    }
    
    public String getTorrentUrl() {
        return torrentUrl;
    }

    public MediaFormat getFormat() {
        return mediaFormat;
    }

    public int getFormatId() {
        return mediaFormat.id;
    }
}
