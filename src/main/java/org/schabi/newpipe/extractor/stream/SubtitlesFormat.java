package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.Subtitles;

public enum SubtitlesFormat {
    // YouTube subtitles formats
    // TRANSCRIPT(3) is default YT format based on TTML,
    // but unlike VTT or TTML, it is NOT W3 standard
    VTT (0x0, "vtt"),
    TTML (0x1, "ttml"),
    TRANSCRIPT1 (0x2, "srv1"),
    TRANSCRIPT2 (0x3, "srv2"),
    TRANSCRIPT3 (0x4, "srv3");

    private int id;
    private String extension;

    SubtitlesFormat(int id, String extension) {
        this.id = id;
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
