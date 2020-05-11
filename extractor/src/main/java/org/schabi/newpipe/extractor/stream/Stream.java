package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.MediaFormat;

import java.io.Serializable;
import java.util.List;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * Creates a stream object from url, format and optional torrent url
 */
public abstract class Stream implements Serializable {
    private final MediaFormat mediaFormat;
    public final String url;
    public final String torrentUrl;

    /**
     * @deprecated Use {@link #getFormat()}  or {@link #getFormatId()}
     */
    @Deprecated
    public final int format;

    /**
     * Instantiates a new stream object.
     *
     * @param url    the url
     * @param format the format
     */
    public Stream(String url, MediaFormat format) {
        this(url, null, format);
    }

    /**
     * Instantiates a new stream object.
     *
     * @param url        the url
     * @param torrentUrl the url to torrent file, example https://webtorrent.io/torrents/big-buck-bunny.torrent
     * @param format     the format
     */
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
        if (isNullOrEmpty(streamList)) return false;
        for (Stream cmpStream : streamList) {
            if (stream.equalStats(cmpStream)) return true;
        }
        return false;
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the torrent url.
     *
     * @return the torrent url, example https://webtorrent.io/torrents/big-buck-bunny.torrent
     */
    public String getTorrentUrl() {
        return torrentUrl;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public MediaFormat getFormat() {
        return mediaFormat;
    }

    /**
     * Gets the format id.
     *
     * @return the format id
     */
    public int getFormatId() {
        return mediaFormat.id;
    }
}
