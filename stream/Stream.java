package org.schabi.newpipe.extractor.stream;

import java.io.Serializable;
import java.util.List;

public abstract class Stream implements Serializable {
    public String url;
    public int format = -1;

    public Stream(String url, int format) {
        this.url = url;
        this.format = format;
    }

    /**
     * Reveals whether two streams are the same, but have different urls
     */
    public boolean equalStats(Stream cmp) {
        return cmp != null && format == cmp.format;
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
}
