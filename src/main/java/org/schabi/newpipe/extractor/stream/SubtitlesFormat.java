package org.schabi.newpipe.extractor.stream;

public enum SubtitlesFormat {
    // YouTube subtitles formats
    // TRANSCRIPT(3) is default YT format based on TTML,
    // but unlike VTT or TTML, it is NOT W3 standard
    // TRANSCRIPT subtitles are NOT supported by ExoPlayer, only VTT and TTML
    VTT (0x0, "vtt"),
    TTML (0x1, "ttml"),
    TRANSCRIPT1 (0x2, "srv1"),
    TRANSCRIPT2 (0x3, "srv2"),
    TRANSCRIPT3 (0x4, "srv3");

    private final int id;
    private final String extension;

    SubtitlesFormat(int id, String extension) {
        this.id = id;
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
